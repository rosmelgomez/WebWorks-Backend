package com.upc.webworksbackend.serviceinterface;

import com.upc.webworksbackend.dto.CommentProfileDto;
import com.upc.webworksbackend.dtoaux.CommentProfileSummaryDto;
import com.upc.webworksbackend.exception.NotFoundException;
import com.upc.webworksbackend.model.CommentProfileModel;
import com.upc.webworksbackend.model.SystemScoreModel;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.repository.CommentProfileRepository;
import com.upc.webworksbackend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentProfileService {
    private final CommentProfileRepository commentProfileRepository;
    private final UserRepository userRepository;
    private final SystemScoreService systemScoreService;

    public CommentProfileService(CommentProfileRepository commentProfileRepository, UserRepository userRepository, SystemScoreService systemScoreService) {
        this.commentProfileRepository = commentProfileRepository;
        this.userRepository = userRepository;
        this.systemScoreService = systemScoreService;
    }

    public Integer addCommentProfile(CommentProfileDto commentProfileDto) {
        UserModel userModel = userRepository.findById(commentProfileDto.getId_user())
                .orElseThrow(() -> new NotFoundException("Usuario autor no encontrado con id: " + commentProfileDto.getId_user()));
        ModelMapper modelMapper = new ModelMapper();
        CommentProfileModel commentProfileModel = modelMapper.map(commentProfileDto, CommentProfileModel.class);
        commentProfileModel.setId(null);
        commentProfileModel.setUserCommentProfile(userModel);
        return commentProfileRepository.save(commentProfileModel).getId();
    }

    public List<CommentProfileSummaryDto> listCommentProfilesByUserId(Integer userId) {
        List<SystemScoreModel> scoreModels = systemScoreService.listSystemScoreByUser(userId);
        List<CommentProfileSummaryDto> profileSummaryDtos = new ArrayList<>();
        for (SystemScoreModel scoreModel : scoreModels) {
            if (scoreModel.getCommentProfileSystemScore() != null) {
                CommentProfileSummaryDto commentProfileSummaryDto = new CommentProfileSummaryDto();
                commentProfileSummaryDto.setId(scoreModel.getCommentProfileSystemScore().getId());
                commentProfileSummaryDto.setScore(scoreModel.getCommentProfileSystemScore().getScore());
                commentProfileSummaryDto.setComment(scoreModel.getCommentProfileSystemScore().getComment());
                commentProfileSummaryDto.setCommentDate(scoreModel.getDateScore());
                if (scoreModel.getCommentProfileSystemScore().getUserCommentProfile() != null) {
                    String name = scoreModel.getCommentProfileSystemScore().getUserCommentProfile().getName() + " "
                            + scoreModel.getCommentProfileSystemScore().getUserCommentProfile().getLastname();
                    commentProfileSummaryDto.setCommentAuthor(name);
                } else {
                    commentProfileSummaryDto.setCommentAuthor("");
                }
                if (scoreModel.getUserSystemScore() != null) {
                    commentProfileSummaryDto.setId_user_comment(scoreModel.getUserSystemScore().getId());
                }
                commentProfileSummaryDto.setId_system_score(scoreModel.getId());
                profileSummaryDtos.add(commentProfileSummaryDto);
            }
        }
        return profileSummaryDtos;
    }

}
