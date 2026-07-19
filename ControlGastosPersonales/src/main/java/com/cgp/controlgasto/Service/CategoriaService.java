package com.cgp.controlgasto.Service;

import com.cgp.controlgasto.Model.Categoria;
import com.cgp.controlgasto.Repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    @Transactional
    public Categoria crear(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre de la categoría no puede estar vacío");
        }
        String nombreLimpio = categoria.getNombre().trim();
        if (categoriaRepository.existsByNombre(nombreLimpio)) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }
        categoria.setNombre(nombreLimpio);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizar(Long id, Categoria nueva) {
        Optional<Categoria> existenteOpt = categoriaRepository.findById(id);
        if (existenteOpt.isEmpty()) {
            return null;
        }
        if (nueva.getNombre() == null || nueva.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre de la categoría no puede estar vacío");
        }
        Categoria existente = existenteOpt.get();
        String nuevoNombre = nueva.getNombre().trim();
        if (!existente.getNombre().equalsIgnoreCase(nuevoNombre) && categoriaRepository.existsByNombre(nuevoNombre)) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }
        existente.setNombre(nuevoNombre);
        return categoriaRepository.save(existente);
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
