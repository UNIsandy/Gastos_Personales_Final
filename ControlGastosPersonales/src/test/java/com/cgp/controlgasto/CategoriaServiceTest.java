package com.cgp.controlgasto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cgp.controlgasto.Model.Categoria;
import com.cgp.controlgasto.Repository.CategoriaRepository;
import com.cgp.controlgasto.Service.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CategoriaServiceTest {

    private CategoriaRepository categoriaRepository;
    private CategoriaService service;

    @BeforeEach
    public void setUp() {
        categoriaRepository = mock(CategoriaRepository.class);
        service = new CategoriaService(categoriaRepository);
    }

    @Test
    public void testCrearCategoria() {
        Categoria c = new Categoria();
        c.setNombre("Comida");

        when(categoriaRepository.existsByNombre("Comida")).thenReturn(false);
        when(categoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Categoria resultado = service.crear(c);

        assertEquals("Comida", resultado.getNombre());
        verify(categoriaRepository).save(c);
    }

    @Test
    public void testListarCategorias() {
        when(categoriaRepository.findAll()).thenReturn(List.of(new Categoria("Transporte")));

        assertFalse(service.listar().isEmpty());
    }
}
