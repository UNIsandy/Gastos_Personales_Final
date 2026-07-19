package com.cgp.controlgasto.Repository;

import com.cgp.controlgasto.Model.TipoTransaccion;
import com.cgp.controlgasto.Model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    List<Transaccion> findByUsuarioId(Long usuarioId);
    List<Transaccion> findByCategoriaId(Long categoriaId);
    List<Transaccion> findByTipo(TipoTransaccion tipo);
}
