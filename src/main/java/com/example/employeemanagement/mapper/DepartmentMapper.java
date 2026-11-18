package com.example.employeemanagement.mapper;

import com.example.employeemanagement.dto.request.DepartmentRequestDTO;
import com.example.employeemanagement.dto.response.DepartmentResponseDTO;
import com.example.employeemanagement.dto.response.EmployeeResponseDTO;
import com.example.employeemanagement.entity.Department;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DepartmentMapper {

    private final EmployeeMapper employeeMapper;

    public DepartmentMapper(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    
    public Department toEntity(DepartmentRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Department department = new Department();
        department.setName(dto.getName());
        department.setLocation(dto.getLocation());
        return department;
    }

    
    public void updateEntityFromDto(Department department, DepartmentRequestDTO dto) {
        if (department == null || dto == null) {
            return;
        }
        department.setName(dto.getName());
        department.setLocation(dto.getLocation());
    }

    
    public DepartmentResponseDTO toDto(Department entity) {
        if (entity == null) {
            return null;
        }
        DepartmentResponseDTO dto = new DepartmentResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setLocation(entity.getLocation());
        dto.setEmployees(Collections.emptyList());
        return dto;
    }

    
    public DepartmentResponseDTO toDtoWithEmployees(Department entity) {
        if (entity == null) {
            return null;
        }
        DepartmentResponseDTO dto = new DepartmentResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setLocation(entity.getLocation());

        if (entity.getEmployees() != null && !entity.getEmployees().isEmpty()) {
            List<EmployeeResponseDTO> employeeDTOs = employeeMapper.toDtoList(entity.getEmployees());
            dto.setEmployees(employeeDTOs);
        } else {
            dto.setEmployees(Collections.emptyList());
        }
        return dto;
    }

    
    public List<DepartmentResponseDTO> toDtoList(List<Department> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
