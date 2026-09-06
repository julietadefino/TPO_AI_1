package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.model.Categoria;
import com.uade.ecommerce.repository.CategoriaRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<Categoria> getAll() {
        return categoriaRepository.findAllByOrderByNombreAsc();
    }

    public Optional<Categoria> getById(Long id) {
        return categoriaRepository.findById(id);
    }

    public Categoria crear(Categoria categoria) {
        if (categoria.getNombre() == null ||
                categoria.getNombre().isBlank()) {
            throw ApiException.badRequest(
                    "El nombre de la categoría es obligatorio"
            );
        }

        String nombre = categoria.getNombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw ApiException.conflict(
                    "Ya existe una categoría con ese nombre"
            );
        }

        categoria.setId(null);
        categoria.setNombre(nombre);

        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Long id, String nombre) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Categoría no encontrada"
                        )
                );

        if (nombre == null || nombre.isBlank()) {
            throw ApiException.badRequest(
                    "El nombre de la categoría es obligatorio"
            );
        }

        String nombreNormalizado = nombre.trim();

        if (!categoria.getNombre()
                .equalsIgnoreCase(nombreNormalizado)
                && categoriaRepository.existsByNombreIgnoreCase(
                        nombreNormalizado)) {
            throw ApiException.conflict(
                    "Ya existe una categoría con ese nombre"
            );
        }

        categoria.setNombre(nombreNormalizado);

        return categoriaRepository.save(categoria);
    }

    public void delete(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Categoría no encontrada"
                        )
                );
        if (productoRepository.existsByCategoriaId(id)) {
            throw ApiException.conflict(
                    "No se puede eliminar la categoría porque tiene productos asociados"
            );
        }
        categoriaRepository.delete(categoria);
    }
}