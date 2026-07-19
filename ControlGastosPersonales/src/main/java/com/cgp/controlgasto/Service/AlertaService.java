package com.cgp.controlgasto.Service;

import com.cgp.controlgasto.Model.Alerta;
import com.cgp.controlgasto.Repository.AlertaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public AlertaService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    @Transactional(readOnly = true)
    public List<Alerta> listar() {
        return alertaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Alerta buscarPorId(Long id) {
        return alertaRepository.findById(id).orElse(null);
    }

    @Transactional
    public Alerta crear(Alerta alerta) {
        if (alerta.getMensaje() == null || alerta.getMensaje().isEmpty()) {
            throw new RuntimeException("El mensaje no puede estar vacío");
        }
        return alertaRepository.save(alerta);
    }

    @Transactional
    public Alerta actualizar(Long id, Alerta nueva) {
        Optional<Alerta> existenteOpt = alertaRepository.findById(id);
        if (existenteOpt.isEmpty()) {
            return null;
        }
        Alerta a = existenteOpt.get();
        a.setMensaje(nueva.getMensaje());
        a.setActiva(nueva.isActiva());
        a.setUsuario(nueva.getUsuario());
        return alertaRepository.save(a);
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (alertaRepository.existsById(id)) {
            alertaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<Alerta> obtenerPorUsuario(Long usuarioId) {
        return alertaRepository.findByUsuarioId(usuarioId);
    }
}
