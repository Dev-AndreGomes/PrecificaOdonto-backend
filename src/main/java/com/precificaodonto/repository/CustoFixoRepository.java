package com.precificaodonto.repository;

import com.precificaodonto.domain.CustoFixo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustoFixoRepository extends JpaRepository<CustoFixo, UUID> {

    List<CustoFixo> findByUsuarioIdAndAtivoTrue(UUID usuarioId);

    List<CustoFixo> findByUsuarioId(UUID usuarioId);
}
