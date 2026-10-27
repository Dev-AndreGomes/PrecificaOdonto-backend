package com.precificaodonto.repository;

import com.precificaodonto.domain.ProcedimentoInsumo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcedimentoInsumoRepository extends JpaRepository<ProcedimentoInsumo, UUID> {

    List<ProcedimentoInsumo> findByProcedimentoId(UUID procedimentoId);
}
