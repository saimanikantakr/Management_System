package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.dto.request.EmployeeRequestDTO;
import com.example.employeemanagement.dto.response.EmployeeResponseDTO;
import com.example.employeemanagement.dto.response.PageResponseDTO;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.DuplicateResourceException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.mapper.EmployeeMapper;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "email", "salary");

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO) {
        Department department = departmentRepository.findById(requestDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", requestDTO.getDepartmentId()));

        if (employeeRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Employee", "email", requestDTO.getEmail());
        }

        Employee employee = employeeMapper.toEntity(requestDTO, department);
        department.addEmployee(employee);
        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDto(savedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employeeMapper.toDtoList(employees);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return employeeMapper.toDto(employee);
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (employeeRepository.existsByEmailAndIdNot(requestDTO.getEmail(), id)) {
            throw new DuplicateResourceException("Employee", "email", requestDTO.getEmail());
        }

        Department department = departmentRepository.findById(requestDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", requestDTO.getDepartmentId()));

        if (existingEmployee.getDepartment() != null && !existingEmployee.getDepartment().getId().equals(department.getId())) {
            existingEmployee.getDepartment().removeEmployee(existingEmployee);
            department.addEmployee(existingEmployee);
        }

        employeeMapper.updateEntityFromDto(existingEmployee, requestDTO, department);
        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        return employeeMapper.toDto(updatedEmployee);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (employee.getDepartment() != null) {
            employee.getDepartment().removeEmployee(employee);
        }
        employeeRepository.delete(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<EmployeeResponseDTO> getEmployeesPaginated(int page, int size, String sortBy, String direction) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        String sortProperty = (sortBy == null || sortBy.trim().isEmpty()) ? "id" : sortBy.trim();
        if (!ALLOWED_SORT_FIELDS.contains(sortProperty)) {
            throw new IllegalArgumentException("Invalid sort property: '" + sortProperty
                    + "'. Allowed values: " + ALLOWED_SORT_FIELDS);
        }

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortProperty));

        Page<Employee> employeePage = employeeRepository.findAll(pageable);
        List<EmployeeResponseDTO> content = employeeMapper.toDtoList(employeePage.getContent());

        return new PageResponseDTO<>(
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages(),
                employeePage.isLast(),
                content
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getEmployeesBySalaryGreaterThan(Double salary) {
        if (salary == null || salary < 0) {
            throw new IllegalArgumentException("Salary threshold must be non-negative");
        }
        List<Employee> employees = employeeRepository.findBySalaryGreaterThan(salary);
        return employeeMapper.toDtoList(employees);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> searchEmployeesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword must not be blank");
        }
        List<Employee> employees = employeeRepository.findByNameContainingIgnoreCase(name.trim());
        return employeeMapper.toDtoList(employees);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getEmployeesBySalaryBetween(Double minSalary, Double maxSalary) {
        if (minSalary == null || maxSalary == null) {
            throw new IllegalArgumentException("Min and max salary must not be null");
        }
        if (minSalary < 0 || maxSalary < 0) {
            throw new IllegalArgumentException("Salary values must be non-negative");
        }
        if (minSalary > maxSalary) {
            throw new IllegalArgumentException("Minimum salary (" + minSalary + ") cannot exceed maximum salary (" + maxSalary + ")");
        }
        List<Employee> employees = employeeRepository.findBySalaryBetween(minSalary, maxSalary);
        return employeeMapper.toDtoList(employees);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getEmployeesByDepartmentId(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }
        List<Employee> employees = employeeRepository.findByDepartmentId(departmentId);
        return employeeMapper.toDtoList(employees);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getEmployeesWithSalaryGreaterThanOrderBySalaryDesc(Double salary) {
        if (salary == null || salary < 0) {
            throw new IllegalArgumentException("Salary threshold must be non-negative");
        }
        List<Employee> employees = employeeRepository.findEmployeesWithSalaryGreaterThanOrderBySalaryDesc(salary);
        return employeeMapper.toDtoList(employees);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getEmployeesByDepartmentIdNative(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }
        List<Employee> employees = employeeRepository.findEmployeesByDepartmentIdNative(departmentId);
        return employeeMapper.toDtoList(employees);
    }
}
