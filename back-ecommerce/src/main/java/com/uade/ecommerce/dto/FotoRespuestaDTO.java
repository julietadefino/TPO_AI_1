package com.uade.ecommerce.dto;

import com.uade.ecommerce.model.Foto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Foto tal como se devuelve al cliente. Expone el id para que el
 * front pueda borrar una foto puntual con
 * DELETE /api/productos/{id}/fotos/{fotoId}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FotoRespuestaDTO {

    private Long id;
    private String url;

    public static FotoRespuestaDTO fromEntity(Foto foto) {
        return new FotoRespuestaDTO(
                foto.getId(),
                foto.getUrl()
        );
    }
}
