package com.upc.webworksbackend.serviceinterface;

import com.upc.webworksbackend.dto.RepositoryDto;
import com.upc.webworksbackend.exception.BusinessRuleException;
import com.upc.webworksbackend.exception.NotFoundException;
import com.upc.webworksbackend.model.RepositoryModel;
import com.upc.webworksbackend.model.SubscriptionModel;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.repository.ProjectRepository;
import com.upc.webworksbackend.repository.RepositoryRepository;
import com.upc.webworksbackend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final SubscriptionService subscriptionService;

    public RepositoryService(RepositoryRepository repositoryRepository, UserRepository userRepository,
                             ProjectRepository projectRepository, SubscriptionService subscriptionService) {
        this.repositoryRepository = repositoryRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.subscriptionService = subscriptionService;
    }

    public List<RepositoryDto> listRepository() {
        List<RepositoryModel> repositoryModels = repositoryRepository.findAll();
        List<RepositoryDto> repositoryDtos = new ArrayList<>();
        for (RepositoryModel repositoryModel : repositoryModels) {
            repositoryDtos.add(toDto(repositoryModel));
        }
        return repositoryDtos;
    }

    public Boolean addRepository(RepositoryDto repositoryDto) {
        UserModel userModel = userRepository.findById(repositoryDto.getId_user())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + repositoryDto.getId_user()));

        SubscriptionModel active = subscriptionService.activeSubscription(userModel.getId())
                .orElseThrow(() -> new BusinessRuleException(
                        "Necesitas una suscripción activa antes de registrar repositorios."));
        int repositoryLimit = active.getPlanSubscription().getMaxNumberRepository();
        int projectLimit = active.getPlanSubscription().getMaxNumberProject();
        if (repositoryRepository.countByUserRepository_Id(userModel.getId()) >= repositoryLimit) {
            throw new BusinessRuleException("Alcanzaste el límite de " + repositoryLimit
                    + " repositorios de tu plan.");
        }
        if (repositoryDto.getNumberProject() < 1 || repositoryDto.getNumberProject() > projectLimit) {
            throw new BusinessRuleException("La capacidad del repositorio debe estar entre 1 y "
                    + projectLimit + " proyectos según tu plan.");
        }
        ModelMapper modelMapper = new ModelMapper();
        RepositoryModel repositoryModel = modelMapper.map(repositoryDto, RepositoryModel.class);
        repositoryModel.setId(null);
        if (repositoryModel.getDateCreate() == null) {
            repositoryModel.setDateCreate(new Date());
        }
        repositoryModel.setUserRepository(userModel);
        repositoryRepository.save(repositoryModel);
        return true;
    }

    public RepositoryDto repositoryById(Integer id) {
        return repositoryRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Repositorio no encontrado con id: " + id));
    }

    public List<RepositoryDto> repositoryByUser(Integer id) {
        List<RepositoryModel> repositoryModels = repositoryRepository.findAllByUserRepository_Id(id);
        List<RepositoryDto> repositoryDtos = new ArrayList<>();
        for (RepositoryModel repositoryModel : repositoryModels) {
            repositoryDtos.add(toDto(repositoryModel));
        }
        return repositoryDtos;
    }

    public Boolean updateRepository(RepositoryDto repositoryDto) {
        RepositoryModel repositoryModel = repositoryRepository.findById(repositoryDto.getId())
                .orElseThrow(() -> new NotFoundException("Repositorio no encontrado con id: " + repositoryDto.getId()));

        long currentProjects = projectRepository.countByRepositoryProject_Id(repositoryModel.getId());
        if (repositoryDto.getNumberProject() < currentProjects) {
            throw new BusinessRuleException("La capacidad no puede ser menor que los " + currentProjects + " proyectos ya registrados.");
        }
        if (repositoryDto.getNumberProject() > repositoryModel.getNumberProject()) {
            SubscriptionModel active = subscriptionService.activeSubscription(repositoryModel.getUserRepository().getId())
                    .orElseThrow(() -> new BusinessRuleException(
                            "Necesitas una suscripción activa para ampliar la capacidad del repositorio."));
            int planLimit = active.getPlanSubscription().getMaxNumberProject();
            if (repositoryDto.getNumberProject() > planLimit) {
                throw new BusinessRuleException("Tu plan permite como máximo " + planLimit + " proyectos.");
            }
        }
        repositoryModel.setName(repositoryDto.getName());
        repositoryModel.setDescription(repositoryDto.getDescription());
        repositoryModel.setNumberProject(repositoryDto.getNumberProject());
        repositoryRepository.save(repositoryModel);
        return true;
    }

    public Boolean deleteRepository(Integer id) {
        RepositoryModel repositoryModel = repositoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Repositorio no encontrado con id: " + id));
        repositoryRepository.delete(repositoryModel);
        return true;
    }

    private RepositoryDto toDto(RepositoryModel model) {
        ModelMapper mapper = new ModelMapper();
        RepositoryDto dto = mapper.map(model, RepositoryDto.class);
        if (model.getUserRepository() != null) {
            dto.setId_user(model.getUserRepository().getId());
        }
        return dto;
    }
}
