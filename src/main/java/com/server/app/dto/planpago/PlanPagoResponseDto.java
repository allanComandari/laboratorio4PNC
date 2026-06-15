package com.server.app.dto.planpago;

import com.server.app.entities.EstadoPlanPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanPagoResponseDto {
    private Integer id;
    private Integer numeroCuota;
    private BigDecimal montoCapital;
    private BigDecimal montoInteres;
    private BigDecimal totalCuota;
    private LocalDate fechaVencimiento;
    private EstadoPlanPago estado;
    private Integer prestamoId;
}
