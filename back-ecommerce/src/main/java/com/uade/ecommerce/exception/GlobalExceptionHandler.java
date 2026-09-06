package com.uade.ecommerce.exception;

import com.uade.ecommerce.dto.ErrorRespuestaDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorRespuestaDTO> manejarApiException(
            ApiException exception,
            HttpServletRequest request
    ) {
        ErrorRespuestaDTO respuesta =
                new ErrorRespuestaDTO(
                        LocalDateTime.now(),
                        exception.getStatus().value(),
                        exception.getStatus().getReasonPhrase(),
                        exception.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(exception.getStatus())
                .body(respuesta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuestaDTO> manejarErrorInesperado(
            Exception exception,
            HttpServletRequest request
    ) {
        ErrorRespuestaDTO respuesta =
                new ErrorRespuestaDTO(
                        LocalDateTime.now(),
                        500,
                        "Internal Server Error",
                        "Ocurrió un error inesperado",
                        request.getRequestURI()
                );

        return ResponseEntity
                .internalServerError()
                .body(respuesta);
    }
}