// repository/FotoRepository.java
package com.uade.ecommerce.repository;

import com.uade.ecommerce.model.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FotoRepository extends JpaRepository<Foto, Long> {
}