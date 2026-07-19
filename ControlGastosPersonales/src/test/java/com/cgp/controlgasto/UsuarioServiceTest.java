package com.cgp.controlgasto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cgp.controlgasto.Model.Usuario;
import com.cgp.controlgasto.Repository.UsuarioRepository;
import com.cgp.controlgasto.Service.UsuarioService;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService service;

    @BeforeEach
    public void setUp() {
        service = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    public void testCrearUsuario() {
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        Usuario u = new Usuario();
        u.setNombre("Carlos");
        u.setEmail("carlos@email.com");
        u.setPassword("1234");

        Usuario resultado = service.crear(u);

        assertNotNull(resultado.getId());
        assertEquals("Carlos", resultado.getNombre());
    }

    @Test
    public void testEliminarUsuario() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        boolean eliminado = service.eliminar(1L);

        assertTrue(eliminado);
    }
}