package com.precificaodonto.repository;

import com.precificaodonto.domain.ConfigClinica;
import com.precificaodonto.domain.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigClinicaRepository extends JpaRepository<ConfigClinica, UUID> {

    Optional<ConfigClinica> findByUsuarioId(UUID usuarioId);

    Optional<ConfigClinica> findByUsuario(Usuario usuario);
}
