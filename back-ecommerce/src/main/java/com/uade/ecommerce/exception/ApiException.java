package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(
            HttpStatus status,
            String mensaje
    ) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException badRequest(String mensaje) {
        return new ApiException(
                HttpStatus.BAD_REQUEST,
                mensaje
        );
    }

    public static ApiException notFound(String mensaje) {
        return new ApiException(
                HttpStatus.NOT_FOUND,
                mensaje
        );
    }

    public static ApiException conflict(String mensaje) {
        return new ApiException(
                HttpStatus.CONFLICT,
                mensaje
        );
    }

    public static ApiException forbidden(String mensaje) {
        return new ApiException(
                HttpStatus.FORBIDDEN,
                mensaje
        );
    }

    public static ApiException unauthorized(String mensaje) {
        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                mensaje
        );
    }
}