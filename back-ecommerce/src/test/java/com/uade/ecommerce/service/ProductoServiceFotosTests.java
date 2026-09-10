package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.model.Categoria;
import com.uade.ecommerce.model.Foto;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.repository.CategoriaRepository;
import com.uade.ecommerce.repository.ItemCarritoRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import com.uade.ecommerce.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;

/**
 * Pruebas de la gestión de fotos de un producto: validación de URLs,
 * alta y baja individual, y sincronía de la relación con Producto.
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceFotosTests {

    private static final Long ID_PRODUCTO = 1L;
    private static final Long ID_DUENIO = 10L;
    private static final Long ID_OTRO_USUARIO = 99L;
    private static final Long ID_CATEGORIA = 5L;

    private static final String URL_UNO =
            "https://cdn.ejemplo.com/remera-frente.jpg";
    private static final String URL_DOS =
            "https://cdn.ejemplo.com/remera-espalda.jpg";

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Usuario duenio;
    private Categoria categoria;

    @BeforeEach
    void prepararDatos() {
        duenio = new Usuario();
        duenio.setId(ID_DUENIO);
        duenio.setNombreUsuario("juan");

        categoria = new Categoria();
        categoria.setId(ID_CATEGORIA);
        categoria.setNombre("Indumentaria");
    }

    // ---------- Validación de URLs ----------

    @Test
    void creaElProductoCuandoLaUrlEsValida() {
        prepararAltaDeProducto();

        Producto guardado = productoService.crear(
                productoNuevo(),
                ID_CATEGORIA,
                ID_DUENIO,
                List.of(URL_UNO)
        );

        assertEquals(1, guardado.getFotos().size());
        assertEquals(URL_UNO, guardado.getFotos().get(0).getUrl());
    }

    @Test
    void normalizaLosEspaciosAlrededorDeLaUrl() {
        prepararAltaDeProducto();

        Producto guardado = productoService.crear(
                productoNuevo(),
                ID_CATEGORIA,
                ID_DUENIO,
                List.of("   " + URL_UNO + "   ")
        );

        assertEquals(URL_UNO, guardado.getFotos().get(0).getUrl());
    }

    @Test
    void rechazaElProductoSinLaListaDeFotos() {
        ApiException error = assertThrows(
                ApiException.class,
                () -> productoService.crear(
                        productoNuevo(),
                        ID_CATEGORIA,
                        ID_DUENIO,
                        null
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals(
                "El producto debe tener al menos una foto",
                error.getMessage()
        );
    }

    @Test
    void rechazaElProductoConListaDeFotosVacia() {
        ApiException error = assertThrows(
                ApiException.class,
                () -> productoService.crear(
                        productoNuevo(),
                        ID_CATEGORIA,
                        ID_DUENIO,
                        List.of()
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    void rechazaLaUrlEnBlanco() {
        ApiException error = assertThrows(
                ApiException.class,
                () -> productoService.crear(
                        productoNuevo(),
                        ID_CATEGORIA,
                        ID_DUENIO,
                        Arrays.asList(URL_UNO, "   ")
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals(
                "La URL de la foto es obligatoria",
                error.getMessage()
        );
    }

    @Test
    void rechazaElTextoQueNoEsUnaUrl() {
        ApiException error = assertThrows(
                ApiException.class,
                () -> productoService.crear(
                        productoNuevo(),
                        ID_CATEGORIA,
                        ID_DUENIO,
                        List.of("no-es-una-url")
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertTrue(error.getMessage().contains("no es válida"));
    }

    @Test
    void rechazaLaUrlQueNoUsaHttpNiHttps() {
        ApiException error = assertThrows(
                ApiException.class,
                () -> productoService.crear(
                        productoNuevo(),
                        ID_CATEGORIA,
                        ID_DUENIO,
                        List.of("ftp://cdn.ejemplo.com/remera.jpg")
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    void rechazaLaUrlMasLargaQueElMaximoDeLaColumna() {
        String larga = "https://cdn.ejemplo.com/"
                + "a".repeat(Foto.MAX_LONGITUD_URL)
                + ".jpg";

        ApiException error = assertThrows(
                ApiException.class,
                () -> productoService.crear(
                        productoNuevo(),
                        ID_CATEGORIA,
                        ID_DUENIO,
                        List.of(larga)
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertTrue(error.getMessage().contains("500"));
    }

    @Test
    void rechazaLasFotosRepetidasEnElMismoPedido() {
        ApiException error = assertThrows(
                ApiException.class,
                () -> productoService.crear(
                        productoNuevo(),
                        ID_CATEGORIA,
                        ID_DUENIO,
                        List.of(URL_UNO, URL_UNO)
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertTrue(error.getMessage().contains("repetida"));
    }

    // ---------- Alta de fotos individuales ----------

    @Test
    void agregaLaFotoSinPisarLasQueYaTenia() {
        Producto producto = productoExistenteCon(URL_UNO);
        prepararBusquedaYGuardado(producto);

        Producto actualizado = productoService.agregarFotos(
                ID_PRODUCTO,
                ID_DUENIO,
                List.of(URL_DOS)
        );

        List<String> urls = actualizado.getFotos()
                .stream()
                .map(Foto::getUrl)
                .toList();

        assertEquals(List.of(URL_UNO, URL_DOS), urls);
    }

    @Test
    void dejaLaRelacionBidireccionalSincronizada() {
        Producto producto = productoExistenteCon(URL_UNO);
        prepararBusquedaYGuardado(producto);

        Producto actualizado = productoService.agregarFotos(
                ID_PRODUCTO,
                ID_DUENIO,
                List.of(URL_DOS)
        );

        Foto agregada = actualizado.getFotos().get(1);

        assertSame(actualizado, agregada.getProducto());
    }

    @Test
    void rechazaAgregarUnaFotoQueElProductoYaTiene() {
        Producto producto = productoExistenteCon(URL_UNO);
        prepararBusqueda(producto);

        ApiException error = assertThrows(
                ApiException.class,
                () -> productoService.agregarFotos(
                        ID_PRODUCTO,
                        ID_DUENIO,
                        List.of(URL_UNO)
                )
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    @Test
    void rechazaQueOtroUsuarioAgregueFotos() {
        Producto producto = productoExistenteCon(URL_UNO);
        prepararBusqueda(producto);

        ApiException error = assertThrows(
                ApiException.class,
                () -> productoService.agregarFotos(
                        ID_PRODUCTO,
                        ID_OTRO_USUARIO,
                        List.of(URL_DOS)
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    }

    // ---------- Baja de fotos individuales ----------

    @Test
    void eliminaSolamenteLaFotoIndicada() {
        Producto producto = productoExistenteCon(URL_UNO, URL_DOS);
        prepararBusquedaYGuardado(producto);

        Producto actualizado = productoService.eliminarFoto(
                ID_PRODUCTO,
                1L,
                ID_DUENIO
        );

        assertEquals(1, actualizado.getFotos().size());
        assertEquals(URL_DOS, actualizado.getFotos().get(0).getUrl());
    }

    @Test
    void rechazaEliminarLaUnicaFotoDelProducto() {
        Producto producto = productoExistenteCon(URL_UNO);
        prepararBusqueda(producto);

        ApiException error = assertThrows(
                ApiException.class,
                () -> productoService.eliminarFoto(
                        ID_PRODUCTO,
                        1L,
                        ID_DUENIO
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals(
                "El producto debe conservar al menos una foto",
                error.getMessage()
        );
        assertEquals(1, producto.getFotos().size());
    }

    @Test
    void devuelve404SiLaFotoNoEsDeEseProducto() {
        Producto producto = productoExistenteCon(URL_UNO, URL_DOS);
        prepararBusqueda(producto);

        ApiException error = assertThrows(
                ApiException.class,
                () -> productoService.eliminarFoto(
                        ID_PRODUCTO,
                        777L,
                        ID_DUENIO
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void rechazaQueOtroUsuarioElimineUnaFoto() {
        Producto producto = productoExistenteCon(URL_UNO, URL_DOS);
        prepararBusqueda(producto);

        ApiException error = assertThrows(
                ApiException.class,
                () -> productoService.eliminarFoto(
                        ID_PRODUCTO,
                        1L,
                        ID_OTRO_USUARIO
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        assertEquals(2, producto.getFotos().size());
    }

    // ---------- Ayudantes ----------

    private Producto productoNuevo() {
        Producto producto = new Producto();
        producto.setNombre("Remera");
        producto.setDescripcion("Remera de algodón");
        producto.setPrecio(BigDecimal.valueOf(1500.0));
        producto.setStock(10);

        return producto;
    }

    private Producto productoExistenteCon(String... urls) {
        Producto producto = productoNuevo();
        producto.setId(ID_PRODUCTO);
        producto.setUsuario(duenio);
        producto.setCategoria(categoria);
        producto.setFotos(new ArrayList<>());

        long id = 1L;

        for (String url : urls) {
            Foto foto = new Foto();
            foto.setId(id++);
            foto.setUrl(url);
            producto.agregarFoto(foto);
        }

        return producto;
    }

    private void prepararAltaDeProducto() {
        when(categoriaRepository.findById(ID_CATEGORIA))
                .thenReturn(Optional.of(categoria));
        when(usuarioRepository.findById(ID_DUENIO))
                .thenReturn(Optional.of(duenio));
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void prepararBusqueda(Producto producto) {
        when(productoRepository.findById(ID_PRODUCTO))
                .thenReturn(Optional.of(producto));
    }

    private void prepararBusquedaYGuardado(Producto producto) {
        prepararBusqueda(producto);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }
}
