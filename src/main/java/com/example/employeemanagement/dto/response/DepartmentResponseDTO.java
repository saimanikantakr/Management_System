package com.example.employeemanagement.dto.response;

import java.util.ArrayList;
import java.util.List;

public class DepartmentResponseDTO {

    private Long id;
    private String name;
    private String location;
    private List<EmployeeResponseDTO> employees = new ArrayList<>();

    public DepartmentResponseDTO() {
    }

    public DepartmentResponseDTO(Long id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.employees = new ArrayList<>();
    }

    public DepartmentResponseDTO(Long id, String name, String location, List<EmployeeResponseDTO> employees) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.employees = employees != null ? employees : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<EmployeeResponseDTO> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeResponseDTO> employees) {
        this.employees = employees != null ? employees : new ArrayList<>();
    }
}
