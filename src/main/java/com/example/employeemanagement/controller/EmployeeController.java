package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeDTO;
import com.example.employeemanagement.dto.DepartmentStatsDTO;
import com.example.employeemanagement.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO createdEmployee = employeeService.createEmployee(employeeDTO);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.ok(updatedEmployee);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }
    
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<EmployeeDTO> deactivateEmployee(@PathVariable Long id) {
        EmployeeDTO employee = employeeService.deactivateEmployee(id);
        return ResponseEntity.ok(employee);
    }
    
    @PatchMapping("/{id}/activate")
    public ResponseEntity<EmployeeDTO> activateEmployee(@PathVariable Long id) {
        EmployeeDTO employee = employeeService.activateEmployee(id);
        return ResponseEntity.ok(employee);
    }
    
    @GetMapping("/department/{department}")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDepartment(@PathVariable String department) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByDepartment(department);
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDTO>> searchEmployees(@RequestParam String keyword) {
        List<EmployeeDTO> employees = employeeService.searchEmployees(keyword);
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/stats/department")
    public ResponseEntity<Map<String, Long>> getEmployeeCountByDepartment() {
        Map<String, Long> stats = employeeService.getEmployeeCountByDepartment();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats/department/{department}")
    public ResponseEntity<DepartmentStatsDTO> getDepartmentStatistics(@PathVariable String department) {
        DepartmentStatsDTO stats = employeeService.getDepartmentStatistics(department);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<EmployeeDTO>> getActiveEmployees() {
        List<EmployeeDTO> employees = employeeService.getActiveEmployees();
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/inactive")
    public ResponseEntity<List<EmployeeDTO>> getInactiveEmployees() {
        List<EmployeeDTO> employees = employeeService.getInactiveEmployees();
        return ResponseEntity.ok(employees);
    }
}