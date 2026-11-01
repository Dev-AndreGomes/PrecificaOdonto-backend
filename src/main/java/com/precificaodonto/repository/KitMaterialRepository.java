package com.precificaodonto.repository;

import com.precificaodonto.domain.KitMaterial;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KitMaterialRepository extends JpaRepository<KitMaterial, UUID> {

    List<KitMaterial> findByUsuarioIdAndAtivoTrueOrderByNomeAsc(UUID usuarioId);

    List<KitMaterial> findByUsuarioIdAndKitBiossegurancaTrueAndAtivoTrue(UUID usuarioId);

    List<KitMaterial> findByUsuarioId(UUID usuarioId);
}
