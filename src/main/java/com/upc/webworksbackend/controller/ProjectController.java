package com.upc.webworksbackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.upc.webworksbackend.dto.ProjectDbo;
import com.upc.webworksbackend.dto.RepositoryDto;
import com.upc.webworksbackend.exception.ForbiddenException;
import com.upc.webworksbackend.model.ProjectModel;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.security.CurrentUser;
import com.upc.webworksbackend.serviceinterface.ProjectService;
import com.upc.webworksbackend.serviceinterface.RepositoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/project")
public class ProjectController {
    private final ProjectService projectService;
    private final RepositoryService repositoryService;
    private final CurrentUser currentUser;

    public ProjectController(ProjectService projectService, RepositoryService repositoryService, CurrentUser currentUser) {
        this.projectService = projectService;
        this.repositoryService = repositoryService;
        this.currentUser = currentUser;
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PostMapping("/addProject")
    public ResponseEntity<Boolean> agregarProyecto(@RequestBody ProjectDbo projectDbo) {
        UserModel user = currentUser.getCurrentUser();
        RepositoryDto repo = repositoryService.repositoryById(projectDbo.getId_repository());
        if (!user.getId().equals(repo.getId_user())) {
            throw new ForbiddenException("No tiene permisos para agregar proyectos en repositorios de otro usuario.");
        }
        return new ResponseEntity<>(projectService.addProject(projectDbo), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/projectsRepository/{id}")
    public ResponseEntity<List<ProjectDbo>> projectsRepository(@PathVariable Integer id) {
        return new ResponseEntity<>(projectService.projectsByRepository(id), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/projectById/{id}")
    public ResponseEntity<ProjectDbo> projectById(@PathVariable Integer id) {
        return new ResponseEntity<>(projectService.projectById(id), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PutMapping("/updateProject")
    public ResponseEntity<Boolean> updateProject(@RequestBody ProjectDbo projectDbo) {
        UserModel user = currentUser.getCurrentUser();
        ProjectModel project = projectService.getProjectEntity(projectDbo.getId());
        if (project.getRepositoryProject() == null || project.getRepositoryProject().getUserRepository() == null
                || !project.getRepositoryProject().getUserRepository().getId().equals(user.getId())) {
            throw new ForbiddenException("No tiene permisos para modificar proyectos de un repositorio ajeno.");
        }
        return new ResponseEntity<>(projectService.updateProject(projectDbo), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @DeleteMapping("/deleteProject/{id}")
    public ResponseEntity<Boolean> deleteProject(@PathVariable Integer id) {
        UserModel user = currentUser.getCurrentUser();
        ProjectModel project = projectService.getProjectEntity(id);
        if (project.getRepositoryProject() == null || project.getRepositoryProject().getUserRepository() == null
                || !project.getRepositoryProject().getUserRepository().getId().equals(user.getId())) {
            throw new ForbiddenException("No tiene permisos para eliminar proyectos de un repositorio ajeno.");
        }
        return new ResponseEntity<>(projectService.deleteProject(id), HttpStatus.OK);
    }

}
