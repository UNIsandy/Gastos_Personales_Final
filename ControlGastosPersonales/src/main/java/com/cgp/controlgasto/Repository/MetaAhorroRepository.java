package com.cgp.controlgasto.Repository;

import com.cgp.controlgasto.Model.MetaAhorro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaAhorroRepository extends JpaRepository<MetaAhorro, Long> {
    List<MetaAhorro> findByUsuarioId(Long usuarioId);
    List<MetaAhorro> findByUsuarioIdAndActivaTrue(Long usuarioId);
}
