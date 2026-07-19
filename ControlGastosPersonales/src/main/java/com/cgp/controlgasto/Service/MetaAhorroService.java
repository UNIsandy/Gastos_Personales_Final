package com.cgp.controlgasto.Service;

import com.cgp.controlgasto.Model.MetaAhorro;
import com.cgp.controlgasto.Model.TipoTransaccion;
import com.cgp.controlgasto.Repository.MetaAhorroRepository;
import com.cgp.controlgasto.Repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MetaAhorroService {

    private final MetaAhorroRepository repository;
    private final TransaccionRepository transaccionRepository;

    public MetaAhorroService(MetaAhorroRepository repository, TransaccionRepository transaccionRepository) {
        this.repository = repository;
        this.transaccionRepository = transaccionRepository;
    }

    @Transactional(readOnly = true)
    public List<MetaAhorro> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public MetaAhorro buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<MetaAhorro> obtenerPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<MetaAhorro> obtenerActivasPorUsuario(Long usuarioId) {
        return repository.findByUsuarioIdAndActivaTrue(usuarioId);
    }

    @Transactional
    public MetaAhorro crear(MetaAhorro meta) {
        if (meta.getMontoObjetivo() == null || meta.getMontoObjetivo() <= 0) {
            throw new RuntimeException("El monto objetivo debe ser mayor que cero");
        }
        if (meta.getNombre() == null || meta.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre de la meta es obligatorio");
        }
        if (meta.getMontoActual() == null) meta.setMontoActual(0.0);
        return repository.save(meta);
    }

    @Transactional
    public MetaAhorro actualizar(Long id, MetaAhorro actualizacion) {
        MetaAhorro existente = repository.findById(id).orElse(null);
        if (existente == null) return null;
        if (actualizacion.getNombre() != null) existente.setNombre(actualizacion.getNombre());
        if (actualizacion.getMontoObjetivo() != null) existente.setMontoObjetivo(actualizacion.getMontoObjetivo());
        if (actualizacion.getFechaLimite() != null) existente.setFechaLimite(actualizacion.getFechaLimite());
        return repository.save(existente);
    }

    @Transactional
    public MetaAhorro aportar(Long id, Double monto) {
        MetaAhorro meta = repository.findById(id).orElse(null);
        if (meta == null) return null;
        if (monto == null || monto <= 0) throw new RuntimeException("El monto debe ser mayor que cero");

        Long usuarioId = meta.getUsuario().getId();
        var transacciones = transaccionRepository.findByUsuarioId(usuarioId);

        double totalIngresos = transacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
            .mapToDouble(t -> t.getMonto()).sum();

        double totalGastos = transacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
            .mapToDouble(t -> t.getMonto()).sum();

        double totalAportado = repository.findByUsuarioId(usuarioId).stream()
            .mapToDouble(m -> m.getMontoActual() != null ? m.getMontoActual() : 0.0)
            .sum() - (meta.getMontoActual() != null ? meta.getMontoActual() : 0.0);

        double disponible = totalIngresos - totalGastos - totalAportado;
        if (monto > disponible) {
            throw new RuntimeException(
                "No tienes suficiente saldo disponible. Tus ingresos netos son S/ "
                + String.format("%.2f", totalIngresos - totalGastos)
                + ", ya has destinado S/ " + String.format("%.2f", totalAportado)
                + " a otras metas. Disponible: S/ " + String.format("%.2f", disponible)
            );
        }

        meta.setMontoActual(meta.getMontoActual() + monto);
        if (meta.getMontoActual() >= meta.getMontoObjetivo()) meta.setActiva(false);
        return repository.save(meta);
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
