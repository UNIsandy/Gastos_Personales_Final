package com.cgp.controlgasto.Service;

import com.cgp.controlgasto.Model.TipoTransaccion;
import com.cgp.controlgasto.Model.Transaccion;
import com.cgp.controlgasto.Repository.TransaccionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificacionProgramadaService {

    private final TransaccionRepository transaccionRepository;
    private final EmailService emailService;

    public NotificacionProgramadaService(TransaccionRepository transaccionRepository, EmailService emailService) {
        this.transaccionRepository = transaccionRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void verificarTransaccionesProgramadas() {
        List<Transaccion> todas = transaccionRepository.findAll();
        LocalDate hoy = LocalDate.now();

        List<Transaccion> programadas = todas.stream()
            .filter(t -> !t.getFecha().isBefore(hoy))
            .toList();

        for (Transaccion t : programadas) {
            String email = t.getUsuario().getEmail();
            if (email == null || email.isBlank()) continue;

            long dias = ChronoUnit.DAYS.between(hoy, t.getFecha());
            String tipo = t.getTipo() == TipoTransaccion.GASTO ? "Gasto" : "Ingreso";
            String montoStr = "S/ " + String.format("%.2f", t.getMonto());
            String frontendUrl = "https://melodious-balance-production-37ee.up.railway.app/programadas";

            if (dias == 0) {
                String asunto = "🔔 Transacción programada para hoy";
                String cuerpo = "Hola " + t.getUsuario().getNombre() + ",\n\n"
                    + "La siguiente transacción está programada para HOY:\n\n"
                    + "  " + tipo + ": " + t.getDescripcion() + "\n"
                    + "  Monto: " + montoStr + "\n"
                    + "  Categoría: " + (t.getCategoria() != null ? t.getCategoria().getNombre() : "Sin categoría") + "\n\n"
                    + "Si ya no deseas que se realice, ingresa a:\n"
                    + frontendUrl + "\n"
                    + "y elimina esta transacción programada.\n\n"
                    + "Saludos,\nControl Gastos";
                emailService.enviar(email, asunto, cuerpo);
            } else if (dias <= 3) {
                String asunto = "📅 Recordatorio: " + tipo + " programado en " + dias + " día(s)";
                String cuerpo = "Hola " + t.getUsuario().getNombre() + ",\n\n"
                    + "Te recordamos la siguiente transacción programada:\n\n"
                    + "  " + tipo + ": " + t.getDescripcion() + "\n"
                    + "  Monto: " + montoStr + "\n"
                    + "  Fecha: " + t.getFecha() + " (faltan " + dias + " día(s))\n"
                    + "  Categoría: " + (t.getCategoria() != null ? t.getCategoria().getNombre() : "Sin categoría") + "\n\n"
                    + "Si deseas modificar o cancelar esta transacción, ingresa a:\n"
                    + frontendUrl + "\n\n"
                    + "Saludos,\nControl Gastos";
                emailService.enviar(email, asunto, cuerpo);
            }
        }
    }
}
