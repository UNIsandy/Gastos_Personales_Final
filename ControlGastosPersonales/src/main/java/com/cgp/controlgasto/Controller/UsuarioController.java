package com.cgp.controlgasto.Controller;

import com.cgp.controlgasto.Model.Usuario;
import com.cgp.controlgasto.Service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // LISTAR TODOS
    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(404).body("Usuario no encontrado"));
    }

    // BUSCAR POR EMAIL
    @GetMapping("/email/{email}")
    public ResponseEntity<?> buscarPorEmail(@PathVariable String email) {
        return usuarioService.buscarPorEmail(email)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(404).body("Usuario no encontrado con email: " + email));
    }

    // JPQL: BUSCAR POR NOMBRE EXACTO
    @GetMapping("/buscar/nombre/{nombre}")
    public ResponseEntity<List<Usuario>> buscarPorNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(usuarioService.buscarPorNombre(nombre));
    }

    // JPQL: BUSCAR POR NOMBRE CONTENIDO
    @GetMapping("/buscar/nombre-like/{nombre}")
    public ResponseEntity<List<Usuario>> buscarPorNombreLike(@PathVariable String nombre) {
        return ResponseEntity.ok(usuarioService.buscarPorNombreContiene(nombre));
    }

    // JPQL: BUSCAR MAYORES DE EDAD
    @GetMapping("/buscar/mayores/{edad}")
    public ResponseEntity<List<Usuario>> buscarMayoresDe(@PathVariable Integer edad) {
        return ResponseEntity.ok(usuarioService.buscarMayoresDe(edad));
    }

    // JPQL: BUSCAR POR RANGO DE EDAD
    @GetMapping("/buscar/edad/{edadMin}/{edadMax}")
    public ResponseEntity<List<Usuario>> buscarPorRangoEdad(
            @PathVariable Integer edadMin, @PathVariable Integer edadMax) {
        return ResponseEntity.ok(usuarioService.buscarPorRangoEdad(edadMin, edadMax));
    }

    // JPQL: BUSCAR EMAIL CONTENIDO
    @GetMapping("/buscar/email-like/{email}")
    public ResponseEntity<List<Usuario>> buscarEmailLike(@PathVariable String email) {
        return ResponseEntity.ok(usuarioService.buscarPorEmailContiene(email));
    }

    // CREAR USUARIO
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Usuario usuario) {
        try {
            Usuario nuevo = usuarioService.crear(usuario);
            return ResponseEntity.status(201).body(nuevo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ACTUALIZAR USUARIO
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        try {
            Usuario actualizado = usuarioService.actualizar(id, usuario);

            if (actualizado == null) {
                return ResponseEntity.status(404).body("No existe usuario con id: " + id);
            }

            return ResponseEntity.ok(actualizado);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ELIMINAR USUARIO
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        boolean eliminado = usuarioService.eliminar(id);

        if (!eliminado) {
            return ResponseEntity.status(404).body("No existe usuario con id: " + id);
        }

        return ResponseEntity.ok("Usuario eliminado correctamente");
    }
}