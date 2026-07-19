package com.cgp.controlgasto.Service;

import com.cgp.controlgasto.Model.TipoTransaccion;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TipoTransaccionService {
//listar
    public List<TipoTransaccion> listar() {
        return Arrays.asList(TipoTransaccion.values());
    }
//buscar por id
    public Optional<TipoTransaccion> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return Optional.empty();
        }
//buscar por nombre
        String valor = nombre.trim().toUpperCase();
        for (TipoTransaccion tipo : TipoTransaccion.values()) {
            if (tipo.name().equals(valor)) {
                return Optional.of(tipo);
            }
        }
        return Optional.empty();
    }
//validacion
    public boolean esValido(String nombre) {
        return buscarPorNombre(nombre).isPresent();
    }
}
