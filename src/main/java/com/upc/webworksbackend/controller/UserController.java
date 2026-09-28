package com.upc.webworksbackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.upc.webworksbackend.dto.UserDto;
import com.upc.webworksbackend.exception.ForbiddenException;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.security.CurrentUser;
import com.upc.webworksbackend.serviceinterface.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    /// User
    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/userByUsername/{username}")
    public ResponseEntity<UserDto> userByUsername(@PathVariable String username) {
        return new ResponseEntity<>(userService.userByUsername(username), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PutMapping("/updateUser")
    public ResponseEntity<Boolean> updateUser(@RequestBody UserDto userDto) {
        UserModel user = currentUser.getCurrentUser();
        if (!user.getId().equals(userDto.getId())) {
            throw new ForbiddenException("No tiene permisos para modificar los datos de otro usuario.");
        }
        return new ResponseEntity<>(userService.updateUser(userDto), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    /// Company
    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/userById/{idUser}")
    public ResponseEntity<UserDto> userById(@PathVariable Integer idUser) {
        return new ResponseEntity<>(userService.getUserById(idUser), HttpStatus.OK);
    }
}
