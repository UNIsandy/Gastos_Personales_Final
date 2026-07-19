package com.cgp.controlgasto.Service;

import com.cgp.controlgasto.Model.MetaAhorro;
import com.cgp.controlgasto.Model.TipoTransaccion;
import com.cgp.controlgasto.Repository.MetaAhorroRepository;
import com.cgp.controlgasto.Repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Transactional
    public List<MetaAhorro> obtenerPorUsuario(Long usuarioId) {
        List<MetaAhorro> metas = repository.findByUsuarioId(usuarioId);
        LocalDate hoy = LocalDate.now();
        for (MetaAhorro meta : metas) {
            if (meta.isActiva() && meta.getFechaLimite() != null
                && meta.getFechaLimite().isBefore(hoy)
                && (meta.getMontoActual() == null || meta.getMontoActual() < meta.getMontoObjetivo())) {
                meta.setActiva(false);
                repository.save(meta);
            }
        }
        return repository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public List<MetaAhorro> obtenerActivasPorUsuario(Long usuarioId) {
        desactivarVencidas(usuarioId);
        return repository.findByUsuarioIdAndActivaTrue(usuarioId);
    }

    private void desactivarVencidas(Long usuarioId) {
        List<MetaAhorro> metas = repository.findByUsuarioId(usuarioId);
        LocalDate hoy = LocalDate.now();
        for (MetaAhorro meta : metas) {
            if (meta.isActiva() && meta.getFechaLimite() != null
                && meta.getFechaLimite().isBefore(hoy)
                && (meta.getMontoActual() == null || meta.getMontoActual() < meta.getMontoObjetivo())) {
                meta.setActiva(false);
                repository.save(meta);
            }
        }
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
        if (monto == null || monto == 0) throw new RuntimeException("El monto debe ser diferente de cero");

        Long usuarioId = meta.getUsuario().getId();

        if (monto < 0) {
            double retiro = -monto;
            if (retiro > meta.getMontoActual()) {
                throw new RuntimeException("No puedes retirar más de lo que has aportado. Aportado: S/ "
                    + String.format("%.2f", meta.getMontoActual()));
            }
        } else {
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
        }

        meta.setMontoActual(meta.getMontoActual() + monto);
        if (meta.getMontoActual() >= meta.getMontoObjetivo()) {
            meta.setActiva(false);
        } else {
            meta.setActiva(true);
        }
        return repository.save(meta);
    }

    @Transactional
    public MetaAhorro reactivar(Long id, LocalDate nuevaFechaLimite, Double aporteInicial) {
        MetaAhorro meta = repository.findById(id).orElse(null);
        if (meta == null) return null;
        if (meta.isActiva()) throw new RuntimeException("La meta ya está activa");
        if (nuevaFechaLimite == null) throw new RuntimeException("Debes indicar una nueva fecha límite");
        if (nuevaFechaLimite.isBefore(LocalDate.now())) throw new RuntimeException("La nueva fecha debe ser futura");
        if (aporteInicial == null || aporteInicial <= 0) throw new RuntimeException("Debes hacer un aporte inicial para reactivar la meta");

        if (aporteInicial > 0) {
            var transacciones = transaccionRepository.findByUsuarioId(meta.getUsuario().getId());
            double totalIngresos = transacciones.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                .mapToDouble(t -> t.getMonto()).sum();
            double totalGastos = transacciones.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
                .mapToDouble(t -> t.getMonto()).sum();
            double totalAportado = repository.findByUsuarioId(meta.getUsuario().getId()).stream()
                .mapToDouble(m -> m.getMontoActual() != null ? m.getMontoActual() : 0.0)
                .sum() - (meta.getMontoActual() != null ? meta.getMontoActual() : 0.0);
            double disponible = totalIngresos - totalGastos - totalAportado;
            if (aporteInicial > disponible) {
                throw new RuntimeException("No tienes suficiente saldo. Disponible: S/ "
                    + String.format("%.2f", disponible));
            }
        }

        meta.setActiva(true);
        meta.setFechaLimite(nuevaFechaLimite);
        meta.setMontoActual(meta.getMontoActual() + aporteInicial);
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
