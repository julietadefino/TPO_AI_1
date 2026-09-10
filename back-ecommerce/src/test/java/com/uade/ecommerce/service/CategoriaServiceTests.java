package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.model.Categoria;
import com.uade.ecommerce.repository.CategoriaRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTests {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria crearCategoria(Long id, String nombre) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        return categoria;
    }

    @Test
    void devuelveLasCategoriasEnOrdenAlfabetico() {
        List<Categoria> categorias = List.of(
                crearCategoria(1L, "Electrónica"),
                crearCategoria(2L, "Hogar"),
                crearCategoria(3L, "Ropa")
        );

        when(categoriaRepository.findAllByOrderByNombreAsc())
                .thenReturn(categorias);

        List<Categoria> resultado = categoriaService.getAll();

        assertEquals(
                List.of("Electrónica", "Hogar", "Ropa"),
                resultado.stream().map(Categoria::getNombre).toList()
        );
        verify(categoriaRepository).findAllByOrderByNombreAsc();
    }

    @Test
    void creaUnaCategoriaNormalizandoElNombre() {
        Categoria nueva = crearCategoria(99L, "  Hogar  ");

        when(categoriaRepository.existsByNombreIgnoreCase("Hogar"))
                .thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Categoria resultado = categoriaService.crear(nueva);

        assertNull(resultado.getId());
        assertEquals("Hogar", resultado.getNombre());
        verify(categoriaRepository)
                .existsByNombreIgnoreCase("Hogar");
        verify(categoriaRepository).save(nueva);
    }

    @Test
    void rechazaUnNombreVacio() {
        Categoria nueva = crearCategoria(null, "   ");

        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> categoriaService.crear(nueva)
        );

        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatus());
        assertEquals(
                "El nombre de la categoría es obligatorio",
                excepcion.getMessage()
        );
        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    @Test
    void rechazaUnaCategoriaRepetidaSinImportarMayusculas() {
        Categoria nueva = crearCategoria(null, "electrónica");

        when(categoriaRepository
                .existsByNombreIgnoreCase("electrónica"))
                .thenReturn(true);

        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> categoriaService.crear(nueva)
        );

        assertEquals(HttpStatus.CONFLICT, excepcion.getStatus());
        assertEquals(
                "Ya existe una categoría con ese nombre",
                excepcion.getMessage()
        );
        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    @Test
    void actualizaUnaCategoriaExistente() {
        Categoria existente =
                crearCategoria(1L, "Tecnología");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(existente));
        when(categoriaRepository
                .existsByNombreIgnoreCase("Electrónica"))
                .thenReturn(false);
        when(categoriaRepository.save(existente))
                .thenReturn(existente);

        Categoria resultado = categoriaService.actualizar(
                1L,
                "  Electrónica  "
        );

        assertEquals("Electrónica", resultado.getNombre());
        verify(categoriaRepository).save(existente);
    }

    @Test
    void rechazaUnNombreRepetidoAlActualizar() {
        Categoria existente =
                crearCategoria(1L, "Electrónica");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(existente));
        when(categoriaRepository.existsByNombreIgnoreCase("Ropa"))
                .thenReturn(true);

        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> categoriaService.actualizar(1L, "Ropa")
        );

        assertEquals(HttpStatus.CONFLICT, excepcion.getStatus());
        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    @Test
    void rechazaIdentificadoresInvalidos() {
        ApiException idCero = assertThrows(
                ApiException.class,
                () -> categoriaService.getById(0L)
        );

        ApiException idNegativo = assertThrows(
                ApiException.class,
                () -> categoriaService.actualizar(-1L, "Hogar")
        );

        ApiException idNulo = assertThrows(
                ApiException.class,
                () -> categoriaService.delete(null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, idCero.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, idNegativo.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, idNulo.getStatus());
        assertEquals(
                "El identificador de la categoría debe ser mayor a cero",
                idCero.getMessage()
        );
        verifyNoInteractions(
                categoriaRepository,
                productoRepository
        );
    }

    @Test
    void eliminaUnaCategoriaSinProductosAsociados() {
        Categoria categoria =
                crearCategoria(1L, "Hogar");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));
        when(productoRepository.existsByCategoriaId(1L))
                .thenReturn(false);

        categoriaService.delete(1L);

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void impideEliminarUnaCategoriaConProductosAsociados() {
        Categoria categoria =
                crearCategoria(1L, "Electrónica");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));
        when(productoRepository.existsByCategoriaId(1L))
                .thenReturn(true);

        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> categoriaService.delete(1L)
        );

        assertEquals(HttpStatus.CONFLICT, excepcion.getStatus());
        assertEquals(
                "No se puede eliminar la categoría porque tiene productos asociados",
                excepcion.getMessage()
        );
        verify(categoriaRepository, never()).delete(categoria);
    }

    @Test
    void devuelveErrorCuandoLaCategoriaNoExiste() {
        when(categoriaRepository.findById(50L))
                .thenReturn(Optional.empty());

        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> categoriaService.actualizar(50L, "Hogar")
        );

        assertEquals(HttpStatus.NOT_FOUND, excepcion.getStatus());
    }
}