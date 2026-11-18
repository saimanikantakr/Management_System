package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.request.AssignEmployeeDTO;
import com.example.employeemanagement.dto.request.DepartmentRequestDTO;
import com.example.employeemanagement.dto.response.DepartmentResponseDTO;
import com.example.employeemanagement.dto.response.EmployeeResponseDTO;
import com.example.employeemanagement.service.DepartmentService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    
    @PostMapping
    public ResponseEntity<DepartmentResponseDTO> createDepartment(@Valid @RequestBody DepartmentRequestDTO requestDTO) {
        DepartmentResponseDTO created = departmentService.createDepartment(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    
    @GetMapping
    public ResponseEntity<List<DepartmentResponseDTO>> getAllDepartments() {
        List<DepartmentResponseDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentById(@PathVariable Long id) {
        DepartmentResponseDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequestDTO requestDTO) {
        DepartmentResponseDTO updated = departmentService.updateDepartment(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    
    @PostMapping("/{departmentId}/employees/{employeeId}")
    public ResponseEntity<EmployeeResponseDTO> assignEmployee(
            @PathVariable Long departmentId,
            @PathVariable Long employeeId) {
        EmployeeResponseDTO updated = departmentService.assignEmployeeToDepartment(departmentId, employeeId);
        return ResponseEntity.ok(updated);
    }

    
    @PostMapping("/{departmentId}/employees")
    public ResponseEntity<EmployeeResponseDTO> assignEmployeeWithBody(
            @PathVariable Long departmentId,
            @Valid @RequestBody AssignEmployeeDTO assignDTO) {
        EmployeeResponseDTO updated = departmentService.assignEmployeeToDepartment(departmentId, assignDTO.getEmployeeId());
        return ResponseEntity.ok(updated);
    }

    
    @GetMapping("/{departmentId}/employees")
    public ResponseEntity<List<EmployeeResponseDTO>> getDepartmentEmployees(@PathVariable Long departmentId) {
        List<EmployeeResponseDTO> employees = departmentService.getEmployeesByDepartmentId(departmentId);
        return ResponseEntity.ok(employees);
    }
}
