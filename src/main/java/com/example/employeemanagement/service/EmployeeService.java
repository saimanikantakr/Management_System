package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.request.EmployeeRequestDTO;
import com.example.employeemanagement.dto.response.EmployeeResponseDTO;
import com.example.employeemanagement.dto.response.PageResponseDTO;

import java.util.List;

public interface EmployeeService {

    EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO);

    List<EmployeeResponseDTO> getAllEmployees();

    EmployeeResponseDTO getEmployeeById(Long id);

    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO);

    void deleteEmployee(Long id);

    PageResponseDTO<EmployeeResponseDTO> getEmployeesPaginated(int page, int size, String sortBy, String direction);

    List<EmployeeResponseDTO> getEmployeesBySalaryGreaterThan(Double salary);

    List<EmployeeResponseDTO> searchEmployeesByName(String name);

    List<EmployeeResponseDTO> getEmployeesBySalaryBetween(Double minSalary, Double maxSalary);

    List<EmployeeResponseDTO> getEmployeesByDepartmentId(Long departmentId);

    List<EmployeeResponseDTO> getEmployeesWithSalaryGreaterThanOrderBySalaryDesc(Double salary);

    List<EmployeeResponseDTO> getEmployeesByDepartmentIdNative(Long departmentId);
}
