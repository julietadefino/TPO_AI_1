package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.model.Carrito;
import com.uade.ecommerce.model.ItemCarrito;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.repository.CarritoRepository;
import com.uade.ecommerce.repository.ItemCarritoRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public Carrito getByUsuario(Long usuarioId) {
        return buscarCarrito(usuarioId);
    }

    public Carrito agregarProducto(
            Long usuarioId,
            Long productoId,
            Integer cantidad
    ) {
        if (productoId == null) {
            throw ApiException.badRequest(
                    "El ID del producto es obligatorio"
            );
        }

        if (cantidad == null || cantidad <= 0) {
            throw ApiException.badRequest(
                    "La cantidad debe ser mayor que cero"
            );
        }

        Carrito carrito = buscarCarrito(usuarioId);

        Producto producto = productoRepository
                .findById(productoId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Producto no encontrado"
                        )
                );

        if (producto.getStock() <= 0) {
            throw ApiException.badRequest(
                    "El producto no tiene stock"
            );
        }

        if (carrito.getItems() == null) {
            carrito.setItems(new ArrayList<>());
        }

        ItemCarrito itemExistente =
                itemCarritoRepository
                        .findByCarritoIdAndProductoId(
                                carrito.getId(),
                                productoId
                        )
                        .orElse(null);

        if (itemExistente != null) {
            int nuevaCantidad =
                    itemExistente.getCantidad() + cantidad;

            validarStock(producto, nuevaCantidad);

            itemExistente.setCantidad(nuevaCantidad);
            itemCarritoRepository.save(itemExistente);
        } else {
            validarStock(producto, cantidad);

            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(cantidad);

            carrito.getItems().add(nuevoItem);
        }

        return carritoRepository.save(carrito);
    }

    public Carrito eliminarItem(
            Long usuarioId,
            Long itemId
    ) {
        if (itemId == null) {
            throw ApiException.badRequest(
                    "El ID del ítem es obligatorio"
            );
        }

        Carrito carrito = buscarCarrito(usuarioId);

        ItemCarrito item = itemCarritoRepository
                .findById(itemId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Ítem del carrito no encontrado"
                        )
                );

        if (!item.getCarrito().getId()
                .equals(carrito.getId())) {
            throw ApiException.forbidden(
                    "El ítem no pertenece al carrito del usuario"
            );
        }

        carrito.getItems().remove(item);
        itemCarritoRepository.delete(item);

        return carritoRepository.save(carrito);
    }

    public Carrito vaciar(Long usuarioId) {
        Carrito carrito = buscarCarrito(usuarioId);

        if (carrito.getItems() != null) {
            carrito.getItems().clear();
        }

        return carritoRepository.save(carrito);
    }

        public BigDecimal checkout(Long usuarioId) {
        Carrito carrito = buscarCarrito(usuarioId);
        List<ItemCarrito> items = carrito.getItems();

        if (items == null || items.isEmpty()) {
            throw ApiException.badRequest(
                    "No se puede realizar el checkout " +
                    "porque el carrito está vacío"
            );
        }

        /*
         * Primero se valida el stock de todos los productos.
         * Si uno no tiene stock suficiente, la transacción
         * se cancela sin modificar ningún producto.
         */
        for (ItemCarrito item : items) {
            validarStock(
                    item.getProducto(),
                    item.getCantidad()
            );
        }

        BigDecimal total = BigDecimal.ZERO;

        for (ItemCarrito item : items) {
            Producto producto = item.getProducto();

            total = total.add(
                    producto.getPrecio()
                            .multiply(BigDecimal.valueOf(item.getCantidad()))
            );

            producto.setStock(
                    producto.getStock()
                    - item.getCantidad()
            );

            productoRepository.save(producto);
        }

        carrito.getItems().clear();
        carritoRepository.save(carrito);

        return total;
    }

    private Carrito buscarCarrito(Long usuarioId) {
        if (usuarioId == null) {
            throw ApiException.badRequest(
                    "El ID del usuario es obligatorio"
            );
        }

        return carritoRepository
                .findByUsuarioId(usuarioId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Carrito no encontrado para el usuario"
                        )
                );
    }

    private void validarStock(
            Producto producto,
            Integer cantidad
    ) {
        if (producto.getStock() < cantidad) {
            throw ApiException.badRequest(
                    "Stock insuficiente para el producto: "
                    + producto.getNombre()
            );
        }
    }
}