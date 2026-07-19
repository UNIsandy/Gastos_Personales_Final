package com.cgp.controlgasto.Controller;

import com.cgp.controlgasto.Model.Presupuesto;
import com.cgp.controlgasto.Service.PresupuestoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presupuestos")
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    public PresupuestoController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }

    // LISTAR TODOS
    @GetMapping
    public ResponseEntity<List<Presupuesto>> listar() {
        return ResponseEntity.ok(presupuestoService.listar());
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Presupuesto presupuesto = presupuestoService.buscarPorId(id);

        if (presupuesto == null) {
            return ResponseEntity.status(404).body("Presupuesto no encontrado");
        }

        return ResponseEntity.ok(presupuesto);
    }

    // CREAR
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Presupuesto presupuesto) {
        try {
            Presupuesto nuevo = presupuestoService.crear(presupuesto);
            return ResponseEntity.status(201).body(nuevo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ACTUALIZAR
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Presupuesto presupuesto) {
        try {
            Presupuesto actualizado = presupuestoService.actualizar(id, presupuesto);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ELIMINAR
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        boolean eliminado = presupuestoService.eliminar(id);

        if (!eliminado) {
            return ResponseEntity.status(404).body("No existe presupuesto con id: " + id);
        }

        return ResponseEntity.ok("Presupuesto eliminado correctamente");
    }

    // OBTENER PRESUPUESTOS POR USUARIO
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Presupuesto>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(presupuestoService.obtenerPorUsuario(usuarioId));
    }
}
