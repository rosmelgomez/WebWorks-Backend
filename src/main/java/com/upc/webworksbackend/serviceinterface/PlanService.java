package com.upc.webworksbackend.serviceinterface;

import com.upc.webworksbackend.dto.PlanDto;
import com.upc.webworksbackend.exception.NotFoundException;
import com.upc.webworksbackend.model.PlanModel;
import com.upc.webworksbackend.repository.PlanRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PlanService {

    final private PlanRepository planRepository;
    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<PlanDto> ListPlan(){
        List<PlanModel> listPlan = planRepository.findAll();
        ModelMapper modelMapper = new ModelMapper();
        return Arrays.asList(modelMapper.map(listPlan , PlanDto[].class));
    }

    public PlanDto getPlanById(Integer id){
        PlanModel planModel = planRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plan no encontrado con id: " + id));
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(planModel , PlanDto.class);
    }
}
