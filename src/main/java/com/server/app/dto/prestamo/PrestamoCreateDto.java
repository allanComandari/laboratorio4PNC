package com.server.app.dto.prestamo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoCreateDto {

    @NotNull(message = "El capital solicitado es requerido")
    @DecimalMin(value = "0.01", message = "El capital solicitado debe ser mayor que 0")
    private BigDecimal capitalSolicitado;

    @NotNull(message = "La tasa de interés anual es requerida")
    @DecimalMin(value = "0.0", message = "La tasa de interés anual debe ser mayor o igual que 0")
    private BigDecimal tasaInteresAnual;

    @NotNull(message = "El plazo en meses es requerido")
    @Min(value = 1, message = "El plazo debe ser de al menos 1 mes")
    private Integer plazoMeses;

    @NotNull(message = "El ID de usuario es requerido")
    private Integer usuarioId;
}
