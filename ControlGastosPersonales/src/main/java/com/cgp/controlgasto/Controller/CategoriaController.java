package com.cgp.controlgasto.Controller;

import com.cgp.controlgasto.Model.Categoria;
import com.cgp.controlgasto.Service.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // LISTAR
    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.buscarPorId(id);

        if (categoria == null) {
            return ResponseEntity.status(404).body("Categoría no encontrada");
        }

        return ResponseEntity.ok(categoria);
    }

    // CREAR
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Categoria categoria) {
        try {
            Categoria nueva = categoriaService.crear(categoria);
            return ResponseEntity.status(201).body(nueva);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ACTUALIZAR
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Categoria categoria) {
        try {
            Categoria actualizada = categoriaService.actualizar(id, categoria);

            if (actualizada == null) {
                return ResponseEntity.status(404).body("No existe categoría con id: " + id);
            }

            return ResponseEntity.ok(actualizada);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ELIMINAR
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        boolean eliminado = categoriaService.eliminar(id);

        if (!eliminado) {
            return ResponseEntity.status(404).body("No existe categoría con id: " + id);
        }

        return ResponseEntity.ok("Categoría eliminada correctamente");
    }
}