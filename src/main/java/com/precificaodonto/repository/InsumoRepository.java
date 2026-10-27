package com.precificaodonto.repository;

import com.precificaodonto.domain.Insumo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, UUID> {

    List<Insumo> findByUsuarioIdAndAtivoTrue(UUID usuarioId);

    List<Insumo> findByUsuarioIdAndKitBiossegurancaTrueAndAtivoTrue(UUID usuarioId);

    List<Insumo> findByUsuarioId(UUID usuarioId);
}
