package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.LoginDTO;
import com.uade.ecommerce.dto.UsuarioRegistroDTO;
import com.uade.ecommerce.dto.UsuarioRespuestaDTO;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uade.ecommerce.exception.ApiException;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioRespuestaDTO> getAll() {
        return usuarioService.getAll()
                .stream()
                .map(UsuarioRespuestaDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public UsuarioRespuestaDTO getById(
            @PathVariable Long id
    ) {
        Usuario usuario = usuarioService.getById(id)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Usuario no encontrado"
                        )
                );

        return UsuarioRespuestaDTO.fromEntity(usuario);
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioRespuestaDTO> registrar(
            @RequestBody UsuarioRegistroDTO datos
    ) {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(datos.getNombreUsuario());
        usuario.setMail(datos.getMail());
        usuario.setContrasenia(datos.getContrasenia());
        usuario.setNombre(datos.getNombre());
        usuario.setApellido(datos.getApellido());

        Usuario registrado =
                usuarioService.registrar(usuario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UsuarioRespuestaDTO.fromEntity(registrado));
    }

    @PostMapping("/login")
    public UsuarioRespuestaDTO login(
            @RequestBody LoginDTO datos
    ) {
        Usuario usuario = usuarioService.login(
                datos.getMail(),
                datos.getContrasenia()
        );

        return UsuarioRespuestaDTO.fromEntity(usuario);
    }
}