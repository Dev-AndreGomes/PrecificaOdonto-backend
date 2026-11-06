package com.precificaodonto.repository;

import com.precificaodonto.domain.Procedimento;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcedimentoRepository extends JpaRepository<Procedimento, UUID> {

    List<Procedimento> findByUsuarioIdAndAtivoTrueOrderByNomeAsc(UUID usuarioId);

    List<Procedimento> findByUsuarioIdAndAtivoTrue(UUID usuarioId);

    List<Procedimento> findByUsuarioId(UUID usuarioId);

    Optional<Procedimento> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}
