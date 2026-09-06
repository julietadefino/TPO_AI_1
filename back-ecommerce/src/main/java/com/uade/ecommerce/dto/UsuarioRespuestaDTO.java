package com.uade.ecommerce.dto;

import com.uade.ecommerce.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRespuestaDTO {

    private Long id;
    private String nombreUsuario;
    private String mail;
    private String nombre;
    private String apellido;

    public static UsuarioRespuestaDTO fromEntity(Usuario usuario) {
        return new UsuarioRespuestaDTO(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getMail(),
                usuario.getNombre(),
                usuario.getApellido()
        );
    }
}