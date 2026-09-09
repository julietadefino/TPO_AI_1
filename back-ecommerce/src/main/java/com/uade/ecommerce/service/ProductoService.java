package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.model.Categoria;
import com.uade.ecommerce.model.Foto;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.repository.CategoriaRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import com.uade.ecommerce.repository.UsuarioRepository;
import com.uade.ecommerce.repository.ItemCarritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    public List<Producto> getAll() {
        return productoRepository.findAllByOrderByNombreAsc();
    }

    public Optional<Producto> getById(Long id) {
        return productoRepository.findById(id);
    }

    public List<Producto> getByCategoria(Long categoriaId) {
        return productoRepository
                .findByCategoriaIdOrderByNombreAsc(categoriaId);
    }

    public Producto crear(
            Producto producto,
            Long categoriaId,
            Long usuarioId,
            List<String> urlsFotos
    ) {
        validarProducto(producto);
        validarIds(categoriaId, usuarioId);

        List<String> urls = validarUrls(urlsFotos);

        Categoria categoria = categoriaRepository
                .findById(categoriaId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Categoría no encontrada"
                        )
                );

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Usuario no encontrado"
                        )
                );

        producto.setId(null);
        producto.setNombre(producto.getNombre().trim());
        producto.setCategoria(categoria);
        producto.setUsuario(usuario);
        reemplazarFotos(producto, urls);

        return productoRepository.save(producto);
    }

    public Producto actualizar(
            Long productoId,
            Long usuarioId,
            Producto nuevosDatos,
            Long categoriaId,
            List<String> urlsFotos
    ) {
        Producto producto = buscarProducto(productoId);

        validarPropietario(producto, usuarioId);
        validarProducto(nuevosDatos);
        validarIds(categoriaId, usuarioId);

        List<String> urls = validarUrls(urlsFotos);

        Categoria categoria = categoriaRepository
                .findById(categoriaId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Categoría no encontrada"
                        )
                );

        producto.setNombre(
                nuevosDatos.getNombre().trim()
        );
        producto.setDescripcion(
                nuevosDatos.getDescripcion().trim()
        );
        producto.setPrecio(nuevosDatos.getPrecio());
        producto.setStock(nuevosDatos.getStock());
        producto.setCategoria(categoria);

        reemplazarFotos(producto, urls);

        return productoRepository.save(producto);
    }

    /**
     * Agrega fotos sin tocar el resto del producto.
     * Rechaza las URLs que el producto ya tiene cargadas.
     */
    public Producto agregarFotos(
            Long productoId,
            Long usuarioId,
            List<String> urlsFotos
    ) {
        Producto producto = buscarProducto(productoId);
        validarPropietario(producto, usuarioId);

        List<String> urls = validarUrls(urlsFotos);

        for (String url : urls) {
            if (tieneFotoConUrl(producto, url)) {
                throw ApiException.conflict(
                        "El producto ya tiene la foto " + url
                );
            }
        }

        for (String url : urls) {
            producto.agregarFoto(crearFoto(url));
        }

        return productoRepository.save(producto);
    }

    /**
     * Elimina una única foto del producto, siempre que no sea
     * la última: un producto no puede quedarse sin fotos.
     */
    public Producto eliminarFoto(
            Long productoId,
            Long fotoId,
            Long usuarioId
    ) {
        if (fotoId == null) {
            throw ApiException.badRequest(
                    "El ID de la foto es obligatorio"
            );
        }

        Producto producto = buscarProducto(productoId);
        validarPropietario(producto, usuarioId);

        Foto foto = producto.getFotos().stream()
                .filter(actual -> fotoId.equals(actual.getId()))
                .findFirst()
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Foto no encontrada en el producto"
                        )
                );

        if (producto.getFotos().size() == 1) {
            throw ApiException.badRequest(
                    "El producto debe conservar al menos una foto"
            );
        }

        producto.eliminarFoto(foto);

        return productoRepository.save(producto);
    }

    public Producto actualizarStock(
            Long productoId,
            Long usuarioId,
            Integer nuevoStock
    ) {
        if (nuevoStock == null || nuevoStock < 0) {
            throw ApiException.badRequest(
                    "El stock no puede ser negativo"
            );
        }

        Producto producto = buscarProducto(productoId);
        validarPropietario(producto, usuarioId);

        producto.setStock(nuevoStock);

        return productoRepository.save(producto);
    }

    public void eliminar(
            Long productoId,
            Long usuarioId
    ) {
        Producto producto = buscarProducto(productoId);
        validarPropietario(producto, usuarioId);

        itemCarritoRepository.deleteByProductoId(productoId);
        productoRepository.delete(producto);
    }

    private Producto buscarProducto(Long productoId) {
        if (productoId == null) {
            throw ApiException.badRequest(
                    "El ID del producto es obligatorio"
            );
        }

        return productoRepository.findById(productoId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Producto no encontrado"
                        )
                );
    }

    private void validarPropietario(
            Producto producto,
            Long usuarioId
    ) {
        if (usuarioId == null) {
            throw ApiException.badRequest(
                    "El ID del usuario es obligatorio"
            );
        }

        if (!producto.getUsuario().getId().equals(usuarioId)) {
            throw ApiException.forbidden(
                    "El usuario no puede modificar este producto"
            );
        }
    }

    private void validarProducto(Producto producto) {
        if (producto.getNombre() == null ||
                producto.getNombre().isBlank()) {
            throw ApiException.badRequest(
                    "El nombre del producto es obligatorio"
            );
        }

        if (producto.getDescripcion() == null ||
                producto.getDescripcion().isBlank()) {
            throw ApiException.badRequest(
                    "La descripción del producto es obligatoria"
            );
        }

        if (producto.getPrecio() == null ||
                producto.getPrecio() < 0) {
            throw ApiException.badRequest(
                    "El precio no puede ser negativo"
            );
        }

        if (producto.getStock() == null ||
                producto.getStock() < 0) {
            throw ApiException.badRequest(
                    "El stock no puede ser negativo"
            );
        }
    }

    private void validarIds(
            Long categoriaId,
            Long usuarioId
    ) {
        if (categoriaId == null) {
            throw ApiException.badRequest(
                    "La categoría es obligatoria"
            );
        }

        if (usuarioId == null) {
            throw ApiException.badRequest(
                    "El usuario es obligatorio"
            );
        }
    }

    /**
     * Deja el producto únicamente con las fotos indicadas.
     * Las anteriores se borran por orphanRemoval.
     */
    private void reemplazarFotos(
            Producto producto,
            List<String> urls
    ) {
        List<Foto> nuevas = new ArrayList<>();

        for (String url : urls) {
            nuevas.add(crearFoto(url));
        }

        producto.getFotos().clear();

        for (Foto foto : nuevas) {
            producto.agregarFoto(foto);
        }
    }

    private Foto crearFoto(String url) {
        Foto foto = new Foto();
        foto.setUrl(url);

        return foto;
    }

    private boolean tieneFotoConUrl(
            Producto producto,
            String url
    ) {
        return producto.getFotos().stream()
                .anyMatch(foto -> url.equals(foto.getUrl()));
    }

    /**
     * Valida que la lista traiga al menos una URL utilizable y
     * devuelve las URLs ya normalizadas, sin repetidos.
     */
    private List<String> validarUrls(List<String> urlsFotos) {
        if (urlsFotos == null || urlsFotos.isEmpty()) {
            throw ApiException.badRequest(
                    "El producto debe tener al menos una foto"
            );
        }

        List<String> urls = new ArrayList<>();

        for (String url : urlsFotos) {
            String normalizada = validarUrl(url);

            if (urls.contains(normalizada)) {
                throw ApiException.badRequest(
                        "La foto " + normalizada + " está repetida"
                );
            }

            urls.add(normalizada);
        }

        return urls;
    }

    private String validarUrl(String url) {
        if (url == null || url.isBlank()) {
            throw ApiException.badRequest(
                    "La URL de la foto es obligatoria"
            );
        }

        String normalizada = url.trim();

        if (normalizada.length() > Foto.MAX_LONGITUD_URL) {
            throw ApiException.badRequest(
                    "La URL de la foto no puede superar los "
                            + Foto.MAX_LONGITUD_URL
                            + " caracteres"
            );
        }

        if (!esUrlValida(normalizada)) {
            throw ApiException.badRequest(
                    "La URL de la foto no es válida: " + normalizada
            );
        }

        return normalizada;
    }

    /**
     * Acepta únicamente URLs http/https absolutas y con host.
     * El sistema guarda direcciones de imágenes, no archivos.
     */
    private boolean esUrlValida(String url) {
        try {
            URI uri = new URI(url);
            String esquema = uri.getScheme();

            return uri.isAbsolute()
                    && uri.getHost() != null
                    && ("http".equalsIgnoreCase(esquema)
                        || "https".equalsIgnoreCase(esquema));
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
