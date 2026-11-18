package com.example.employeemanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class EmployeeRequestDTO {

    @NotBlank(message = "Employee name must not be blank")
    @Size(min = 2, max = 100, message = "Employee name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    @Size(max = 120, message = "Email cannot exceed 120 characters")
    private String email;

    @NotNull(message = "Salary must not be null")
    @Positive(message = "Salary must be greater than zero")
    private Double salary;

    @NotNull(message = "Department ID must not be null")
    private Long departmentId;

    public EmployeeRequestDTO() {
    }

    public EmployeeRequestDTO(String name, String email, Double salary, Long departmentId) {
        this.name = name;
        this.email = email;
        this.salary = salary;
        this.departmentId = departmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
