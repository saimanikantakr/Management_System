package com.example.employeemanagement.service;

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
import com.example.employeemanagement.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Spy
    private EmployeeMapper employeeMapper = new EmployeeMapper();

    @Spy
    private DepartmentMapper departmentMapper = new DepartmentMapper(new EmployeeMapper());

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;
    private Employee employee;

    @BeforeEach
    void setUp() {
        department = new Department(1L, "Engineering", "Bangalore");
        employee = new Employee(1L, "Rahul Kumar", "rahul@example.com", 65000.0, null);
    }

    @Test
    @DisplayName("createDepartment persists department successfully")
    void testCreateDepartment_Success() {
        DepartmentRequestDTO requestDTO = new DepartmentRequestDTO("Finance", "Delhi");
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> {
            Department dept = inv.getArgument(0);
            dept.setId(2L);
            return dept;
        });

        DepartmentResponseDTO response = departmentService.createDepartment(requestDTO);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Finance");
        assertThat(response.getLocation()).isEqualTo("Delhi");
    }

    @Test
    @DisplayName("getDepartmentById returns department with its associated employees")
    void testGetDepartmentById_Success() {
        department.addEmployee(employee);
        when(departmentRepository.findByIdWithEmployees(1L)).thenReturn(Optional.of(department));

        DepartmentResponseDTO response = departmentService.getDepartmentById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmployees()).hasSize(1);
        assertThat(response.getEmployees().get(0).getName()).isEqualTo("Rahul Kumar");
    }

    @Test
    @DisplayName("getDepartmentById throws ResourceNotFoundException when department not found")
    void testGetDepartmentById_NotFound() {
        when(departmentRepository.findByIdWithEmployees(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found with id: '99'");
    }

    @Test
    @DisplayName("assignEmployeeToDepartment assigns employee and updates relationship")
    void testAssignEmployeeToDepartment_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponseDTO result = departmentService.assignEmployeeToDepartment(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getDepartmentId()).isEqualTo(1L);
        assertThat(result.getDepartmentName()).isEqualTo("Engineering");
        assertThat(employee.getDepartment()).isEqualTo(department);
    }

    @Test
    @DisplayName("assignEmployeeToDepartment throws ResourceNotFoundException when department not found")
    void testAssignEmployeeToDepartment_DepartmentNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.assignEmployeeToDepartment(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found with id: '99'");
    }

    @Test
    @DisplayName("assignEmployeeToDepartment throws ResourceNotFoundException when employee not found")
    void testAssignEmployeeToDepartment_EmployeeNotFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.assignEmployeeToDepartment(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: '99'");
    }

    @Test
    @DisplayName("getEmployeesByDepartmentId returns all employees in department")
    void testGetEmployeesByDepartmentId_Success() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(employeeRepository.findByDepartmentId(1L)).thenReturn(List.of(employee));

        List<EmployeeResponseDTO> results = departmentService.getEmployeesByDepartmentId(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Rahul Kumar");
    }

    @Test
    @DisplayName("getEmployeesByDepartmentId throws ResourceNotFoundException if department does not exist")
    void testGetEmployeesByDepartmentId_NotFound() {
        when(departmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> departmentService.getEmployeesByDepartmentId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found with id: '99'");
    }
}
