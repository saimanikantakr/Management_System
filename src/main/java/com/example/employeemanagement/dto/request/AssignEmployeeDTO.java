package com.example.employeemanagement.dto.request;

import jakarta.validation.constraints.NotNull;

public class AssignEmployeeDTO {

    @NotNull(message = "Employee ID must not be null")
    private Long employeeId;

    private Long departmentId;

    public AssignEmployeeDTO() {
    }

    public AssignEmployeeDTO(Long employeeId) {
        this.employeeId = employeeId;
    }

    public AssignEmployeeDTO(Long employeeId, Long departmentId) {
        this.employeeId = employeeId;
        this.departmentId = departmentId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
