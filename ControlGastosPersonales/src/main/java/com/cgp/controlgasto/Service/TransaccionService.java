package com.cgp.controlgasto.Service;

import com.cgp.controlgasto.Model.Categoria;
import com.cgp.controlgasto.Model.TipoTransaccion;
import com.cgp.controlgasto.Model.Transaccion;
import com.cgp.controlgasto.Repository.CategoriaRepository;
import com.cgp.controlgasto.Repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CategoriaRepository categoriaRepository;

    public TransaccionService(TransaccionRepository transaccionRepository, CategoriaRepository categoriaRepository) {
        this.transaccionRepository = transaccionRepository;
        this.categoriaRepository = categoriaRepository;
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
