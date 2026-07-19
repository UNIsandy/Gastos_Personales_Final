package com.cgp.controlgasto.Service;

import com.cgp.controlgasto.Model.Categoria;
import com.cgp.controlgasto.Model.Presupuesto;
import com.cgp.controlgasto.Model.TipoTransaccion;
import com.cgp.controlgasto.Model.Transaccion;
import com.cgp.controlgasto.Repository.CategoriaRepository;
import com.cgp.controlgasto.Repository.PresupuestoRepository;
import com.cgp.controlgasto.Repository.TransaccionRepository;
import com.cgp.controlgasto.dto.RiesgoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CategoriaRepository categoriaRepository;
    private final PresupuestoRepository presupuestoRepository;

    public TransaccionService(TransaccionRepository transaccionRepository, CategoriaRepository categoriaRepository, PresupuestoRepository presupuestoRepository) {
        this.transaccionRepository = transaccionRepository;
        this.categoriaRepository = categoriaRepository;
        this.presupuestoRepository = presupuestoRepository;
    }

    @Transactional(readOnly = true)
    public List<Transaccion> listar() {
        return transaccionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Transaccion buscarPorId(Long id) {
        return transaccionRepository.findById(id).orElse(null);
    }

    @Transactional
    public Transaccion crear(Transaccion transaccion) {
        validarTransaccion(transaccion);
        if (transaccion.getCategoria() != null && transaccion.getCategoria().getId() == null) {
            Categoria cat = categoriaRepository.findByNombre(transaccion.getCategoria().getNombre())
                .orElseGet(() -> categoriaRepository.save(new Categoria(transaccion.getCategoria().getNombre())));
            transaccion.setCategoria(cat);
        }
        return transaccionRepository.save(transaccion);
    }

    @Transactional
    public Transaccion actualizar(Long id, Transaccion actualizacion) {
        Optional<Transaccion> existenteOpt = transaccionRepository.findById(id);
        if (existenteOpt.isEmpty()) {
            return null;
        }
        validarTransaccion(actualizacion);
        Transaccion existente = existenteOpt.get();

        if (actualizacion.getCategoria() != null && actualizacion.getCategoria().getId() == null) {
            Categoria cat = categoriaRepository.findByNombre(actualizacion.getCategoria().getNombre())
                .orElseGet(() -> categoriaRepository.save(new Categoria(actualizacion.getCategoria().getNombre())));
            actualizacion.setCategoria(cat);
        }

        existente.setDescripcion(actualizacion.getDescripcion());
        existente.setMonto(actualizacion.getMonto());
        existente.setFecha(actualizacion.getFecha());
        existente.setCategoria(actualizacion.getCategoria());
        existente.setUsuario(actualizacion.getUsuario());
        existente.setTipo(actualizacion.getTipo());
        return transaccionRepository.save(existente);
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (transaccionRepository.existsById(id)) {
            transaccionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<Transaccion> obtenerPorUsuario(Long usuarioId) {
        return transaccionRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Transaccion> obtenerPorCategoria(Long categoriaId) {
        return transaccionRepository.findByCategoriaId(categoriaId);
    }

    @Transactional(readOnly = true)
    public List<Transaccion> obtenerPorTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return List.of();
        }
        return transaccionRepository.findByTipo(TipoTransaccion.valueOf(tipo.trim().toUpperCase()));
    }

    @Transactional(readOnly = true)
    public RiesgoDTO verificarRiesgo(Long usuarioId, String tipo, Double monto, String categoriaNombre) {
        boolean riesgo = false;
        List<String> recomendaciones = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        int mesActual = hoy.getMonthValue();
        int anioActual = hoy.getYear();

        // Only check risk for expenses
        if (!"GASTO".equalsIgnoreCase(tipo)) {
            return new RiesgoDTO(false, null, null, recomendaciones);
        }

        List<Transaccion> transacciones = transaccionRepository.findByUsuarioId(usuarioId);
        List<Transaccion> mesActualTransacciones = transacciones.stream()
            .filter(t -> t.getFecha().getMonthValue() == mesActual && t.getFecha().getYear() == anioActual)
            .toList();

        double ingresosMes = mesActualTransacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
            .mapToDouble(Transaccion::getMonto).sum();

        double gastosMes = mesActualTransacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
            .mapToDouble(Transaccion::getMonto).sum();

        double gastosProyectados = gastosMes + monto;
        String mensaje = "";

        if (ingresosMes == 0 && gastosMes == 0) {
            riesgo = true;
            mensaje = "No has registrado ingresos este mes. Este gasto puede ponerte en déficit.";
            recomendaciones.add("Registra tus ingresos mensuales para tener un control real de tus finanzas.");
            recomendaciones.add("Considera posponer este gasto hasta que tengas claro tu flujo de ingresos.");
        }

        if (gastosProyectados > ingresosMes && ingresosMes > 0) {
            riesgo = true;
            double excedente = gastosProyectados - ingresosMes;
            mensaje = "Este gasto pondría tus gastos del mes por encima de tus ingresos en S/ "
                + String.format("%.2f", excedente) + ".";
            recomendaciones.add("Revisa tus gastos en Entretenimiento y otras categorías no esenciales.");
            recomendaciones.add("Intenta mantener tus gastos totales por debajo del 70% de tus ingresos.");
            recomendaciones.add("Considera aumentar tus ingresos o reducir gastos fijos.");
        }

        if (monto > ingresosMes * 0.5 && ingresosMes > 0) {
            riesgo = true;
            if (mensaje.isEmpty()) {
                mensaje = "Este gasto representa más del 50% de tus ingresos del mes.";
            }
            recomendaciones.add("Dividir este gasto en partes más pequeñas puede ayudarte a no desestabilizar tu presupuesto.");
            recomendaciones.add("Evalúa si este gasto es prioritario o puedes aplazarlo.");
        }

        // Check budget for current month if exists
        Optional<Presupuesto> presupuestoOpt = presupuestoRepository.findByUsuarioIdAndMesAndAnio(usuarioId, mesActual, anioActual);
        if (presupuestoOpt.isPresent() && gastosProyectados > presupuestoOpt.get().getMontoLimite()) {
            riesgo = true;
            double excede = gastosProyectados - presupuestoOpt.get().getMontoLimite();
            if (mensaje.isEmpty()) {
                mensaje = "Este gasto haría que superes tu presupuesto mensual en S/ "
                    + String.format("%.2f", excede) + ".";
            }
            recomendaciones.add("Ajusta tu presupuesto mensual o reduce gastos en otras categorías para compensar.");
        }

        String titulo = riesgo ? "⚠️ Tu economía está en riesgo" : null;
        return new RiesgoDTO(riesgo, titulo, mensaje, recomendaciones);
    }

    private void validarTransaccion(Transaccion transaccion) {
        if (transaccion == null) {
            throw new RuntimeException("Transacción inválida");
        }
        if (transaccion.getDescripcion() == null || transaccion.getDescripcion().trim().isEmpty()) {
            throw new RuntimeException("La descripción no puede estar vacía");
        }
        if (transaccion.getMonto() == null || transaccion.getMonto() <= 0) {
            throw new RuntimeException("El monto debe ser mayor que cero");
        }
        if (transaccion.getFecha() == null) {
            throw new RuntimeException("La fecha es obligatoria");
        }
        if (transaccion.getFecha().isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha no puede ser futura");
        }
        if (transaccion.getTipo() == null) {
            throw new RuntimeException("El tipo de transacción es obligatorio");
        }
    }
}
