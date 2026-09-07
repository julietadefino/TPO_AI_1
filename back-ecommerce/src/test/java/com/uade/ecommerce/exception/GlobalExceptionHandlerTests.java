package com.uade.ecommerce.exception;

import com.uade.ecommerce.dto.ErrorRespuestaDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void devuelve404CuandoNoSeEncuentraUnRecurso() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/productos/99");

        ResponseEntity<ErrorRespuestaDTO> response =
                handler.manejarApiException(
                        ApiException.notFound("Producto no encontrado"),
                        request
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getEstado());
        assertEquals("Producto no encontrado", response.getBody().getMensaje());
        assertEquals("/api/productos/99", response.getBody().getRuta());
    }

    @Test
    void devuelve400CuandoLaSolicitudEsInvalida() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/api/productos");

        ResponseEntity<ErrorRespuestaDTO> response =
                handler.manejarApiException(
                        ApiException.badRequest("El nombre es obligatorio"),
                        request
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getEstado());
        assertEquals("El nombre es obligatorio", response.getBody().getMensaje());
        assertEquals("/api/productos", response.getBody().getRuta());
    }
}
