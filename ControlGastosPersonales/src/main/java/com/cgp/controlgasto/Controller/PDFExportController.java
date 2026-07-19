package com.cgp.controlgasto.Controller;

import com.cgp.controlgasto.Service.PDFExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
public class PDFExportController {

    private final PDFExportService service;

    public PDFExportController(PDFExportService service) {
        this.service = service;
    }

    @GetMapping("/mensual/{usuarioId}/{mes}/{anio}")
    public ResponseEntity<?> descargarReporteMensual(
            @PathVariable Long usuarioId,
            @PathVariable int mes,
            @PathVariable int anio) {
        try {
            byte[] pdf = service.generarReporteMensual(usuarioId, mes, anio);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_" + mes + "_" + anio + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al generar PDF: " + e.getMessage());
        }
    }
}
