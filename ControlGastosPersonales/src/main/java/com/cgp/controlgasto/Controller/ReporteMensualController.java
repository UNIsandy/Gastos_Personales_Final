package com.cgp.controlgasto.Controller;

import com.cgp.controlgasto.Model.ReporteMensual;
import com.cgp.controlgasto.Service.ReporteMensualService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteMensualController {

    private final ReporteMensualService reporteService;

    public ReporteMensualController(ReporteMensualService reporteService) {
        this.reporteService = reporteService;
    }

    // LISTAR TODOS
    @GetMapping
    public ResponseEntity<List<ReporteMensual>> listar() {
        return ResponseEntity.ok(reporteService.listar());
    }

    // BUSCAR POR AÑO Y MES
    @GetMapping("/{anio}/{mes}")
    public ResponseEntity<?> buscarPorPeriodo(@PathVariable int anio, @PathVariable int mes) {
        ReporteMensual reporte = reporteService.buscarPorPeriodo(anio, mes);

        if (reporte == null) {
            return ResponseEntity.status(404).body("Reporte no encontrado para " + anio + "-" + mes);
        }

        return ResponseEntity.ok(reporte);
    }

    // LISTAR POR AÑO
    @GetMapping("/anio/{anio}")
    public ResponseEntity<List<ReporteMensual>> obtenerPorAnio(@PathVariable int anio) {
        return ResponseEntity.ok(reporteService.obtenerPorAnio(anio));
    }

    // CREAR REPORTE
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ReporteMensual reporte) {
        try {
            ReporteMensual nuevo = reporteService.crear(reporte);
            return ResponseEntity.status(201).body(nuevo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ACTUALIZAR REPORTE POR PERIODO
    @PutMapping("/{anio}/{mes}")
    public ResponseEntity<?> actualizar(@PathVariable int anio,
                                        @PathVariable int mes,
                                        @RequestBody ReporteMensual reporte) {
        try {
            ReporteMensual actualizado = reporteService.actualizar(anio, mes, reporte);

            if (actualizado == null) {
                return ResponseEntity.status(404).body("No existe reporte para " + anio + "-" + mes);
            }

            return ResponseEntity.ok(actualizado);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ELIMINAR REPORTE
    @DeleteMapping("/{anio}/{mes}")
    public ResponseEntity<?> eliminar(@PathVariable int anio, @PathVariable int mes) {
        boolean eliminado = reporteService.eliminar(anio, mes);

        if (!eliminado) {
            return ResponseEntity.status(404).body("No existe reporte para " + anio + "-" + mes);
        }

        return ResponseEntity.ok("Reporte eliminado correctamente");
    }
}