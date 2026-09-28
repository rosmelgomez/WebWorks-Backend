package com.upc.webworksbackend.security;

import com.upc.webworksbackend.exception.ForbiddenException;
import com.upc.webworksbackend.model.CompanyModel;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.repository.CompanyRespository;
import com.upc.webworksbackend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    private final UserRepository userRepository;
    private final CompanyRespository companyRespository;

    public CurrentUser(UserRepository userRepository, CompanyRespository companyRespository) {
        this.userRepository = userRepository;
        this.companyRespository = companyRespository;
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new ForbiddenException("No se encuentra ninguna sesión de usuario autenticada.");
        }
        return authentication.getName();
    }

    public UserModel getCurrentUser() {
        String username = getCurrentUsername();
        UserModel user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ForbiddenException("El usuario autenticado no existe en el sistema.");
        }
        return user;
    }

    public CompanyModel getCurrentCompany() {
        String username = getCurrentUsername();
        CompanyModel company = companyRespository.findByUsername(username);
        if (company == null) {
            throw new ForbiddenException("La empresa autenticada no existe en el sistema.");
        }
        return company;
    }
}
