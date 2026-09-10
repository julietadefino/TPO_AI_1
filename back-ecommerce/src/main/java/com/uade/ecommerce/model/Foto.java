package com.uade.ecommerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Foto de un producto.
 *
 * Se almacena la URL de una imagen alojada en un servicio externo,
 * no el archivo binario. Ver docs/FOTOS.md.
 */
@Entity
@Table(name = "fotos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Foto {

    /** Longitud máxima de la URL, compartida por la columna y la validación. */
    public static final int MAX_LONGITUD_URL = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_LONGITUD_URL)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
}
