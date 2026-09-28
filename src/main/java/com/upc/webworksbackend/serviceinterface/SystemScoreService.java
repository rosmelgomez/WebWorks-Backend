package com.upc.webworksbackend.serviceinterface;

import com.upc.webworksbackend.dto.SystemScoreDto;
import com.upc.webworksbackend.exception.ForbiddenException;
import com.upc.webworksbackend.exception.NotFoundException;
import com.upc.webworksbackend.model.CommentProfileModel;
import com.upc.webworksbackend.model.SystemScoreModel;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.repository.CommentProfileRepository;
import com.upc.webworksbackend.repository.UserRepository;
import com.upc.webworksbackend.repository.SystemScoreRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SystemScoreService {
    final SystemScoreRepository systemScoreRepository;
    final CommentProfileRepository commentProfileRepository;
    final UserRepository userRepository;

    public SystemScoreService(SystemScoreRepository systemScoreRepository, CommentProfileRepository commentProfileRepository, UserRepository userRepository) {
        this.systemScoreRepository = systemScoreRepository;
        this.commentProfileRepository = commentProfileRepository;
        this.userRepository = userRepository;
    }

    public Boolean addSystemScore(SystemScoreDto systemScoreDto){
        CommentProfileModel commentProfileModel = commentProfileRepository.findById(systemScoreDto.getId_commentProfile())
                .orElseThrow(() -> new NotFoundException("Comentario de perfil no encontrado con id: " + systemScoreDto.getId_commentProfile()));
        UserModel userModel = userRepository.findById(systemScoreDto.getId_user())
                .orElseThrow(() -> new NotFoundException("Usuario destino no encontrado con id: " + systemScoreDto.getId_user()));

        // Impedir comentarse o calificarse a si mismo
        if (commentProfileModel.getUserCommentProfile() != null &&
                commentProfileModel.getUserCommentProfile().getId().equals(userModel.getId())) {
            throw new ForbiddenException("No puedes calificar o comentar tu propio perfil.");
        }

        ModelMapper modelMapper = new ModelMapper();
        SystemScoreModel systemScoreModel = modelMapper.map(systemScoreDto, SystemScoreModel.class);
        systemScoreModel.setId(null);
        if (systemScoreModel.getDateScore() == null) {
            systemScoreModel.setDateScore(new Date());
        }
        systemScoreModel.setCommentProfileSystemScore(commentProfileModel);
        systemScoreModel.setUserSystemScore(userModel);
        systemScoreRepository.save(systemScoreModel);
        return true;
    }

    public List<SystemScoreModel> listSystemScoreByUser(Integer idUser){
        List<SystemScoreModel> scoreModels = systemScoreRepository.findAll();
        List<SystemScoreModel> systemScoreModels = new ArrayList<>();
        for (SystemScoreModel scoreModel : scoreModels) {
            if (scoreModel.getUserSystemScore() != null && scoreModel.getUserSystemScore().getId().equals(idUser)) {
                systemScoreModels.add(scoreModel);
            }
        }
        return systemScoreModels;
    }

}
