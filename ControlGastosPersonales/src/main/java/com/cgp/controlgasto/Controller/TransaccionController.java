package com.cgp.controlgasto.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cgp.controlgasto.Model.Transaccion;
import com.cgp.controlgasto.Service.TransaccionService;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    // LISTAR TODAS
    @GetMapping
    public ResponseEntity<List<Transaccion>> listar() {
        return ResponseEntity.ok(transaccionService.listar());
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Transaccion transaccion = transaccionService.buscarPorId(id);

        if (transaccion == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Transacción no encontrada"));
        }

        return ResponseEntity.ok(transaccion);
    }

    // CREAR
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Transaccion transaccion) {
        try {
            Transaccion nueva = transaccionService.crear(transaccion);
            return ResponseEntity.status(201).body(nueva);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ACTUALIZAR COMPLETO (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Transaccion transaccion) {
        try {
            Transaccion actualizada = transaccionService.actualizar(id, transaccion);

            if (actualizada == null) {
                return ResponseEntity.status(404).body(Map.of("error", "No existe transacción con id: " + id));
            }

            return ResponseEntity.ok(actualizada);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ACTUALIZAR PARCIAL (PATCH)
    @PatchMapping("/{id}")
    public ResponseEntity<?> actualizarParcial(@PathVariable Long id, @RequestBody Transaccion parcial) {

        Transaccion existente = transaccionService.buscarPorId(id);

        if (existente == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Transacción no encontrada"));
        }

        // SOLO ACTUALIZA LO QUE VIENE
        if (parcial.getDescripcion() != null) {
            existente.setDescripcion(parcial.getDescripcion());
        }

        if (parcial.getMonto() != null) {
            existente.setMonto(parcial.getMonto());
        }

        if (parcial.getFecha() != null) {
            existente.setFecha(parcial.getFecha());
        }

        if (parcial.getCategoria() != null) {
            existente.setCategoria(parcial.getCategoria());
        }

        if (parcial.getUsuario() != null) {
            existente.setUsuario(parcial.getUsuario());
        }

        return ResponseEntity.ok(existente);
    }

    // ELIMINAR
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        boolean eliminado = transaccionService.eliminar(id);

        if (!eliminado) {
            return ResponseEntity.status(404).body(Map.of("error", "No existe transacción con id: " + id));
        }

        return ResponseEntity.ok(Map.of("message", "Transacción eliminada correctamente"));
    }

    // VERIFICAR RIESGO ANTES DE CREAR TRANSACCION
    @PostMapping("/verificar-riesgo")
    public ResponseEntity<?> verificarRiesgo(@RequestBody Map<String, Object> body) {
        try {
            Long usuarioId = Long.valueOf(body.get("usuarioId").toString());
            String tipo = (String) body.get("tipo");
            Double monto = Double.valueOf(body.get("monto").toString());
            String categoria = (String) body.getOrDefault("categoria", "");
            return ResponseEntity.ok(transaccionService.verificarRiesgo(usuarioId, tipo, monto, categoria));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // OBTENER POR USUARIO
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Transaccion>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(transaccionService.obtenerPorUsuario(usuarioId));
    }

    // OBTENER POR CATEGORIA
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Transaccion>> obtenerPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(transaccionService.obtenerPorCategoria(categoriaId));
    }

    // OBTENER POR TIPO
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Transaccion>> obtenerPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(transaccionService.obtenerPorTipo(tipo));
    }
}