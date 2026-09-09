package com.uade.ecommerce.dto;

import com.uade.ecommerce.model.ItemCarrito;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarritoRespuestaDTO {

    private Long id;
    private Long productoId;
    private String producto;
        private BigDecimal precioUnitario;
    private Integer cantidad;
        private BigDecimal subtotal;

    public static ItemCarritoRespuestaDTO fromEntity(
            ItemCarrito item
    ) {
        BigDecimal subtotal = item.getProducto().getPrecio()
                .multiply(BigDecimal.valueOf(item.getCantidad()));

        return new ItemCarritoRespuestaDTO(
                item.getId(),
                item.getProducto().getId(),
                item.getProducto().getNombre(),
                item.getProducto().getPrecio(),
                item.getCantidad(),
                subtotal
        );
    }
}