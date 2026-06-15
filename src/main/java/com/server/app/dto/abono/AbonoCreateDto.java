package com.server.app.dto.abono;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbonoCreateDto {

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que 0")
    private BigDecimal monto;

    @NotNull(message = "La fecha de pago es requerida")
    private LocalDate fechaPago;

    @NotNull(message = "El ID del plan de pago es requerido")
    private Integer planPagoId;
}
