package com.upc.webworksbackend.serviceinterface;

import com.upc.webworksbackend.dto.CompanyDto;
import com.upc.webworksbackend.exception.ForbiddenException;
import com.upc.webworksbackend.exception.NotFoundException;
import com.upc.webworksbackend.model.CompanyModel;
import com.upc.webworksbackend.repository.CompanyRespository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompanyService {

    private final CompanyRespository companyRespository;

    public CompanyService(CompanyRespository companyRespository) {
        this.companyRespository = companyRespository;
    }

    public CompanyDto addCompany(CompanyDto companyDto) {
        CompanyModel companyModel = companyRespository.findByUsername(companyDto.getUsername());
        if (companyModel == null) {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encodedPassword = passwordEncoder.encode(companyDto.getPassword());
            companyDto.setPassword(encodedPassword);
            ModelMapper modelMapper = new ModelMapper();
            companyModel = modelMapper.map(companyDto, CompanyModel.class);
            companyModel = companyRespository.save(companyModel);
            companyDto = modelMapper.map(companyModel, CompanyDto.class);
            return companyDto;
        }
        return null;
    }

    public CompanyDto companyByUsername(String username) {
        CompanyModel companyModel = companyRespository.findByUsername(username);
        if (companyModel != null) {
            ModelMapper modelMapper = new ModelMapper();
            return modelMapper.map(companyModel, CompanyDto.class);
        }
        throw new NotFoundException("Empresa no encontrada con username: " + username);
    }

    public Boolean updateCompany(CompanyDto companyDto) {
        CompanyModel companyModel = companyRespository.findById(companyDto.getId())
                .orElseThrow(() -> new NotFoundException("Empresa no encontrada con id: " + companyDto.getId()));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (companyDto.getCurrentPassword() == null || !passwordEncoder.matches(companyDto.getCurrentPassword(), companyModel.getPassword())) {
            throw new ForbiddenException("La contraseña actual es incorrecta.");
        }

        if (companyDto.getPassword() != null && !companyDto.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(companyDto.getPassword());
            companyModel.setPassword(encodedPassword);
        }
        companyModel.setRuc(companyDto.getRuc());
        companyModel.setSocialReason(companyDto.getSocialReason());
        companyModel.setSector(companyDto.getSector());
        companyModel.setLegalRepresentative(companyDto.getLegalRepresentative());
        companyModel.setDescription(companyDto.getDescription());
        companyModel.setUsername(companyDto.getUsername());
        // El rol se mantiene intacto
        companyRespository.save(companyModel);
        return true;
    }

    public List<CompanyDto> getAllCompany() {
        List<CompanyModel> companyModels = companyRespository.findAll();
        List<CompanyDto> companyDtos = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        for (CompanyModel companyModel1 : companyModels) {
            CompanyDto companyDto = modelMapper.map(companyModel1, CompanyDto.class);
            companyDtos.add(companyDto);
        }
        return companyDtos;
    }

}
