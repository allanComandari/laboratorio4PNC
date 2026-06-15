package com.server.app.controllers;

import com.server.app.dto.prestamo.PrestamoCreateDto;
import com.server.app.dto.prestamo.PrestamoResponseDto;
import com.server.app.services.PrestamoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;

    @PostMapping
    public ResponseEntity<PrestamoResponseDto> create(@Valid @RequestBody PrestamoCreateDto dto) {
        PrestamoResponseDto response = prestamoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PrestamoResponseDto>> findAll() {
        List<PrestamoResponseDto> response = prestamoService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrestamoResponseDto> findById(@PathVariable int id) {
        PrestamoResponseDto response = prestamoService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<PrestamoResponseDto>> findByUserId(@PathVariable int userId) {
        List<PrestamoResponseDto> response = prestamoService.findByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        prestamoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
