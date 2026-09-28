package com.upc.webworksbackend.serviceinterface;

import com.upc.webworksbackend.dto.MethodPaymentDto;
import com.upc.webworksbackend.exception.NotFoundException;
import com.upc.webworksbackend.model.MethodPaymentModel;
import com.upc.webworksbackend.model.MoneyModel;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.repository.MethodPaymentRepository;
import com.upc.webworksbackend.repository.MoneyRepository;
import com.upc.webworksbackend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MethodPaymentService {
    final MethodPaymentRepository methodPaymentRepository;
    final UserRepository userRepository;
    final MoneyRepository moneyRepository;

    public MethodPaymentService(MethodPaymentRepository methodPaymentRepository, UserRepository userRepository, MoneyRepository moneyRepository) {
        this.methodPaymentRepository = methodPaymentRepository;
        this.userRepository = userRepository;
        this.moneyRepository = moneyRepository;
    }

    public Boolean addMethodPayment(MethodPaymentDto methodPaymentDto){
        UserModel personModel= userRepository.findById(methodPaymentDto.getId_user())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + methodPaymentDto.getId_user()));
        MoneyModel moneyModel= moneyRepository.findById(methodPaymentDto.getId_money())
                .orElseThrow(() -> new NotFoundException("Moneda no encontrada con id: " + methodPaymentDto.getId_money()));

        ModelMapper modelMapper = new ModelMapper();
        MethodPaymentModel methodPaymentModel = modelMapper.map(methodPaymentDto, MethodPaymentModel.class);
        methodPaymentModel.setId(null);
        methodPaymentModel.setUserMethodPayment(personModel);
        methodPaymentModel.setMoneyMethodPayment(moneyModel);
        methodPaymentRepository.save(methodPaymentModel);
        return true;
    }

    public List<MethodPaymentDto> methodsPaymentByUser(Integer id){
        List<MethodPaymentModel> methodPaymentModels=methodPaymentRepository.findAll();
        List<MethodPaymentDto> methodPaymentDtos=new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        for (MethodPaymentModel methodPaymentModel : methodPaymentModels) {
            if(methodPaymentModel.getUserMethodPayment() != null && methodPaymentModel.getUserMethodPayment().getId().equals(id)){
                MethodPaymentDto methodPaymentDto = modelMapper.map(methodPaymentModel, MethodPaymentDto.class);
                methodPaymentDto.setId_user(methodPaymentModel.getUserMethodPayment().getId());
                if (methodPaymentModel.getMoneyMethodPayment() != null) {
                    methodPaymentDto.setId_money(methodPaymentModel.getMoneyMethodPayment().getId());
                }
                methodPaymentDtos.add(methodPaymentDto);
            }
        }
        return methodPaymentDtos;
    }

    public MethodPaymentDto methodPaymentById(Integer id){
        MethodPaymentModel methodPaymentModel=methodPaymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Método de pago no encontrado con id: " + id));
        ModelMapper modelMapper = new ModelMapper();
        MethodPaymentDto dto = modelMapper.map(methodPaymentModel, MethodPaymentDto.class);
        if (methodPaymentModel.getUserMethodPayment() != null) {
            dto.setId_user(methodPaymentModel.getUserMethodPayment().getId());
        }
        if (methodPaymentModel.getMoneyMethodPayment() != null) {
            dto.setId_money(methodPaymentModel.getMoneyMethodPayment().getId());
        }
        return dto;
    }

    public Boolean deleteMethodPayment(Integer id){
        MethodPaymentModel methodPaymentModel=methodPaymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Método de pago no encontrado con id: " + id));
        methodPaymentRepository.delete(methodPaymentModel);
        return true;
    }

    public Boolean updateMethodPayment(MethodPaymentDto methodPaymentDto){
        MethodPaymentModel methodPaymentModel=methodPaymentRepository.findById(methodPaymentDto.getId())
                .orElseThrow(() -> new NotFoundException("Método de pago no encontrado con id: " + methodPaymentDto.getId()));
        methodPaymentModel.setNumberCard(methodPaymentDto.getNumberCard());
        methodPaymentModel.setDateCard(methodPaymentDto.getDateCard());
        methodPaymentModel.setCvv(methodPaymentDto.getCvv());
        methodPaymentRepository.save(methodPaymentModel);
        return true;
    }
}
