package com.server.app.services;

import com.server.app.dto.abono.AbonoCreateDto;
import com.server.app.dto.abono.AbonoResponseDto;
import com.server.app.entities.*;
import com.server.app.exceptions.BadRequestException;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.AbonoRepository;
import com.server.app.repositories.PlanPagoRepository;
import com.server.app.repositories.PrestamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AbonoService {

    private final AbonoRepository abonoRepository;
    private final PlanPagoRepository planPagoRepository;
    private final PrestamoRepository prestamoRepository;

    @Transactional
    public AbonoResponseDto create(AbonoCreateDto dto) {
        PlanPago planPago = planPagoRepository.findById(dto.getPlanPagoId())
                .orElseThrow(() -> new NotFoundException("Plan de pago no encontrado con ID: " + dto.getPlanPagoId()));

        if (planPago.getEstado() == EstadoPlanPago.PAGADO) {
            throw new BadRequestException("Esta cuota ya se encuentra totalmente pagada");
        }

        // Calculate late fee (recargo_mora) if overdue
        BigDecimal recargoMora = BigDecimal.ZERO;
        if (dto.getFechaPago().isAfter(planPago.getFechaVencimiento())) {
            long diasRetraso = ChronoUnit.DAYS.between(planPago.getFechaVencimiento(), dto.getFechaPago());
            BigDecimal baseAmount = planPago.getMontoCapital().add(planPago.getMontoInteres());
            // 0.1% daily penalty of the base installment amount
            recargoMora = baseAmount.multiply(BigDecimal.valueOf(diasRetraso))
                    .multiply(BigDecimal.valueOf(0.001))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Calculate pending balance of the base installment
        List<Abono> existingAbonos = abonoRepository.findByPlanPagoId(planPago.getId());
        BigDecimal totalPaidSoFar = existingAbonos.stream()
                .map(Abono::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal baseCuota = planPago.getMontoCapital().add(planPago.getMontoInteres());
        BigDecimal pendingBase = baseCuota.subtract(totalPaidSoFar);

        // Maximum allowable payment (base remaining + the late fee for this specific payment)
        BigDecimal maxAllowable = pendingBase.add(recargoMora);

        if (dto.getMonto().compareTo(maxAllowable) > 0) {
            throw new BadRequestException("El monto del abono (" + dto.getMonto() + ") excede el saldo pendiente de la cuota incluyendo recargos (" + maxAllowable + ")");
        }

        // Save the abono transaction
        Abono abono = Abono.builder()
                .monto(dto.getMonto())
                .fechaPago(dto.getFechaPago())
                .recargoMora(recargoMora)
                .planPago(planPago)
                .build();

        Abono savedAbono = abonoRepository.save(abono);

        // Re-calculate total paid including the current abono
        BigDecimal totalPaidUpdated = totalPaidSoFar.add(dto.getMonto());

        // Note: the payment covers the late fee first, then base installment.
        // So the installment is fully paid if: totalPaidUpdated >= baseCuota + accumulatedRecargoMora
        // To be precise and user-friendly, let's sum all recorded recargos:
        BigDecimal totalRecargos = existingAbonos.stream()
                .map(Abono::getRecargoMora)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(recargoMora);

        BigDecimal totalRequired = baseCuota.add(totalRecargos);

        if (totalPaidUpdated.compareTo(totalRequired) >= 0) {
            planPago.setEstado(EstadoPlanPago.PAGADO);
            planPagoRepository.save(planPago);

            // Check if all other installments in the Prestamo are paid
            Prestamo prestamo = planPago.getPrestamo();
            boolean allPaid = planPagoRepository.findByPrestamoIdOrderByNumeroCuotaAsc(prestamo.getId())
                    .stream()
                    .allMatch(p -> p.getEstado() == EstadoPlanPago.PAGADO);

            if (allPaid) {
                prestamo.setEstado(EstadoPrestamo.PAGADO);
                prestamoRepository.save(prestamo);
            }
        }

        return convertToResponseDto(savedAbono);
    }

    public List<AbonoResponseDto> findByPlanPagoId(int planPagoId) {
        if (!planPagoRepository.existsById(planPagoId)) {
            throw new NotFoundException("Plan de pago no encontrado con ID: " + planPagoId);
        }
        return abonoRepository.findByPlanPagoId(planPagoId).stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public AbonoResponseDto convertToResponseDto(Abono abono) {
        return AbonoResponseDto.builder()
                .id(abono.getId())
                .monto(abono.getMonto())
                .fechaPago(abono.getFechaPago())
                .recargoMora(abono.getRecargoMora())
                .planPagoId(abono.getPlanPago().getId())
                .build();
    }
}
