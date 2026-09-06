package com.uade.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgregarItemCarritoDTO {

    private Long usuarioId;
    private Long productoId;
    private Integer cantidad;
}