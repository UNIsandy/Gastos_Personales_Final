package com.cgp.controlgasto.Controller;

import com.cgp.controlgasto.Model.Transaccion;
import com.cgp.controlgasto.Service.TransaccionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private final TransaccionService transaccionService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public FileUploadController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @PostMapping("/comprobante/{transaccionId}")
    public ResponseEntity<?> subirComprobante(
            @PathVariable Long transaccionId,
            @RequestParam("file") MultipartFile file) {

        Transaccion transaccion = transaccionService.buscarPorId(transaccionId);
        if (transaccion == null) {
            return ResponseEntity.status(404).body("Transacción no encontrada");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String nombreArchivo = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(nombreArchivo);
            Files.copy(file.getInputStream(), filePath);

            transaccion.setRutaComprobante(nombreArchivo);
            transaccionService.actualizar(transaccionId, transaccion);

            return ResponseEntity.ok(Map.of("mensaje", "Comprobante subido correctamente", "archivo", nombreArchivo));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error al subir archivo: " + e.getMessage());
        }
    }
}
