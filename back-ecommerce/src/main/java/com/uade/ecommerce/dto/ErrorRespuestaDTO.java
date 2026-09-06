package com.uade.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorRespuestaDTO {

    private LocalDateTime fecha;
    private Integer estado;
    private String error;
    private String mensaje;
    private String ruta;
}