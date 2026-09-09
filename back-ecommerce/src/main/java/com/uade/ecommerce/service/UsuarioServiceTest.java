package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.repository.CarritoRepository;
import com.uade.ecommerce.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CarritoRepository carritoRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UsuarioService usuarioService;
    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setNombreUsuario("nachoscervino");
        usuarioPrueba.setMail("nacho@ejemplo.com");
        usuarioPrueba.setContrasenia("Nacho123!");
        usuarioPrueba.setNombre("Nacho");
        usuarioPrueba.setApellido("Scervino");
    }

    @Test
    void registrar_debeCifrarContraseniaYGuardarUsuario() {
        when(usuarioRepository.existsByMail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByNombreUsuario(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Nacho123!")).thenReturn("$2a$10$EncodedPasswordHash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario usuarioRegistrado = usuarioService.registrar(usuarioPrueba);

        assertNotNull(usuarioRegistrado);
        assertEquals("$2a$10$EncodedPasswordHash", usuarioRegistrado.getContrasenia());
        verify(passwordEncoder, times(1)).encode("Nacho123!");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void login_conCredencialesCorrectas_debeRetornarUsuario() {
        Usuario usuarioEnBD = new Usuario();
        usuarioEnBD.setMail("nacho@ejemplo.com");
        usuarioEnBD.setContrasenia("$2a$10$EncodedPasswordHash");

        when(usuarioRepository.findByMail("nacho@ejemplo.com")).thenReturn(Optional.of(usuarioEnBD));
        when(passwordEncoder.matches("Nacho123!", "$2a$10$EncodedPasswordHash")).thenReturn(true);

        Usuario resultado = usuarioService.login("nacho@ejemplo.com", "Nacho123!");

        assertNotNull(resultado);
        assertEquals("nacho@ejemplo.com", resultado.getMail());
        verify(passwordEncoder, times(1)).matches("Nacho123!", "$2a$10$EncodedPasswordHash");
    }

    @Test
    void login_conContraseniaIncorrecta_debeLanzarExcepcion() {
        Usuario usuarioEnBD = new Usuario();
        usuarioEnBD.setMail("nacho@ejemplo.com");
        usuarioEnBD.setContrasenia("$2a$10$EncodedPasswordHash");

        when(usuarioRepository.findByMail("nacho@ejemplo.com")).thenReturn(Optional.of(usuarioEnBD));
        when(passwordEncoder.matches("ContraseniaIncorrecta", "$2a$10$EncodedPasswordHash")).thenReturn(false);

        ApiException excepcion = assertThrows(ApiException.class, () -> {
            usuarioService.login("nacho@ejemplo.com", "ContraseniaIncorrecta");
        });

        assertEquals("Usuario o contraseña incorrectos", excepcion.getMessage());
        verify(passwordEncoder, times(1)).matches("ContraseniaIncorrecta", "$2a$10$EncodedPasswordHash");
    }
}
