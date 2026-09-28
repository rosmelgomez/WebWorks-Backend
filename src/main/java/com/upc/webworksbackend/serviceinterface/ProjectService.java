package com.upc.webworksbackend.serviceinterface;

import com.upc.webworksbackend.dto.ProjectDbo;
import com.upc.webworksbackend.exception.BusinessRuleException;
import com.upc.webworksbackend.exception.NotFoundException;
import com.upc.webworksbackend.model.ProjectModel;
import com.upc.webworksbackend.model.RepositoryModel;
import com.upc.webworksbackend.model.SubscriptionModel;
import com.upc.webworksbackend.repository.ProjectRepository;
import com.upc.webworksbackend.repository.RepositoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ProjectService {
    final ProjectRepository projectRepository;
    final RepositoryRepository repositoryRepository;
    final SubscriptionService subscriptionService;

    public ProjectService(ProjectRepository projectRepository, RepositoryRepository repositoryRepository,
                          SubscriptionService subscriptionService) {
        this.projectRepository = projectRepository;
        this.repositoryRepository = repositoryRepository;
        this.subscriptionService = subscriptionService;
    }

    public Boolean addProject(ProjectDbo projectDbo) {
        if (projectDbo.getId_repository() == null) {
            throw new BusinessRuleException("El repositorio es requerido.");
        }
        RepositoryModel repositoryModel = repositoryRepository.findById(projectDbo.getId_repository())
                .orElseThrow(() -> new NotFoundException("Repositorio no encontrado con id: " + projectDbo.getId_repository()));

        SubscriptionModel active = subscriptionService.activeSubscription(repositoryModel.getUserRepository().getId())
                .orElseThrow(() -> new BusinessRuleException("Necesitas una suscripción activa para registrar proyectos."));

        int planLimit = active.getPlanSubscription().getMaxNumberProject();
        long totalProjectsUser = projectRepository.countByRepositoryProject_UserRepository_Id(repositoryModel.getUserRepository().getId());
        if (totalProjectsUser >= planLimit) {
            throw new BusinessRuleException("Alcanzaste el límite de " + planLimit + " proyectos de tu plan.");
        }

        long repoProjects = projectRepository.countByRepositoryProject_Id(repositoryModel.getId());
        if (repoProjects >= repositoryModel.getNumberProject()) {
            throw new BusinessRuleException("El repositorio ha alcanzado su capacidad máxima de " + repositoryModel.getNumberProject() + " proyectos.");
        }

        ModelMapper modelMapper = new ModelMapper();
        ProjectModel projectModel = modelMapper.map(projectDbo, ProjectModel.class);
        projectModel.setId(null);
        if (projectModel.getDateCreate() == null) {
            projectModel.setDateCreate(new Date());
        }
        projectModel.setRepositoryProject(repositoryModel);
        projectRepository.save(projectModel);
        return true;
    }

    public List<ProjectDbo> findAll() {
        List<ProjectModel> projectModels = projectRepository.findAll();
        List<ProjectDbo> projectDbos = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        for (ProjectModel projectModel : projectModels) {
            ProjectDbo projectDbo = modelMapper.map(projectModel, ProjectDbo.class);
            if (projectModel.getRepositoryProject() != null) {
                projectDbo.setId_repository(projectModel.getRepositoryProject().getId());
            }
            projectDbos.add(projectDbo);
        }
        return projectDbos;
    }

    public List<ProjectDbo> projectsByRepository(Integer id) {
        List<ProjectModel> projectModels = projectRepository.findAllByRepositoryProject_Id(id);
        List<ProjectDbo> projectDbos = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        for (ProjectModel projectModel : projectModels) {
            ProjectDbo projectDbo = modelMapper.map(projectModel, ProjectDbo.class);
            if (projectModel.getRepositoryProject() != null) {
                projectDbo.setId_repository(projectModel.getRepositoryProject().getId());
            }
            projectDbos.add(projectDbo);
        }
        return projectDbos;
    }

    public ProjectDbo projectById(Integer id) {
        ProjectModel project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proyecto no encontrado con id: " + id));
        ModelMapper modelMapper = new ModelMapper();
        ProjectDbo projectDbo = modelMapper.map(project, ProjectDbo.class);
        if (project.getRepositoryProject() != null) {
            projectDbo.setId_repository(project.getRepositoryProject().getId());
        }
        return projectDbo;
    }

    public ProjectModel getProjectEntity(Integer id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proyecto no encontrado con id: " + id));
    }

    public Boolean updateProject(ProjectDbo projectDbo) {
        ProjectModel projectModel = projectRepository.findById(projectDbo.getId())
                .orElseThrow(() -> new NotFoundException("Proyecto no encontrado con id: " + projectDbo.getId()));
        projectModel.setName(projectDbo.getName());
        projectModel.setDescription(projectDbo.getDescription());
        projectModel.setLanguage(projectDbo.getLanguage());
        projectRepository.save(projectModel);
        return true;
    }

    public Boolean deleteProject(Integer id) {
        ProjectModel projectModel = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proyecto no encontrado con id: " + id));
        projectRepository.delete(projectModel);
        return true;
    }
}
