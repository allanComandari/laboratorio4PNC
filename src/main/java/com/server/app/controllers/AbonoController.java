package com.server.app.controllers;

import com.server.app.dto.abono.AbonoCreateDto;
import com.server.app.dto.abono.AbonoResponseDto;
import com.server.app.services.AbonoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/abonos")
@RequiredArgsConstructor
public class AbonoController {

    private final AbonoService abonoService;

    @PostMapping
    public ResponseEntity<AbonoResponseDto> create(@Valid @RequestBody AbonoCreateDto dto) {
        AbonoResponseDto response = abonoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/plan-pago/{planPagoId}")
    public ResponseEntity<List<AbonoResponseDto>> findByPlanPagoId(@PathVariable int planPagoId) {
        List<AbonoResponseDto> response = abonoService.findByPlanPagoId(planPagoId);
        return ResponseEntity.ok(response);
    }
}
