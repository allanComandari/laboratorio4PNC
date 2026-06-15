package com.server.app.controllers;

import com.server.app.dto.planpago.PlanPagoResponseDto;
import com.server.app.services.PlanPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plan-pagos")
@RequiredArgsConstructor
public class PlanPagoController {

    private final PlanPagoService planPagoService;

    @GetMapping("/{id}")
    public ResponseEntity<PlanPagoResponseDto> findById(@PathVariable int id) {
        PlanPagoResponseDto response = planPagoService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/prestamo/{prestamoId}")
    public ResponseEntity<List<PlanPagoResponseDto>> findByPrestamoId(@PathVariable int prestamoId) {
        List<PlanPagoResponseDto> response = planPagoService.findByPrestamoId(prestamoId);
        return ResponseEntity.ok(response);
    }
}
