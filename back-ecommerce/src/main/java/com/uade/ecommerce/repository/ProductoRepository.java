package com.uade.ecommerce.repository;

import com.uade.ecommerce.model.Producto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @EntityGraph(attributePaths = "fotos")
    List<Producto> findAllByOrderByNombreAsc();

    @EntityGraph(attributePaths = "fotos")
    List<Producto> findByCategoriaIdOrderByNombreAsc(Long categoriaId);

    @EntityGraph(attributePaths = "fotos")
    Optional<Producto> findById(Long id);

    boolean existsByCategoriaId(Long categoriaId);
}
