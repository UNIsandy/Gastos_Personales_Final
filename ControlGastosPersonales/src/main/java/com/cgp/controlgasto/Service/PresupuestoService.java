package com.cgp.controlgasto.Service;

import com.cgp.controlgasto.Model.Presupuesto;
import com.cgp.controlgasto.Repository.PresupuestoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;

    public PresupuestoService(PresupuestoRepository presupuestoRepository) {
        this.presupuestoRepository = presupuestoRepository;
    }

    @Transactional(readOnly = true)
    public List<Presupuesto> listar() {
        return presupuestoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Presupuesto buscarPorId(Long id) {
        return presupuestoRepository.findById(id).orElse(null);
    }

    @Transactional
    public Presupuesto crear(Presupuesto presupuesto) {
        validarPresupuesto(presupuesto);
        if (existePresupuestoDuplicado(presupuesto)) {
            throw new RuntimeException("Ya existe un presupuesto para ese mes y año");
        }
        return presupuestoRepository.save(presupuesto);
    }

    @Transactional
    public Presupuesto actualizar(Long id, Presupuesto actualizacion) {
        Optional<Presupuesto> existenteOpt = presupuestoRepository.findById(id);
        if (existenteOpt.isEmpty()) {
            throw new RuntimeException("Presupuesto no encontrado");
        }
        validarPresupuesto(actualizacion);
        Presupuesto existente = existenteOpt.get();

        if (!(existente.getMes() == actualizacion.getMes() &&
              existente.getAnio() == actualizacion.getAnio() &&
              existente.getUsuario() != null &&
              actualizacion.getUsuario() != null &&
              existente.getUsuario().getId().equals(actualizacion.getUsuario().getId()))
            && existePresupuestoDuplicado(actualizacion)) {
            throw new RuntimeException("Ya existe un presupuesto para ese mes y año");
        }

        existente.setMontoLimite(actualizacion.getMontoLimite());
        existente.setMes(actualizacion.getMes());
        existente.setAnio(actualizacion.getAnio());
        existente.setUsuario(actualizacion.getUsuario());
        return presupuestoRepository.save(existente);
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (presupuestoRepository.existsById(id)) {
            presupuestoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<Presupuesto> obtenerPorUsuario(Long usuarioId) {
        return presupuestoRepository.findByUsuarioId(usuarioId);
    }

    private void validarPresupuesto(Presupuesto presupuesto) {
        if (presupuesto == null) {
            throw new RuntimeException("Presupuesto inválido");
        }
        if (presupuesto.getMontoLimite() <= 0) {
            throw new RuntimeException("El monto límite debe ser mayor que cero");
        }
        if (presupuesto.getMes() < 1 || presupuesto.getMes() > 12) {
            throw new RuntimeException("El mes debe estar entre 1 y 12");
        }
        if (presupuesto.getAnio() <= 0) {
            throw new RuntimeException("El año debe ser mayor que cero");
        }
        if (presupuesto.getUsuario() == null || presupuesto.getUsuario().getId() == null) {
            throw new RuntimeException("El usuario es obligatorio");
        }
    }

    private boolean existePresupuestoDuplicado(Presupuesto nuevo) {
        return presupuestoRepository.findByUsuarioIdAndMesAndAnio(
            nuevo.getUsuario().getId(), nuevo.getMes(), nuevo.getAnio()).isPresent();
    }
}
