package com.upc.webworksbackend.serviceinterface;

import com.upc.webworksbackend.dto.JobApplicationDto;
import com.upc.webworksbackend.dtoaux.JobApplicationSummaryDto;
import com.upc.webworksbackend.exception.NotFoundException;
import com.upc.webworksbackend.model.EmploymentModel;
import com.upc.webworksbackend.model.JobApplicationModel;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.repository.EmploymentRepository;
import com.upc.webworksbackend.repository.JobApplicationRepository;
import com.upc.webworksbackend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;
    private final EmploymentRepository employmentRepository;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository, UserRepository userRepository, EmploymentRepository employmentRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
        this.employmentRepository = employmentRepository;
    }

    public Boolean addJobApplication(JobApplicationDto jobApplicationDto){
        UserModel userModel=userRepository.findById(jobApplicationDto.getId_user())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + jobApplicationDto.getId_user()));
        EmploymentModel employmentModel=employmentRepository.findById(jobApplicationDto.getId_employment())
                .orElseThrow(() -> new NotFoundException("Oferta laboral no encontrada con id: " + jobApplicationDto.getId_employment()));

        ModelMapper modelMapper = new ModelMapper();
        JobApplicationModel jobApplicationModel = modelMapper.map(jobApplicationDto, JobApplicationModel.class);
        jobApplicationModel.setId(null);
        if (jobApplicationModel.getDateApplication() == null) {
            jobApplicationModel.setDateApplication(new Date());
        }
        jobApplicationModel.setUserJobApplication(userModel);
        jobApplicationModel.setEmploymentJobApplication(employmentModel);
        jobApplicationRepository.save(jobApplicationModel);
        return true;
    }

    public Boolean checkJobApplication(Integer idEmployment, Integer idUser){
        List<JobApplicationModel> jobApplicationModels=jobApplicationRepository.findAll();

        for(JobApplicationModel jobApplicationModel:jobApplicationModels){
            if (jobApplicationModel.getEmploymentJobApplication() != null && jobApplicationModel.getUserJobApplication() != null &&
                    jobApplicationModel.getEmploymentJobApplication().getId().equals(idEmployment) && jobApplicationModel.getUserJobApplication().getId().equals(idUser) &&
                    (Objects.equals(jobApplicationModel.getStatus(), "ACCEPTED") || Objects.equals(jobApplicationModel.getStatus(), "PENDING"))){
                return true;
            }
        }
        return false;
    }

    public List<JobApplicationSummaryDto> getJobApplicationStatusByCompany(Integer idCompany, String status){
        List<JobApplicationModel> jobApplicationModels=jobApplicationRepository.findAll();
        List<JobApplicationSummaryDto> jobApplicationDtos=new ArrayList<>();
        for(JobApplicationModel jobApplicationModel:jobApplicationModels){
            if(jobApplicationModel.getEmploymentJobApplication() != null &&
                    jobApplicationModel.getEmploymentJobApplication().getCompanyEmployment() != null &&
                    jobApplicationModel.getEmploymentJobApplication().getCompanyEmployment().getId().equals(idCompany) &&
                    Objects.equals(jobApplicationModel.getStatus(), status)){
                JobApplicationSummaryDto jobApplicationSummaryDto = getJobApplicationViewDto(jobApplicationModel);
                jobApplicationDtos.add(jobApplicationSummaryDto);
            }
        }
        return jobApplicationDtos;
    }

    private static JobApplicationSummaryDto getJobApplicationViewDto(JobApplicationModel jobApplicationModel) {
        JobApplicationSummaryDto jobApplicationSummaryDto =new JobApplicationSummaryDto();
        jobApplicationSummaryDto.setId(jobApplicationModel.getId());
        jobApplicationSummaryDto.setStatus(jobApplicationModel.getStatus());
        jobApplicationSummaryDto.setDateApplication(jobApplicationModel.getDateApplication());
        if (jobApplicationModel.getUserJobApplication() != null) {
            jobApplicationSummaryDto.setNamePostulation(jobApplicationModel.getUserJobApplication().getName()+" " + jobApplicationModel.getUserJobApplication().getLastname());
            jobApplicationSummaryDto.setId_user(jobApplicationModel.getUserJobApplication().getId());
        }
        if (jobApplicationModel.getEmploymentJobApplication() != null) {
            jobApplicationSummaryDto.setNameEmployment(jobApplicationModel.getEmploymentJobApplication().getTitle());
            jobApplicationSummaryDto.setPositionEmployment(jobApplicationModel.getEmploymentJobApplication().getPosition());
            jobApplicationSummaryDto.setId_employment(jobApplicationModel.getEmploymentJobApplication().getId());
        }
        return jobApplicationSummaryDto;
    }

    public JobApplicationModel getJobApplicationEntity(Integer idJobApplication) {
        return jobApplicationRepository.findById(idJobApplication)
                .orElseThrow(() -> new NotFoundException("Postulación no encontrada con id: " + idJobApplication));
    }

    public Boolean changeJobApplication(Integer idJobApplication, String status){
        JobApplicationModel jobApplicationModel = getJobApplicationEntity(idJobApplication);
        EmploymentModel employmentModel = employmentRepository.findById(jobApplicationModel.getEmploymentJobApplication().getId())
                .orElseThrow(() -> new NotFoundException("Oferta laboral no encontrada para la postulación"));
        if(employmentModel.getVacancies()>0 && Objects.equals(status, "ACCEPTED")){
            jobApplicationModel.setStatus(status);
            employmentModel.setVacancies(employmentModel.getVacancies()-1);
            employmentModel.setContracted(employmentModel.getContracted()+1);
            employmentRepository.save(employmentModel);
            jobApplicationRepository.save(jobApplicationModel);
            return true;
        } else if(Objects.equals(status, "REFUSE")){
            jobApplicationModel.setStatus(status);
            jobApplicationRepository.save(jobApplicationModel);
            return true;
        }
        return false;
    }

}
