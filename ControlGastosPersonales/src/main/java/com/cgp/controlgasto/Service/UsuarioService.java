package com.cgp.controlgasto.Service;

import com.cgp.controlgasto.Model.Usuario;
import com.cgp.controlgasto.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarPorNombre(String nombre) {
        return usuarioRepository.buscarPorNombre(nombre);
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarMayoresDe(Integer edad) {
        return usuarioRepository.buscarMayoresDe(edad);
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarPorNombreContiene(String nombre) {
        return usuarioRepository.buscarPorNombreContiene(nombre);
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarPorEmailContiene(String email) {
        return usuarioRepository.buscarPorEmailContiene(email);
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarPorRangoEdad(Integer edadMin, Integer edadMax) {
        return usuarioRepository.buscarPorRangoEdad(edadMin, edadMax);
    }

    @Transactional
    public Usuario crear(Usuario usuario) {
        validarUsuario(usuario);
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Long id, Usuario actualizacion) {
        Optional<Usuario> existenteOpt = usuarioRepository.findById(id);
        if (existenteOpt.isEmpty()) {
            return null;
        }
        validarUsuario(actualizacion);
        Usuario existente = existenteOpt.get();
        String nuevoEmail = actualizacion.getEmail().trim();
        
        Optional<Usuario> usuarioConEmail = usuarioRepository.findByEmail(nuevoEmail);
        if (usuarioConEmail.isPresent() && !usuarioConEmail.get().getId().equals(id)) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }
        
        existente.setNombre(actualizacion.getNombre().trim());
        existente.setEmail(nuevoEmail);
        existente.setPassword(passwordEncoder.encode(actualizacion.getPassword()));
        if (actualizacion.getEdad() != null) {
            existente.setEdad(actualizacion.getEdad());
        }
        return usuarioRepository.save(existente);
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new RuntimeException("Usuario inválido");
        }
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre no puede estar vacío");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new RuntimeException("El email no puede estar vacío");
        }
        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            throw new RuntimeException("La contraseña no puede estar vacía");
        }
    }
}