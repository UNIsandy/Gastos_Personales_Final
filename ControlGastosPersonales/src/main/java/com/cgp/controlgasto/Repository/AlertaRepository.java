package com.cgp.controlgasto.Repository;

import com.cgp.controlgasto.Model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findByUsuarioId(Long usuarioId);
    List<Alerta> findByUsuarioIdAndActivaTrue(Long usuarioId);
}
