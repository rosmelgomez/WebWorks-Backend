package com.upc.webworksbackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.upc.webworksbackend.dto.SubscriptionDbo;
import com.upc.webworksbackend.dtoaux.SubscriptionCheck;
import com.upc.webworksbackend.dtoaux.SubscriptionSummaryDto;
import com.upc.webworksbackend.exception.ForbiddenException;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.security.CurrentUser;
import com.upc.webworksbackend.serviceinterface.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscription")
public class SubscriptionController {
    final SubscriptionService subscriptionService;
    final CurrentUser currentUser;

    public SubscriptionController(SubscriptionService subscriptionService, CurrentUser currentUser) {
        this.subscriptionService = subscriptionService;
        this.currentUser = currentUser;
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/subscriptionActive/{id}")
    public ResponseEntity<SubscriptionCheck> SubscriptionsActives(@PathVariable Integer id) {
        return new ResponseEntity<>(subscriptionService.SubscriptionActive(id), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PostMapping("/addSubscription")
    public ResponseEntity<Boolean> addSubscription(@RequestBody SubscriptionDbo subscriptionDbo) {
        UserModel user = currentUser.getCurrentUser();
        if (subscriptionDbo.getId_user() != null && !subscriptionDbo.getId_user().equals(user.getId())) {
            throw new ForbiddenException("No puede registrar suscripciones para otro usuario.");
        }
        subscriptionDbo.setId_user(user.getId());
        return new ResponseEntity<>(subscriptionService.addSubscription(subscriptionDbo), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/listSubscriptionsByUser/{idUser}")
    public ResponseEntity<List<SubscriptionSummaryDto>> listSubscriptionsByUser(@PathVariable Integer idUser) {
        UserModel user = currentUser.getCurrentUser();
        if (!user.getId().equals(idUser)) {
            throw new ForbiddenException("No tiene permisos para consultar las suscripciones de otro usuario.");
        }
        return new ResponseEntity<>(subscriptionService.listSubscriptionsByUser(idUser), HttpStatus.OK);
    }

}
