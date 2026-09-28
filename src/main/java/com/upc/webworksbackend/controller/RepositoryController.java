package com.upc.webworksbackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.upc.webworksbackend.dto.RepositoryDto;
import com.upc.webworksbackend.exception.ForbiddenException;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.security.CurrentUser;
import com.upc.webworksbackend.serviceinterface.RepositoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repository")
public class RepositoryController {

    private final RepositoryService repositoryService;
    private final CurrentUser currentUser;

    public RepositoryController(RepositoryService repositoryService, CurrentUser currentUser) {
        this.repositoryService = repositoryService;
        this.currentUser = currentUser;
    }

    ///USER
    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PostMapping("/addRepository")
    public ResponseEntity<Boolean> addRepository(@RequestBody RepositoryDto repositoryDto) {
        UserModel user = currentUser.getCurrentUser();
        if (repositoryDto.getId_user() != null && !repositoryDto.getId_user().equals(user.getId())) {
            throw new ForbiddenException("No puede registrar repositorios para otro usuario.");
        }
        repositoryDto.setId_user(user.getId());
        return new ResponseEntity<>(repositoryService.addRepository(repositoryDto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/repositoryByUser/{id}")
    public ResponseEntity<List<RepositoryDto>> repositoryByUser(@PathVariable Integer id) {
        return new ResponseEntity<>(repositoryService.repositoryByUser(id), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/repositoryById/{id}")
    public ResponseEntity<RepositoryDto> repositoryById(@PathVariable Integer id) {
        return new ResponseEntity<>(repositoryService.repositoryById(id), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping({"/normal/listRepository","/developer/listRepository"})
    public ResponseEntity<List<RepositoryDto>> findAll() {
        return new ResponseEntity<>(repositoryService.listRepository(), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PutMapping("/updateRepository")
    public ResponseEntity<Boolean> updateRepository(@RequestBody RepositoryDto repositoryDto) {
        UserModel user = currentUser.getCurrentUser();
        RepositoryDto existing = repositoryService.repositoryById(repositoryDto.getId());
        if (!user.getId().equals(existing.getId_user())) {
            throw new ForbiddenException("No tiene permisos para modificar un repositorio de otro usuario.");
        }
        return new ResponseEntity<>(repositoryService.updateRepository(repositoryDto), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @DeleteMapping("/deleteRepository/{id}")
    public ResponseEntity<Boolean> deleteRepository(@PathVariable Integer id) {
        UserModel user = currentUser.getCurrentUser();
        RepositoryDto existing = repositoryService.repositoryById(id);
        if (!user.getId().equals(existing.getId_user())) {
            throw new ForbiddenException("No tiene permisos para eliminar un repositorio de otro usuario.");
        }
        return new ResponseEntity<>(repositoryService.deleteRepository(id), HttpStatus.OK);
    }

    ///Company
    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/repositoryByUserCompany/{idUser}")
    public ResponseEntity<List<RepositoryDto>> repositoryByUserCompany(@PathVariable Integer idUser) {
        return new ResponseEntity<>(repositoryService.repositoryByUser(idUser), HttpStatus.OK);
    }
}
