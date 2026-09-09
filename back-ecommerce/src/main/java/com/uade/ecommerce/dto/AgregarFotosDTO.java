package com.uade.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Cuerpo de POST /api/productos/{id}/fotos.
 * Las fotos son URLs de imágenes ya alojadas, no archivos binarios.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgregarFotosDTO {

    private Long usuarioId;
    private List<String> fotos;
}
