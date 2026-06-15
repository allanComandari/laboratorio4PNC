package com.server.app.services;

import com.server.app.dto.planpago.PlanPagoResponseDto;
import com.server.app.entities.PlanPago;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.PlanPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanPagoService {

    private final PlanPagoRepository planPagoRepository;

    public PlanPagoResponseDto findById(int id) {
        PlanPago plan = planPagoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plan de pago no encontrado con ID: " + id));
        return convertToResponseDto(plan);
    }

    public List<PlanPagoResponseDto> findByPrestamoId(int prestamoId) {
        return planPagoRepository.findByPrestamoIdOrderByNumeroCuotaAsc(prestamoId).stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public PlanPagoResponseDto convertToResponseDto(PlanPago plan) {
        return PlanPagoResponseDto.builder()
                .id(plan.getId())
                .numeroCuota(plan.getNumeroCuota())
                .montoCapital(plan.getMontoCapital())
                .montoInteres(plan.getMontoInteres())
                .totalCuota(plan.getMontoCapital().add(plan.getMontoInteres()))
                .fechaVencimiento(plan.getFechaVencimiento())
                .estado(plan.getEstado())
                .prestamoId(plan.getPrestamo().getId())
                .build();
    }
}
