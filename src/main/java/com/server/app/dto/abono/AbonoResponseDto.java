package com.server.app.dto.abono;

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
public class AbonoResponseDto {
    private Integer id;
    private BigDecimal monto;
    private LocalDate fechaPago;
    private BigDecimal recargoMora;
    private Integer planPagoId;
}
