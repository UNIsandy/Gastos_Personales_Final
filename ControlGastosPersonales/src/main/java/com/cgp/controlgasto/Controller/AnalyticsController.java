package com.cgp.controlgasto.Controller;

import com.cgp.controlgasto.Service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/dashboard/{usuarioId}")
    public ResponseEntity<?> dashboard(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.obtenerDashboard(usuarioId));
    }

    @GetMapping("/gastos-mensuales/{usuarioId}/{anio}")
    public ResponseEntity<?> gastosPorMes(@PathVariable Long usuarioId, @PathVariable int anio) {
        return ResponseEntity.ok(service.gastosPorMes(usuarioId, anio));
    }

    @GetMapping("/ingresos-mensuales/{usuarioId}/{anio}")
    public ResponseEntity<?> ingresosPorMes(@PathVariable Long usuarioId, @PathVariable int anio) {
        return ResponseEntity.ok(service.ingresosPorMes(usuarioId, anio));
    }
}
