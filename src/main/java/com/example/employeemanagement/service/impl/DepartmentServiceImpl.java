package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.dto.request.DepartmentRequestDTO;
import com.example.employeemanagement.dto.response.DepartmentResponseDTO;
import com.example.employeemanagement.dto.response.EmployeeResponseDTO;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.mapper.DepartmentMapper;
import com.example.employeemanagement.mapper.EmployeeMapper;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;
    private final EmployeeMapper employeeMapper;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 EmployeeRepository employeeRepository,
                                 DepartmentMapper departmentMapper,
                                 EmployeeMapper employeeMapper) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.departmentMapper = departmentMapper;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO requestDTO) {
        Department department = departmentMapper.toEntity(requestDTO);
        Department savedDepartment = departmentRepository.save(department);
        return departmentMapper.toDto(savedDepartment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        return departmentMapper.toDtoList(departments);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findByIdWithEmployees(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return departmentMapper.toDtoWithEmployees(department);
    }

    @Override
    public DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO requestDTO) {
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        departmentMapper.updateEntityFromDto(existingDepartment, requestDTO);
        Department updatedDepartment = departmentRepository.save(existingDepartment);
        return departmentMapper.toDto(updatedDepartment);
    }

    @Override
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        if (department.getEmployees() != null) {
            for (Employee employee : department.getEmployees()) {
                employee.setDepartment(null);
            }
        }
        departmentRepository.delete(department);
    }

    @Override
    public EmployeeResponseDTO assignEmployeeToDepartment(Long departmentId, Long employeeId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        if (employee.getDepartment() != null && !employee.getDepartment().getId().equals(departmentId)) {
            employee.getDepartment().removeEmployee(employee);
        }

        department.addEmployee(employee);
        Employee updatedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDto(updatedEmployee);
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
}
