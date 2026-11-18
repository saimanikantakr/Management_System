package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Department engineering;
    private Department hr;
    private Employee emp1;
    private Employee emp2;
    private Employee emp3;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        engineering = new Department("Engineering", "Bangalore");
        hr = new Department("Human Resources", "Mumbai");

        departmentRepository.save(engineering);
        departmentRepository.save(hr);

        emp1 = new Employee("Rahul Kumar", "rahul@example.com", 75000.0, engineering);
        emp2 = new Employee("Priya Sharma", "priya@example.com", 90000.0, engineering);
        emp3 = new Employee("Amit Patel", "amit@example.com", 50000.0, hr);

        employeeRepository.save(emp1);
        employeeRepository.save(emp2);
        employeeRepository.save(emp3);
    }

    @Test
    @DisplayName("Derived Query: findBySalaryGreaterThan returns matching employees")
    void testFindBySalaryGreaterThan() {
        List<Employee> results = employeeRepository.findBySalaryGreaterThan(60000.0);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Employee::getName).containsExactlyInAnyOrder("Rahul Kumar", "Priya Sharma");
    }

    @Test
    @DisplayName("Derived Query: findByNameContainingIgnoreCase returns matching employees")
    void testFindByNameContainingIgnoreCase() {
        List<Employee> results = employeeRepository.findByNameContainingIgnoreCase("kumar");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Rahul Kumar");
    }

    @Test
    @DisplayName("Derived Query: findBySalaryBetween returns employees in salary range")
    void testFindBySalaryBetween() {
        List<Employee> results = employeeRepository.findBySalaryBetween(60000.0, 95000.0);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Employee::getName).containsExactlyInAnyOrder("Rahul Kumar", "Priya Sharma");
    }

    @Test
    @DisplayName("Derived Query: findByDepartmentId returns employees in department")
    void testFindByDepartmentId() {
        List<Employee> results = employeeRepository.findByDepartmentId(engineering.getId());

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Employee::getName).containsExactlyInAnyOrder("Rahul Kumar", "Priya Sharma");
    }

    @Test
    @DisplayName("JPQL Query: findEmployeesWithSalaryGreaterThanOrderBySalaryDesc sorts descending")
    void testFindEmployeesWithSalaryGreaterThanOrderBySalaryDesc() {
        List<Employee> results = employeeRepository.findEmployeesWithSalaryGreaterThanOrderBySalaryDesc(40000.0);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getSalary()).isEqualTo(90000.0);
        assertThat(results.get(1).getSalary()).isEqualTo(75000.0);
        assertThat(results.get(2).getSalary()).isEqualTo(50000.0);
    }

    @Test
    @DisplayName("Native SQL Query: findEmployeesByDepartmentIdNative retrieves correct employees")
    void testFindEmployeesByDepartmentIdNative() {
        List<Employee> results = employeeRepository.findEmployeesByDepartmentIdNative(hr.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Amit Patel");
    }

    @Test
    @DisplayName("Check existsByEmail returns true for existing email")
    void testExistsByEmail() {
        assertThat(employeeRepository.existsByEmail("rahul@example.com")).isTrue();
        assertThat(employeeRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("Check existsByEmailAndIdNot distinguishes duplicate emails")
    void testExistsByEmailAndIdNot() {
        assertThat(employeeRepository.existsByEmailAndIdNot("rahul@example.com", emp1.getId())).isFalse();
        assertThat(employeeRepository.existsByEmailAndIdNot("rahul@example.com", emp2.getId())).isTrue();
    }
}
