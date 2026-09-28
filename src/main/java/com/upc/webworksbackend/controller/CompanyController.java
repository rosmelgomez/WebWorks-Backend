package com.upc.webworksbackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.upc.webworksbackend.dto.CompanyDto;
import com.upc.webworksbackend.exception.ForbiddenException;
import com.upc.webworksbackend.model.CompanyModel;
import com.upc.webworksbackend.security.CurrentUser;
import com.upc.webworksbackend.serviceinterface.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/company")
public class CompanyController {
    private final CompanyService companyService;
    private final CurrentUser currentUser;

    public CompanyController(CompanyService companyService, CurrentUser currentUser) {
        this.companyService = companyService;
        this.currentUser = currentUser;
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/List")
    public ResponseEntity<List<CompanyDto>> ListarCompany() {
        return new ResponseEntity<>(companyService.getAllCompany(), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/companyByUsername/{username}")
    public ResponseEntity<CompanyDto> companyByUsername(@PathVariable String username) {
        return new ResponseEntity<>(companyService.companyByUsername(username), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PutMapping("/updateCompany")
    public ResponseEntity<Boolean> updateCompany(@RequestBody CompanyDto companyDto) {
        CompanyModel currentCompany = currentUser.getCurrentCompany();
        if (!currentCompany.getId().equals(companyDto.getId())) {
            throw new ForbiddenException("No tiene permisos para modificar los datos de otra empresa.");
        }
        return new ResponseEntity<>(companyService.updateCompany(companyDto), HttpStatus.OK);
    }

}
