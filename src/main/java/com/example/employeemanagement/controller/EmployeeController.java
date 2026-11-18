package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.request.EmployeeRequestDTO;
import com.example.employeemanagement.dto.response.EmployeeResponseDTO;
import com.example.employeemanagement.dto.response.PageResponseDTO;
import com.example.employeemanagement.service.EmployeeService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO requestDTO) {
        EmployeeResponseDTO created = employeeService.createEmployee(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO requestDTO) {
        EmployeeResponseDTO updated = employeeService.updateEmployee(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    
    @GetMapping("/paginated")
    public ResponseEntity<PageResponseDTO<EmployeeResponseDTO>> getEmployeesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        PageResponseDTO<EmployeeResponseDTO> response = employeeService.getEmployeesPaginated(page, size, sortBy, direction);
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/search/name")
    public ResponseEntity<List<EmployeeResponseDTO>> searchByName(@RequestParam String keyword) {
        List<EmployeeResponseDTO> employees = employeeService.searchEmployeesByName(keyword);
        return ResponseEntity.ok(employees);
    }

    
    @GetMapping("/search/salary")
    public ResponseEntity<List<EmployeeResponseDTO>> getBySalaryGreaterThan(@RequestParam Double min) {
        List<EmployeeResponseDTO> employees = employeeService.getEmployeesBySalaryGreaterThan(min);
        return ResponseEntity.ok(employees);
    }

    
    @GetMapping("/search/salary/sorted")
    public ResponseEntity<List<EmployeeResponseDTO>> getBySalaryGreaterThanOrderBySalaryDesc(@RequestParam Double min) {
        List<EmployeeResponseDTO> employees = employeeService.getEmployeesWithSalaryGreaterThanOrderBySalaryDesc(min);
        return ResponseEntity.ok(employees);
    }

    
    @GetMapping("/search/salary-range")
    public ResponseEntity<List<EmployeeResponseDTO>> getBySalaryBetween(
            @RequestParam Double min,
            @RequestParam Double max) {
        List<EmployeeResponseDTO> employees = employeeService.getEmployeesBySalaryBetween(min, max);
        return ResponseEntity.ok(employees);
    }

    
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeResponseDTO>> getByDepartmentId(@PathVariable Long departmentId) {
        List<EmployeeResponseDTO> employees = employeeService.getEmployeesByDepartmentId(departmentId);
        return ResponseEntity.ok(employees);
    }

    
    @GetMapping("/department/{departmentId}/native")
    public ResponseEntity<List<EmployeeResponseDTO>> getByDepartmentIdNative(@PathVariable Long departmentId) {
        List<EmployeeResponseDTO> employees = employeeService.getEmployeesByDepartmentIdNative(departmentId);
        return ResponseEntity.ok(employees);
    }
}
