package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);
    
    List<Employee> findByDepartment(String department);
    
    List<Employee> findByFirstNameContainingOrLastNameContaining(String firstName, String lastName);
    
    List<Employee> findByActive(boolean active);
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department = :department")
    Long countEmployeesByDepartment(@Param("department") String department);
    
    @Query("SELECT e FROM Employee e WHERE e.department = :department AND e.active = true")
    List<Employee> findActiveEmployeesByDepartment(@Param("department") String department);
    
    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department = :department")
    Double getAverageSalaryByDepartment(@Param("department") String department);
}