package com.example.employeemanagement.service;

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
import com.example.employeemanagement.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Spy
    private EmployeeMapper employeeMapper = new EmployeeMapper();

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Department department;
    private Employee employee;
    private EmployeeRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        department = new Department(1L, "Engineering", "Bangalore");
        employee = new Employee(1L, "Rahul Kumar", "rahul@example.com", 65000.0, department);
        requestDTO = new EmployeeRequestDTO("Rahul Kumar", "rahul@example.com", 65000.0, 1L);
    }

    @Test
    @DisplayName("createEmployee successfully creates employee")
    void testCreateEmployee_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.existsByEmail("rahul@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setId(1L);
            return emp;
        });

        EmployeeResponseDTO response = employeeService.createEmployee(requestDTO);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Rahul Kumar");
        assertThat(response.getEmail()).isEqualTo("rahul@example.com");
        assertThat(response.getSalary()).isEqualTo(65000.0);
        assertThat(response.getDepartmentId()).isEqualTo(1L);
        assertThat(response.getDepartmentName()).isEqualTo("Engineering");

        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("createEmployee throws ResourceNotFoundException if department does not exist")
    void testCreateEmployee_DepartmentNotFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createEmployee(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found");

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("createEmployee throws DuplicateResourceException if email is taken")
    void testCreateEmployee_DuplicateEmail() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.existsByEmail("rahul@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Employee already exists with email");

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("getEmployeeById returns response when employee found")
    void testGetEmployeeById_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeResponseDTO response = employeeService.getEmployeeById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Rahul Kumar");
    }

    @Test
    @DisplayName("getEmployeeById throws ResourceNotFoundException when not found")
    void testGetEmployeeById_NotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: '99'");
    }

    @Test
    @DisplayName("updateEmployee updates existing employee successfully")
    void testUpdateEmployee_Success() {
        EmployeeRequestDTO updateDTO = new EmployeeRequestDTO("Rahul Sharma", "rahul.sharma@example.com", 70000.0, 1L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndIdNot("rahul.sharma@example.com", 1L)).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponseDTO response = employeeService.updateEmployee(1L, updateDTO);

        assertThat(response.getName()).isEqualTo("Rahul Sharma");
        assertThat(response.getEmail()).isEqualTo("rahul.sharma@example.com");
        assertThat(response.getSalary()).isEqualTo(70000.0);
    }

    @Test
    @DisplayName("updateEmployee throws DuplicateResourceException if email is used by another employee")
    void testUpdateEmployee_DuplicateEmail() {
        EmployeeRequestDTO updateDTO = new EmployeeRequestDTO("Rahul Kumar", "taken@example.com", 70000.0, 1L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, updateDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Employee already exists with email");
    }

    @Test
    @DisplayName("deleteEmployee deletes employee when exists")
    void testDeleteEmployee_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).delete(employee);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository, times(1)).delete(employee);
    }

    @Test
    @DisplayName("deleteEmployee throws ResourceNotFoundException when employee not found")
    void testDeleteEmployee_NotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(employeeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getEmployeesPaginated returns page response with correct metadata")
    void testGetEmployeesPaginated_Success() {
        Page<Employee> page = new PageImpl<>(List.of(employee));
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponseDTO<EmployeeResponseDTO> result = employeeService.getEmployeesPaginated(0, 5, "name", "asc");

        assertThat(result).isNotNull();
        assertThat(result.getCurrentPage()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Rahul Kumar");
    }

    @Test
    @DisplayName("getEmployeesPaginated throws IllegalArgumentException for negative page")
    void testGetEmployeesPaginated_InvalidPage() {
        assertThatThrownBy(() -> employeeService.getEmployeesPaginated(-1, 5, "name", "asc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page index must not be less than zero");
    }

    @Test
    @DisplayName("getEmployeesPaginated throws IllegalArgumentException for invalid sort field")
    void testGetEmployeesPaginated_InvalidSort() {
        assertThatThrownBy(() -> employeeService.getEmployeesPaginated(0, 5, "invalidProperty", "asc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sort property");
    }

    @Test
    @DisplayName("getEmployeesBySalaryBetween throws IllegalArgumentException when min > max")
    void testGetEmployeesBySalaryBetween_InvalidRange() {
        assertThatThrownBy(() -> employeeService.getEmployeesBySalaryBetween(80000.0, 50000.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Minimum salary");
    }
}
