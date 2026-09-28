package com.upc.webworksbackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.upc.webworksbackend.dto.JobApplicationDto;
import com.upc.webworksbackend.dtoaux.JobApplicationSummaryDto;
import com.upc.webworksbackend.exception.ForbiddenException;
import com.upc.webworksbackend.model.CompanyModel;
import com.upc.webworksbackend.model.JobApplicationModel;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.security.CurrentUser;
import com.upc.webworksbackend.serviceinterface.JobApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/jobApplication")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final CurrentUser currentUser;

    public JobApplicationController(JobApplicationService jobApplicationService, CurrentUser currentUser) {
        this.jobApplicationService = jobApplicationService;
        this.currentUser = currentUser;
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PostMapping("/addJobApplication")
    public ResponseEntity<Boolean> addJobApplication(@RequestBody JobApplicationDto jobApplicationDto){
        UserModel user = currentUser.getCurrentUser();
        if (jobApplicationDto.getId_user() != null && !jobApplicationDto.getId_user().equals(user.getId())) {
            throw new ForbiddenException("No puede postular en nombre de otro usuario.");
        }
        jobApplicationDto.setId_user(user.getId());
        return new ResponseEntity<>(jobApplicationService.addJobApplication(jobApplicationDto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/checkJobApplication/{idEmployment}/{idUser}")
    public ResponseEntity<Boolean> checkJobApplication(@PathVariable Integer idEmployment, @PathVariable Integer idUser){
        UserModel user = currentUser.getCurrentUser();
        if (!user.getId().equals(idUser)) {
            throw new ForbiddenException("No tiene permisos para consultar postulaciones de otro usuario.");
        }
        return new ResponseEntity<>(jobApplicationService.checkJobApplication(idEmployment, idUser), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/getJobApplicationStatusByCompany/{idCompany}/{status}")
    public ResponseEntity<List<JobApplicationSummaryDto>> getJobApplicationStatusByCompany(@PathVariable Integer idCompany , @PathVariable String status){
        return new ResponseEntity<>(jobApplicationService.getJobApplicationStatusByCompany(idCompany, status), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PutMapping("/changeJobApplication/{idJobApplication}/{status}")
    public ResponseEntity<Boolean> changeJobApplication(@PathVariable Integer idJobApplication, @PathVariable String status){
        CompanyModel company = currentUser.getCurrentCompany();
        JobApplicationModel app = jobApplicationService.getJobApplicationEntity(idJobApplication);
        if (app.getEmploymentJobApplication() == null || app.getEmploymentJobApplication().getCompanyEmployment() == null
                || !app.getEmploymentJobApplication().getCompanyEmployment().getId().equals(company.getId())) {
            throw new ForbiddenException("Solo la empresa dueña de la oferta laboral puede cambiar el estado de la postulación.");
        }
        return new ResponseEntity<>(jobApplicationService.changeJobApplication(idJobApplication, status), HttpStatus.OK);
    }

}
