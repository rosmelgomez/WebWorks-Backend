package com.upc.webworksbackend.serviceinterface;

import com.upc.webworksbackend.dto.EmploymentDto;
import com.upc.webworksbackend.dtoaux.EmploymentSummaryDto;
import com.upc.webworksbackend.exception.NotFoundException;
import com.upc.webworksbackend.model.CompanyModel;
import com.upc.webworksbackend.model.EmploymentModel;
import com.upc.webworksbackend.repository.CompanyRespository;
import com.upc.webworksbackend.repository.EmploymentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmploymentService {

    private final EmploymentRepository employmentRepository;
    private final CompanyRespository companyRespository;

    public EmploymentService(EmploymentRepository employmentRepository, CompanyRespository companyRespository) {
        this.employmentRepository = employmentRepository;
        this.companyRespository = companyRespository;
    }

    public List<EmploymentDto> getEmploymentsByCompany(Integer idCompany){
        List<EmploymentModel> employmentModels = employmentRepository.findAll();
        List<EmploymentDto> employmentDtos = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        for(EmploymentModel employmentModel : employmentModels){
            if(employmentModel.getCompanyEmployment() != null && employmentModel.getCompanyEmployment().getId().equals(idCompany)){
                EmploymentDto employmentDto = modelMapper.map(employmentModel, EmploymentDto.class);
                employmentDto.setId_company(employmentModel.getCompanyEmployment().getId());
                employmentDtos.add(employmentDto);
            }
        }
        return employmentDtos;
    }

    public Boolean addEmployment(EmploymentDto employmentDto){
        CompanyModel companyModel = companyRespository.findById(employmentDto.getId_company())
                .orElseThrow(() -> new NotFoundException("Empresa no encontrada con id: " + employmentDto.getId_company()));
        ModelMapper modelMapper = new ModelMapper();
        EmploymentModel employmentModel = modelMapper.map(employmentDto, EmploymentModel.class);
        employmentModel.setId(null);
        employmentModel.setCompanyEmployment(companyModel);
        employmentRepository.save(employmentModel);
        return true;
    }

    public EmploymentDto getEmploymentById(Integer id){
        EmploymentModel employmentModel = employmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Oferta laboral no encontrada con id: " + id));
        ModelMapper modelMapper = new ModelMapper();
        EmploymentDto dto = modelMapper.map(employmentModel, EmploymentDto.class);
        if (employmentModel.getCompanyEmployment() != null) {
            dto.setId_company(employmentModel.getCompanyEmployment().getId());
        }
        return dto;
    }

    public Boolean updateEmployment(EmploymentDto employmentDto){
        EmploymentModel employmentModel = employmentRepository.findById(employmentDto.getId())
                .orElseThrow(() -> new NotFoundException("Oferta laboral no encontrada con id: " + employmentDto.getId()));
        employmentModel.setTitle(employmentDto.getTitle());
        employmentModel.setPosition(employmentDto.getPosition());
        employmentModel.setDescription(employmentDto.getDescription());
        employmentModel.setVacancies(employmentDto.getVacancies());
        if (employmentDto.getDateMaxPostulation() != null) {
            employmentModel.setDateMaxPostulation(employmentDto.getDateMaxPostulation());
        }
        employmentRepository.save(employmentModel);
        return true;
    }

    public Boolean deleteEmployment(Integer id){
        EmploymentModel employmentModel = employmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Oferta laboral no encontrada con id: " + id));
        employmentRepository.delete(employmentModel);
        return true;
    }

    public List<EmploymentSummaryDto> getEmploymentsSummary(){
        List<EmploymentModel> employmentModels = employmentRepository.findAll();
        List<EmploymentSummaryDto> employmentSummaryDto = new ArrayList<>();
        for(EmploymentModel employmentModel : employmentModels){
            if(!employmentModel.getDateMaxPostulation().isBefore(LocalDate.now())){
                EmploymentSummaryDto employmentDto = new EmploymentSummaryDto();
                employmentDto.setId(employmentModel.getId());
                if (employmentModel.getCompanyEmployment() != null) {
                    employmentDto.setCompanyName(employmentModel.getCompanyEmployment().getSocialReason());
                }
                employmentDto.setTitle(employmentModel.getTitle());
                employmentDto.setPosition(employmentModel.getPosition());
                employmentDto.setDescription(employmentModel.getDescription());
                employmentDto.setVacancies(employmentModel.getVacancies());
                employmentDto.setDateMaxPostulation(employmentModel.getDateMaxPostulation());
                employmentSummaryDto.add(employmentDto);
            }
        }
        return employmentSummaryDto;
    }

}
