package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeDTO;
import com.example.employeemanagement.dto.DepartmentStatsDTO;
import java.util.List;
import java.util.Map;

public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);
    EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO);
    EmployeeDTO getEmployeeById(Long id);
    List<EmployeeDTO> getAllEmployees();
    void deleteEmployee(Long id);
    EmployeeDTO deactivateEmployee(Long id);
    EmployeeDTO activateEmployee(Long id);
    List<EmployeeDTO> getEmployeesByDepartment(String department);
    List<EmployeeDTO> searchEmployees(String keyword);
    Map<String, Long> getEmployeeCountByDepartment();
    DepartmentStatsDTO getDepartmentStatistics(String department);
    List<EmployeeDTO> getActiveEmployees();
    List<EmployeeDTO> getInactiveEmployees();
}