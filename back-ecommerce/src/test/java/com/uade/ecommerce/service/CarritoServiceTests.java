package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.model.Carrito;
import com.uade.ecommerce.model.ItemCarrito;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.repository.CarritoRepository;
import com.uade.ecommerce.repository.ItemCarritoRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTests {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CarritoService carritoService;

    @Test
    void realizaCheckoutConTotalPreciso() {
        Producto producto = producto("Producto", "19.99", 5);
        ItemCarrito item = item(producto, 2);
        Carrito carrito = carrito(List.of(item));
        when(carritoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(carrito));

        BigDecimal total = carritoService.checkout(1L);

        assertEquals(new BigDecimal("39.98"), total);
        assertEquals(3, producto.getStock());
        assertEquals(0, carrito.getItems().size());
        verify(productoRepository).save(producto);
        verify(carritoRepository).save(carrito);
    }

    @Test
    void rechazaCheckoutConCarritoVacio() {
        Carrito carrito = carrito(List.of());
        when(carritoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(carrito));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> carritoService.checkout(1L)
        );

        assertEquals("No se puede realizar el checkout porque el carrito está vacío",
                exception.getMessage());
        verify(productoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(carritoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void validaTodoElStockAntesDeDescontar() {
        Producto disponible = producto("Disponible", "10.00", 5);
        Producto sinStock = producto("Sin stock", "20.00", 1);
        ItemCarrito primerItem = item(disponible, 2);
        ItemCarrito segundoItem = item(sinStock, 2);
        Carrito carrito = carrito(List.of(primerItem, segundoItem));
        when(carritoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(carrito));

        assertThrows(ApiException.class, () -> carritoService.checkout(1L));

        assertEquals(5, disponible.getStock());
        assertEquals(1, sinStock.getStock());
        assertEquals(2, carrito.getItems().size());
        verify(productoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(carritoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private Producto producto(String nombre, String precio, int stock) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(new BigDecimal(precio));
        producto.setStock(stock);
        return producto;
    }

    private ItemCarrito item(Producto producto, int cantidad) {
        ItemCarrito item = new ItemCarrito();
        item.setProducto(producto);
        item.setCantidad(cantidad);
        return item;
    }

    private Carrito carrito(List<ItemCarrito> items) {
        Carrito carrito = new Carrito();
        carrito.setItems(new java.util.ArrayList<>(items));
        return carrito;
    }
}