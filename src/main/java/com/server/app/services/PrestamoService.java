package com.server.app.services;

import com.server.app.dto.planpago.PlanPagoResponseDto;
import com.server.app.dto.prestamo.PrestamoCreateDto;
import com.server.app.dto.prestamo.PrestamoResponseDto;
import com.server.app.entities.EstadoPlanPago;
import com.server.app.entities.EstadoPrestamo;
import com.server.app.entities.PlanPago;
import com.server.app.entities.Prestamo;
import com.server.app.entities.User;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.UserRepository;
import com.server.app.repositories.PrestamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final UserRepository userRepository;

    @Transactional
    public PrestamoResponseDto create(PrestamoCreateDto dto) {
        User user = userRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + dto.getUsuarioId()));

        // Create initial Prestamo entity
        Prestamo prestamo = Prestamo.builder()
                .capitalSolicitado(dto.getCapitalSolicitado())
                .tasaInteresAnual(dto.getTasaInteresAnual())
                .plazoMeses(dto.getPlazoMeses())
                .estado(EstadoPrestamo.APROBADO) // default to APROBADO so payments can be made
                .user(user)
                .build();

        // Calculate and generate payment schedule (French system)
        generateAmortizationPlan(prestamo);

        // Save prestamo (will cascade save the planPagos due to CascadeType.ALL)
        Prestamo savedPrestamo = prestamoRepository.save(prestamo);

        return convertToResponseDto(savedPrestamo);
    }

    public List<PrestamoResponseDto> findAll() {
        return prestamoRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public PrestamoResponseDto findById(int id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Préstamo no encontrado con ID: " + id));
        return convertToResponseDto(prestamo);
    }

    public List<PrestamoResponseDto> findByUserId(int userId) {
        return prestamoRepository.findByUserId(userId).stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Transactional
    public void delete(int id) {
        if (!prestamoRepository.existsById(id)) {
            throw new NotFoundException("Préstamo no encontrado con ID: " + id);
        }
        prestamoRepository.deleteById(id);
    }

    private void generateAmortizationPlan(Prestamo prestamo) {
        BigDecimal capital = prestamo.getCapitalSolicitado();
        BigDecimal tasaAnual = prestamo.getTasaInteresAnual();
        int plazo = prestamo.getPlazoMeses();

        // Monthly Interest Rate: r = (tasaAnual / 100) / 12
        BigDecimal r = tasaAnual.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal cuotaMensual;
        if (r.compareTo(BigDecimal.ZERO) == 0) {
            cuotaMensual = capital.divide(BigDecimal.valueOf(plazo), 2, RoundingMode.HALF_UP);
        } else {
            // A = P * [r * (1+r)^n] / [(1+r)^n - 1]
            BigDecimal unoMasR = BigDecimal.ONE.add(r);
            BigDecimal unoMasRAPlazo = unoMasR.pow(plazo);
            
            BigDecimal numerador = r.multiply(unoMasRAPlazo);
            BigDecimal denominador = unoMasRAPlazo.subtract(BigDecimal.ONE);
            
            BigDecimal factor = numerador.divide(denominador, 10, RoundingMode.HALF_UP);
            cuotaMensual = capital.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal saldoPendiente = capital;
        LocalDate fechaInicio = LocalDate.now();

        List<PlanPago> planes = new ArrayList<>();
        for (int k = 1; k <= plazo; k++) {
            BigDecimal interes;
            BigDecimal capitalCuota;
            
            if (k == plazo) {
                // Last installment: absorbs any rounding differences
                interes = saldoPendiente.multiply(r).setScale(2, RoundingMode.HALF_UP);
                capitalCuota = saldoPendiente;
                saldoPendiente = BigDecimal.ZERO;
            } else {
                interes = saldoPendiente.multiply(r).setScale(2, RoundingMode.HALF_UP);
                capitalCuota = cuotaMensual.subtract(interes).setScale(2, RoundingMode.HALF_UP);
                saldoPendiente = saldoPendiente.subtract(capitalCuota).setScale(2, RoundingMode.HALF_UP);
            }

            PlanPago plan = PlanPago.builder()
                    .numeroCuota(k)
                    .montoCapital(capitalCuota)
                    .montoInteres(interes)
                    .fechaVencimiento(fechaInicio.plusMonths(k))
                    .estado(EstadoPlanPago.PENDIENTE)
                    .prestamo(prestamo)
                    .build();
            planes.add(plan);
        }

        prestamo.setPlanPagos(planes);
    }

    public PrestamoResponseDto convertToResponseDto(Prestamo prestamo) {
        List<PlanPagoResponseDto> planDtos = prestamo.getPlanPagos().stream()
                .map(this::convertToPlanPagoDto)
                .toList();

        return PrestamoResponseDto.builder()
                .id(prestamo.getId())
                .capitalSolicitado(prestamo.getCapitalSolicitado())
                .tasaInteresAnual(prestamo.getTasaInteresAnual())
                .plazoMeses(prestamo.getPlazoMeses())
                .estado(prestamo.getEstado())
                .usuarioId(prestamo.getUser().getId())
                .planPagos(planDtos)
                .build();
    }

    private PlanPagoResponseDto convertToPlanPagoDto(PlanPago plan) {
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
