package com.upc.webworksbackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.upc.webworksbackend.dto.MethodPaymentDto;
import com.upc.webworksbackend.exception.ForbiddenException;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.security.CurrentUser;
import com.upc.webworksbackend.serviceinterface.MethodPaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/methodpayment")
public class MethodPaymentController {
    private final MethodPaymentService methodPaymentService;
    private final CurrentUser currentUser;

    public MethodPaymentController(MethodPaymentService methodPaymentService, CurrentUser currentUser) {
        this.methodPaymentService = methodPaymentService;
        this.currentUser = currentUser;
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PostMapping("/addMethodPayment")
    public ResponseEntity<Boolean> addMethodPayment(@RequestBody MethodPaymentDto methodPaymentDto) {
        UserModel user = currentUser.getCurrentUser();
        if (methodPaymentDto.getId_user() != null && !methodPaymentDto.getId_user().equals(user.getId())) {
            throw new ForbiddenException("No puede registrar métodos de pago para otro usuario.");
        }
        methodPaymentDto.setId_user(user.getId());
        return new ResponseEntity<>(methodPaymentService.addMethodPayment(methodPaymentDto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/methodsPaymentByUser/{id}")
    public ResponseEntity<List<MethodPaymentDto>> methodsPaymentByUser(@PathVariable Integer id) {
        UserModel user = currentUser.getCurrentUser();
        if (!user.getId().equals(id)) {
            throw new ForbiddenException("No tiene permisos para consultar métodos de pago de otro usuario.");
        }
        return new ResponseEntity<>(methodPaymentService.methodsPaymentByUser(id), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/methodPaymentById/{id}")
    public ResponseEntity<MethodPaymentDto> methodPaymentById(@PathVariable Integer id) {
        UserModel user = currentUser.getCurrentUser();
        MethodPaymentDto dto = methodPaymentService.methodPaymentById(id);
        if (!user.getId().equals(dto.getId_user())) {
            throw new ForbiddenException("No tiene permisos para consultar este método de pago.");
        }
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @DeleteMapping("/deleteMethodPaymentById/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Integer id) {
        UserModel user = currentUser.getCurrentUser();
        MethodPaymentDto dto = methodPaymentService.methodPaymentById(id);
        if (!user.getId().equals(dto.getId_user())) {
            throw new ForbiddenException("No tiene permisos para eliminar este método de pago.");
        }
        return new ResponseEntity<>(methodPaymentService.deleteMethodPayment(id), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @PutMapping("/updateMethodPayment")
    public ResponseEntity<Boolean> methodPaymentUpdate(@RequestBody MethodPaymentDto methodPaymentDto) {
        UserModel user = currentUser.getCurrentUser();
        MethodPaymentDto dto = methodPaymentService.methodPaymentById(methodPaymentDto.getId());
        if (!user.getId().equals(dto.getId_user())) {
            throw new ForbiddenException("No tiene permisos para modificar este método de pago.");
        }
        return new ResponseEntity<>(methodPaymentService.updateMethodPayment(methodPaymentDto), HttpStatus.OK);
    }

}
