package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.request.DepartmentRequestDTO;
import com.example.employeemanagement.dto.response.DepartmentResponseDTO;
import com.example.employeemanagement.dto.response.EmployeeResponseDTO;

import java.util.List;

public interface DepartmentService {

    DepartmentResponseDTO createDepartment(DepartmentRequestDTO requestDTO);

    List<DepartmentResponseDTO> getAllDepartments();

    DepartmentResponseDTO getDepartmentById(Long id);

    DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO requestDTO);

    void deleteDepartment(Long id);

    EmployeeResponseDTO assignEmployeeToDepartment(Long departmentId, Long employeeId);

    List<EmployeeResponseDTO> getEmployeesByDepartmentId(Long departmentId);
}
