package com.upc.webworksbackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.upc.webworksbackend.dto.EmploymentDto;
import com.upc.webworksbackend.dtoaux.EmploymentSummaryDto;
import com.upc.webworksbackend.exception.ForbiddenException;
import com.upc.webworksbackend.model.CompanyModel;
import com.upc.webworksbackend.security.CurrentUser;
import com.upc.webworksbackend.serviceinterface.EmploymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/employment")
public class EmploymentController {
    private final EmploymentService employmentService;
    private final CurrentUser currentUser;

    public EmploymentController(EmploymentService employmentService, CurrentUser currentUser) {
        this.employmentService = employmentService;
        this.currentUser = currentUser;
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/getEmploymentsByCompany/{idCompany}")
    public ResponseEntity<List<EmploymentDto>> getEmploymentsByCompany(@PathVariable Integer idCompany){
        return new ResponseEntity<>(employmentService.getEmploymentsByCompany(idCompany), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PostMapping("/addEmployment")
    public ResponseEntity<Boolean> addEmployment(@RequestBody EmploymentDto employmentDto){
        CompanyModel company = currentUser.getCurrentCompany();
        if (employmentDto.getId_company() != null && !employmentDto.getId_company().equals(company.getId())) {
            throw new ForbiddenException("No puede registrar ofertas para otra empresa.");
        }
        employmentDto.setId_company(company.getId());
        return new ResponseEntity<>(employmentService.addEmployment(employmentDto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/getEmploymentById/{id}")
    public ResponseEntity<EmploymentDto> getEmploymentById(@PathVariable Integer id){
        return new ResponseEntity<>(employmentService.getEmploymentById(id), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PutMapping("/updateEmployment")
    public ResponseEntity<Boolean> updateEmployment(@RequestBody EmploymentDto employmentDto){
        CompanyModel company = currentUser.getCurrentCompany();
        EmploymentDto existing = employmentService.getEmploymentById(employmentDto.getId());
        if (!company.getId().equals(existing.getId_company())) {
            throw new ForbiddenException("No tiene permisos para modificar ofertas de otra empresa.");
        }
        return new ResponseEntity<>(employmentService.updateEmployment(employmentDto), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @DeleteMapping("/deleteEmployment/{id}")
    public ResponseEntity<Boolean> deleteEmployment(@PathVariable Integer id){
        CompanyModel company = currentUser.getCurrentCompany();
        EmploymentDto existing = employmentService.getEmploymentById(id);
        if (!company.getId().equals(existing.getId_company())) {
            throw new ForbiddenException("No tiene permisos para eliminar ofertas de otra empresa.");
        }
        return new ResponseEntity<>(employmentService.deleteEmployment(id), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/getEmploymentsSummary")
    public ResponseEntity<List<EmploymentSummaryDto>> getEmploymentsSummary(){
        return new ResponseEntity<>(employmentService.getEmploymentsSummary(), HttpStatus.OK);
    }

}
