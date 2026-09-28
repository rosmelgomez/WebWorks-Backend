package com.upc.webworksbackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.upc.webworksbackend.dto.PromotionCodeDto;
import com.upc.webworksbackend.serviceinterface.PromotionCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController()
@RequestMapping("/promotionCode")
public class PromotionCodeController {
    final PromotionCodeService promotionCodeService;

    public PromotionCodeController(PromotionCodeService promotionCodeService) {
        this.promotionCodeService = promotionCodeService;
    }
    @PreAuthorize("hasAnyAuthority('DEVELOPER','COMPANY')")
    @GetMapping("/getPromotion/{code}")
    public ResponseEntity<PromotionCodeDto> projectsRepository(@PathVariable String code) {
        return new ResponseEntity<>(promotionCodeService.getPromotionCode(code), HttpStatus.OK);
    }
}
