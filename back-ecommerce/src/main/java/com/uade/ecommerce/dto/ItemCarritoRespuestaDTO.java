package com.uade.ecommerce.dto;

import com.uade.ecommerce.model.ItemCarrito;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarritoRespuestaDTO {

    private Long id;
    private Long productoId;
    private String producto;
    private Double precioUnitario;
    private Integer cantidad;
    private Double subtotal;

    public static ItemCarritoRespuestaDTO fromEntity(
            ItemCarrito item
    ) {
        double subtotal =
                item.getProducto().getPrecio()
                * item.getCantidad();

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