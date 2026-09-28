package com.upc.webworksbackend.controller;

import com.upc.webworksbackend.dto.PlanDto;
import com.upc.webworksbackend.serviceinterface.PlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plan")
public class PlanController {

    final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/getPlanes")
    public ResponseEntity<List<PlanDto>> listPlan() {
        return new ResponseEntity<>(planService.ListPlan(), HttpStatus.OK);
    }
    @GetMapping("/getPlanById/{idPlan}")
    public ResponseEntity<PlanDto> getPlanById(@PathVariable Integer idPlan) {
        return new ResponseEntity<>(planService.getPlanById(idPlan), HttpStatus.OK);
    }
}
