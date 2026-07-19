package com.cgp.controlgasto.Controller;

import com.cgp.controlgasto.Model.TipoTransaccion;
import com.cgp.controlgasto.Service.TipoTransaccionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tipos-transaccion")
public class TipoTransaccionController {

    private final TipoTransaccionService tipoService;

    public TipoTransaccionController(TipoTransaccionService tipoService) {
        this.tipoService = tipoService;
    }

    // LISTAR TODOS LOS TIPOS (INGRESO, GASTO)
    @GetMapping
    public ResponseEntity<List<TipoTransaccion>> listar() {
        return ResponseEntity.ok(tipoService.listar());
    }

    // BUSCAR POR NOMBRE
    @GetMapping("/{nombre}")
    public ResponseEntity<?> buscarPorNombre(@PathVariable String nombre) {
        Optional<TipoTransaccion> tipo = tipoService.buscarPorNombre(nombre);

        if (tipo.isEmpty()) {
            return ResponseEntity.status(404).body("Tipo de transacción no encontrado");
        }

        return ResponseEntity.ok(tipo.get());
    }

    // VALIDAR SI UN TIPO ES CORRECTO
    @GetMapping("/validar/{nombre}")
    public ResponseEntity<?> validar(@PathVariable String nombre) {
        boolean valido = tipoService.esValido(nombre);

        if (!valido) {
            return ResponseEntity.badRequest().body("Tipo de transacción inválido");
        }

        return ResponseEntity.ok("Tipo de transacción válido");
    }
}
