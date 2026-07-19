package com.cgp.controlgasto.Repository;

import com.cgp.controlgasto.Model.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {
    List<Presupuesto> findByUsuarioId(Long usuarioId);
    Optional<Presupuesto> findByUsuarioIdAndMesAndAnio(Long usuarioId, int mes, int anio);
}
