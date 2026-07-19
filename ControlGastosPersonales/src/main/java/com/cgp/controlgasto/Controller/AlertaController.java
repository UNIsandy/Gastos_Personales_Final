package com.cgp.controlgasto.Controller;

import com.cgp.controlgasto.Model.Alerta;
import com.cgp.controlgasto.Service.AlertaService;  
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public ResponseEntity<List<Alerta>> listar() {
        return ResponseEntity.ok(alertaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Alerta alerta = alertaService.buscarPorId(id);

        if (alerta == null) {
            return ResponseEntity.status(404).body("Alerta no encontrada");
        }

        return ResponseEntity.ok(alerta);
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Alerta alerta) {
        try {
            Alerta nueva = alertaService.crear(alerta);
            return ResponseEntity.status(201).body(nueva);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Alerta alerta) {
        Alerta actualizada = alertaService.actualizar(id, alerta);

        if (actualizada == null) {
            return ResponseEntity.status(404).body("No existe alerta con id: " + id);
        }

        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        boolean eliminado = alertaService.eliminar(id);

        if (!eliminado) {
            return ResponseEntity.status(404).body("No existe alerta con id: " + id);
        }

        return ResponseEntity.ok("Alerta eliminada correctamente");
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Alerta>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(alertaService.obtenerPorUsuario(usuarioId));
    }
}