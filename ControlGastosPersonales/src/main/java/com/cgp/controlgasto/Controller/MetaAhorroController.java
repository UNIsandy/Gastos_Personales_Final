package com.cgp.controlgasto.Controller;

import com.cgp.controlgasto.Model.MetaAhorro;
import com.cgp.controlgasto.Service.MetaAhorroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metas")
public class MetaAhorroController {

    private final MetaAhorroService service;

    public MetaAhorroController(MetaAhorroService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<MetaAhorro>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        MetaAhorro meta = service.buscarPorId(id);
        if (meta == null) return ResponseEntity.status(404).body("Meta no encontrada");
        return ResponseEntity.ok(meta);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<MetaAhorro>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.obtenerPorUsuario(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/activas")
    public ResponseEntity<List<MetaAhorro>> obtenerActivas(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.obtenerActivasPorUsuario(usuarioId));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody MetaAhorro meta) {
        try {
            return ResponseEntity.status(201).body(service.crear(meta));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody MetaAhorro meta) {
        try {
            MetaAhorro actualizada = service.actualizar(id, meta);
            if (actualizada == null) return ResponseEntity.status(404).body("Meta no encontrada");
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/aportar")
    public ResponseEntity<?> aportar(@PathVariable Long id, @RequestBody Map<String, Double> body) {
        try {
            MetaAhorro meta = service.aportar(id, body.get("monto"));
            if (meta == null) return ResponseEntity.status(404).body("Meta no encontrada");
            return ResponseEntity.ok(meta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        boolean eliminado = service.eliminar(id);
        if (!eliminado) return ResponseEntity.status(404).body("Meta no encontrada");
        return ResponseEntity.ok("Meta eliminada correctamente");
    }
}
