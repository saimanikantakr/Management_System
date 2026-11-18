package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.request.EmployeeRequestDTO;
import com.example.employeemanagement.dto.response.EmployeeResponseDTO;
import com.example.employeemanagement.dto.response.PageResponseDTO;
import com.example.employeemanagement.exception.DuplicateResourceException;
import com.example.employeemanagement.exception.GlobalExceptionHandler;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private EmployeeResponseDTO sampleEmployee;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        sampleEmployee = new EmployeeResponseDTO(1L, "Rahul Kumar", "rahul@example.com", 65000.0, 1L, "Engineering");
    }

    @Test
    @DisplayName("POST /api/employees returns 201 Created and EmployeeResponseDTO")
    void testCreateEmployee_Success() throws Exception {
        String requestJson = """
                {
                  "name": "Rahul Kumar",
                  "email": "rahul@example.com",
                  "salary": 65000.0,
                  "departmentId": 1
                }
                """;

        when(employeeService.createEmployee(any(EmployeeRequestDTO.class))).thenReturn(sampleEmployee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Rahul Kumar"))
                .andExpect(jsonPath("$.email").value("rahul@example.com"))
                .andExpect(jsonPath("$.salary").value(65000.0))
                .andExpect(jsonPath("$.departmentId").value(1L))
                .andExpect(jsonPath("$.departmentName").value("Engineering"));
    }

    @Test
    @DisplayName("POST /api/employees with invalid payload returns 400 Bad Request")
    void testCreateEmployee_ValidationError() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "email": "invalid-email",
                  "salary": -500.0,
                  "departmentId": null
                }
                """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.salary").exists())
                .andExpect(jsonPath("$.validationErrors.departmentId").exists());
    }

    @Test
    @DisplayName("POST /api/employees with duplicate email returns 409 Conflict")
    void testCreateEmployee_DuplicateEmail() throws Exception {
        String requestJson = """
                {
                  "name": "Rahul Kumar",
                  "email": "duplicate@example.com",
                  "salary": 65000.0,
                  "departmentId": 1
                }
                """;

        when(employeeService.createEmployee(any(EmployeeRequestDTO.class)))
                .thenThrow(new DuplicateResourceException("Employee", "email", "duplicate@example.com"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Employee already exists with email: 'duplicate@example.com'"));
    }

    @Test
    @DisplayName("GET /api/employees returns 200 OK and list of employees")
    void testGetAllEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(sampleEmployee));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Rahul Kumar"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} returns 200 OK when found")
    void testGetEmployeeById_Success() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(sampleEmployee);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Rahul Kumar"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} returns 404 Not Found when missing")
    void testGetEmployeeById_NotFound() throws Exception {
        when(employeeService.getEmployeeById(99L))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 99L));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee not found with id: '99'"));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} returns 200 OK when updated")
    void testUpdateEmployee_Success() throws Exception {
        String requestJson = """
                {
                  "name": "Rahul Kumar",
                  "email": "rahul@example.com",
                  "salary": 70000.0,
                  "departmentId": 1
                }
                """;
        EmployeeResponseDTO updated = new EmployeeResponseDTO(1L, "Rahul Kumar", "rahul@example.com", 70000.0, 1L, "Engineering");

        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequestDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salary").value(70000.0));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} returns 204 No Content")
    void testDeleteEmployee() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @DisplayName("GET /api/employees/paginated returns 200 OK with page metadata")
    void testGetEmployeesPaginated() throws Exception {
        PageResponseDTO<EmployeeResponseDTO> pageResponse = new PageResponseDTO<>(
                0, 5, 1, 1, true, List.of(sampleEmployee)
        );

        when(employeeService.getEmployeesPaginated(0, 5, "name", "asc")).thenReturn(pageResponse);

        mockMvc.perform(get("/api/employees/paginated")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].name").value("Rahul Kumar"));
    }

    @Test
    @DisplayName("GET /api/employees/search/name returns matching employees")
    void testSearchByName() throws Exception {
        when(employeeService.searchEmployeesByName("Rahul")).thenReturn(List.of(sampleEmployee));

        mockMvc.perform(get("/api/employees/search/name").param("keyword", "Rahul"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Rahul Kumar"));
    }

    @Test
    @DisplayName("GET /api/employees/search/salary returns employees with salary above threshold")
    void testGetBySalaryGreaterThan() throws Exception {
        when(employeeService.getEmployeesBySalaryGreaterThan(50000.0)).thenReturn(List.of(sampleEmployee));

        mockMvc.perform(get("/api/employees/search/salary").param("min", "50000.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].salary").value(65000.0));
    }

    @Test
    @DisplayName("GET /api/employees/search/salary/sorted returns JPQL sorted results")
    void testGetBySalaryGreaterThanOrderBySalaryDesc() throws Exception {
        when(employeeService.getEmployeesWithSalaryGreaterThanOrderBySalaryDesc(50000.0)).thenReturn(List.of(sampleEmployee));

        mockMvc.perform(get("/api/employees/search/salary/sorted").param("min", "50000.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].salary").value(65000.0));
    }

    @Test
    @DisplayName("GET /api/employees/search/salary-range returns employees in range")
    void testGetBySalaryBetween() throws Exception {
        when(employeeService.getEmployeesBySalaryBetween(50000.0, 70000.0)).thenReturn(List.of(sampleEmployee));

        mockMvc.perform(get("/api/employees/search/salary-range")
                        .param("min", "50000.0")
                        .param("max", "70000.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Rahul Kumar"));
    }
}
