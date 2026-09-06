package com.uade.ecommerce.dto;

import com.uade.ecommerce.model.Carrito;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarritoRespuestaDTO {

    private Long id;
    private Long usuarioId;
    private List<ItemCarritoRespuestaDTO> items;
    private Double total;

    public static CarritoRespuestaDTO fromEntity(
            Carrito carrito
    ) {
        List<ItemCarritoRespuestaDTO> items =
                carrito.getItems() == null
                        ? List.of()
                        : carrito.getItems()
                                .stream()
                                .map(ItemCarritoRespuestaDTO::fromEntity)
                                .toList();

        double total = items.stream()
                .mapToDouble(
                        ItemCarritoRespuestaDTO::getSubtotal
                )
                .sum();

        return new CarritoRespuestaDTO(
                carrito.getId(),
                carrito.getUsuario().getId(),
                items,
                total
        );
    }
}