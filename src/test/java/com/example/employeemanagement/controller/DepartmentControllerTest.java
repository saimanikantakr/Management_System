package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.request.DepartmentRequestDTO;
import com.example.employeemanagement.dto.response.DepartmentResponseDTO;
import com.example.employeemanagement.dto.response.EmployeeResponseDTO;
import com.example.employeemanagement.exception.GlobalExceptionHandler;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.service.DepartmentService;
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
class DepartmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    private DepartmentResponseDTO sampleDepartment;
    private EmployeeResponseDTO sampleEmployee;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(departmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        sampleEmployee = new EmployeeResponseDTO(1L, "Rahul Kumar", "rahul@example.com", 65000.0, 1L, "Engineering");
        sampleDepartment = new DepartmentResponseDTO(1L, "Engineering", "Bangalore", List.of(sampleEmployee));
    }

    @Test
    @DisplayName("POST /api/departments returns 201 Created and DepartmentResponseDTO")
    void testCreateDepartment_Success() throws Exception {
        String requestJson = """
                {
                  "name": "Engineering",
                  "location": "Bangalore"
                }
                """;

        when(departmentService.createDepartment(any(DepartmentRequestDTO.class))).thenReturn(sampleDepartment);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Engineering"))
                .andExpect(jsonPath("$.location").value("Bangalore"));
    }

    @Test
    @DisplayName("POST /api/departments with blank fields returns 400 Bad Request")
    void testCreateDepartment_ValidationError() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "location": ""
                }
                """;

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.location").exists());
    }

    @Test
    @DisplayName("GET /api/departments returns 200 OK and list of departments")
    void testGetAllDepartments() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(List.of(sampleDepartment));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Engineering"));
    }

    @Test
    @DisplayName("GET /api/departments/{id} returns 200 OK with associated employees")
    void testGetDepartmentById_Success() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(sampleDepartment);

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Engineering"))
                .andExpect(jsonPath("$.employees").isArray())
                .andExpect(jsonPath("$.employees[0].name").value("Rahul Kumar"));
    }

    @Test
    @DisplayName("GET /api/departments/{id} returns 404 Not Found when missing")
    void testGetDepartmentById_NotFound() throws Exception {
        when(departmentService.getDepartmentById(99L))
                .thenThrow(new ResourceNotFoundException("Department", "id", 99L));

        mockMvc.perform(get("/api/departments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Department not found with id: '99'"));
    }

    @Test
    @DisplayName("PUT /api/departments/{id} returns 200 OK when updated")
    void testUpdateDepartment_Success() throws Exception {
        String requestJson = """
                {
                  "name": "Tech",
                  "location": "Hyderabad"
                }
                """;
        DepartmentResponseDTO updated = new DepartmentResponseDTO(1L, "Tech", "Hyderabad");

        when(departmentService.updateDepartment(eq(1L), any(DepartmentRequestDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tech"))
                .andExpect(jsonPath("$.location").value("Hyderabad"));
    }

    @Test
    @DisplayName("DELETE /api/departments/{id} returns 204 No Content")
    void testDeleteDepartment() throws Exception {
        doNothing().when(departmentService).deleteDepartment(1L);

        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isNoContent());

        verify(departmentService, times(1)).deleteDepartment(1L);
    }

    @Test
    @DisplayName("POST /api/departments/{deptId}/employees/{empId} assigns employee successfully")
    void testAssignEmployee_PathVariable() throws Exception {
        when(departmentService.assignEmployeeToDepartment(1L, 2L)).thenReturn(sampleEmployee);

        mockMvc.perform(post("/api/departments/1/employees/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.departmentName").value("Engineering"));
    }

    @Test
    @DisplayName("POST /api/departments/{deptId}/employees with body assigns employee successfully")
    void testAssignEmployee_RequestBody() throws Exception {
        String assignJson = """
                {
                  "employeeId": 2
                }
                """;
        when(departmentService.assignEmployeeToDepartment(1L, 2L)).thenReturn(sampleEmployee);

        mockMvc.perform(post("/api/departments/1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /api/departments/{deptId}/employees returns employees for department")
    void testGetDepartmentEmployees_Success() throws Exception {
        when(departmentService.getEmployeesByDepartmentId(1L)).thenReturn(List.of(sampleEmployee));

        mockMvc.perform(get("/api/departments/1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Rahul Kumar"));
    }

    @Test
    @DisplayName("GET /api/departments/{deptId}/employees returns 404 when department not found")
    void testGetDepartmentEmployees_NotFound() throws Exception {
        when(departmentService.getEmployeesByDepartmentId(99L))
                .thenThrow(new ResourceNotFoundException("Department", "id", 99L));

        mockMvc.perform(get("/api/departments/99/employees"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
