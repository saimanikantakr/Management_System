package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    
    boolean existsByEmail(String email);

    
    boolean existsByEmailAndIdNot(String email, Long id);

    
    List<Employee> findBySalaryGreaterThan(Double salary);

    
    List<Employee> findByNameContainingIgnoreCase(String name);

    
    List<Employee> findBySalaryBetween(Double minSalary, Double maxSalary);

    
    List<Employee> findByDepartmentId(Long departmentId);

    
    @Query("SELECT e FROM Employee e WHERE e.salary > :salary ORDER BY e.salary DESC")
    List<Employee> findEmployeesWithSalaryGreaterThanOrderBySalaryDesc(@Param("salary") Double salary);

    
    @Query(value = "SELECT * FROM employees WHERE department_id = :departmentId", nativeQuery = true)
    List<Employee> findEmployeesByDepartmentIdNative(@Param("departmentId") Long departmentId);
}
