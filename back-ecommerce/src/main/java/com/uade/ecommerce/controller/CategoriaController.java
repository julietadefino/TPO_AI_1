package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.CategoriaDTO;
import com.uade.ecommerce.model.Categoria;
import com.uade.ecommerce.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uade.ecommerce.exception.ApiException;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public List<CategoriaDTO> getAll() {
        return categoriaService.getAll()
                .stream()
                .map(CategoriaDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public CategoriaDTO getById(@PathVariable Long id) {
        Categoria categoria = categoriaService.getById(id)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Categoría no encontrada"
                            )
                );

        return CategoriaDTO.fromEntity(categoria);
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> crear(
            @RequestBody CategoriaDTO datos
    ) {
        Categoria categoria = new Categoria();
        categoria.setNombre(datos.getNombre());

        Categoria guardada = categoriaService.crear(categoria);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CategoriaDTO.fromEntity(guardada));
    }

    @PutMapping("/{id}")
    public CategoriaDTO actualizar(
            @PathVariable Long id,
            @RequestBody CategoriaDTO datos
    ) {
        Categoria actualizada =
                categoriaService.actualizar(id, datos.getNombre());

        return CategoriaDTO.fromEntity(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}