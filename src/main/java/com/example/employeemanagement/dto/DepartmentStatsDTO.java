package com.example.employeemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatsDTO {
    private String department;
    private Long totalEmployees;
    private Long activeEmployees;
    private Double averageSalary;
    private Double totalSalaryBudget;
}