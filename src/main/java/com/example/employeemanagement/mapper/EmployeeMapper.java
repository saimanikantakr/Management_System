package com.example.employeemanagement.mapper;

import com.example.employeemanagement.dto.request.EmployeeRequestDTO;
import com.example.employeemanagement.dto.response.EmployeeResponseDTO;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmployeeMapper {

    
    public Employee toEntity(EmployeeRequestDTO dto, Department department) {
        if (dto == null) {
            return null;
        }
        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setSalary(dto.getSalary());
        employee.setDepartment(department);
        return employee;
    }

    
    public void updateEntityFromDto(Employee employee, EmployeeRequestDTO dto, Department department) {
        if (employee == null || dto == null) {
            return;
        }
        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setSalary(dto.getSalary());
        employee.setDepartment(department);
    }

    
    public EmployeeResponseDTO toDto(Employee entity) {
        if (entity == null) {
            return null;
        }
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setSalary(entity.getSalary());

        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
        }
        return dto;
    }

    
    public List<EmployeeResponseDTO> toDtoList(List<Employee> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
