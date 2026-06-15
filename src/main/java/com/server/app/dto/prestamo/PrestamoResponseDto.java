package com.server.app.dto.prestamo;

import com.server.app.dto.planpago.PlanPagoResponseDto;
import com.server.app.entities.EstadoPrestamo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestamoResponseDto {
    private Integer id;
    private BigDecimal capitalSolicitado;
    private BigDecimal tasaInteresAnual;
    private Integer plazoMeses;
    private EstadoPrestamo estado;
    private Integer usuarioId;
    private List<PlanPagoResponseDto> planPagos;
}
