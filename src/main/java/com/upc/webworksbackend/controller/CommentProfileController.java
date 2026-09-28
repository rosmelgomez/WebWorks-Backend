package com.upc.webworksbackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.upc.webworksbackend.dto.CommentProfileDto;
import com.upc.webworksbackend.dtoaux.CommentProfileSummaryDto;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.security.CurrentUser;
import com.upc.webworksbackend.serviceinterface.CommentProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/commentProfile")
public class CommentProfileController {

    private final CommentProfileService commentProfileService;
    private final CurrentUser currentUser;

    public CommentProfileController(CommentProfileService commentProfileService, CurrentUser currentUser) {
        this.commentProfileService = commentProfileService;
        this.currentUser = currentUser;
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PostMapping("/addComment")
    public ResponseEntity<Integer> addCommentProfile(@RequestBody CommentProfileDto commentProfileDto) {
        UserModel user = currentUser.getCurrentUser();
        // El autor debe ser el usuario autenticado (no el que venga en el body)
        commentProfileDto.setId_user(user.getId());
        return new ResponseEntity<>(commentProfileService.addCommentProfile(commentProfileDto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/getCommentProfileByUser/{idUser}")
    public ResponseEntity<List<CommentProfileSummaryDto>> getCommentProfileByUser(@PathVariable Integer idUser) {
        return new ResponseEntity<>(commentProfileService.listCommentProfilesByUserId(idUser), HttpStatus.OK);
    }
}
