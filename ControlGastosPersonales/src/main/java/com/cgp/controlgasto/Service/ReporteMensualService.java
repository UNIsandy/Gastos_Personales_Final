package com.cgp.controlgasto.Service;

import com.cgp.controlgasto.Model.ReporteMensual;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReporteMensualService {

    private final Map<String, ReporteMensual> reportes = new HashMap<>();

    private String key(int anio, int mes) {
        return anio + "-" + mes;
    }

    public List<ReporteMensual> listar() {
        return new ArrayList<>(reportes.values());
    }

    public ReporteMensual buscarPorPeriodo(int anio, int mes) {
        return reportes.get(key(anio, mes));
    }

    public List<ReporteMensual> obtenerPorAnio(int anio) {
        List<ReporteMensual> resultados = new ArrayList<>();
        for (ReporteMensual reporte : reportes.values()) {
            if (reporte.getAnio() == anio) {
                resultados.add(reporte);
            }
        }
        return resultados;
    }

    public ReporteMensual crear(ReporteMensual reporte) {
        validarReporte(reporte);
        String key = key(reporte.getAnio(), reporte.getMes());
        if (reportes.containsKey(key)) {
            throw new RuntimeException("Ya existe un reporte para ese año y mes");
        }
        reportes.put(key, reporte);
        return reporte;
    }

    public ReporteMensual actualizar(int anio, int mes, ReporteMensual nuevoReporte) {
        validarReporte(nuevoReporte);
        String key = key(anio, mes);
        if (!reportes.containsKey(key)) {
            return null;
        }
        reportes.put(key, nuevoReporte);
        return nuevoReporte;
    }

    public boolean eliminar(int anio, int mes) {
        return reportes.remove(key(anio, mes)) != null;
    }

    private void validarReporte(ReporteMensual reporte) {
        if (reporte == null) {
            throw new RuntimeException("Reporte inválido");
        }
        if (reporte.getAnio() <= 0) {
            throw new RuntimeException("El año debe ser mayor que cero");
        }
        if (reporte.getMes() < 1 || reporte.getMes() > 12) {
            throw new RuntimeException("El mes debe estar entre 1 y 12");
        }
        if (reporte.getTotalIngresos() < 0) {
            throw new RuntimeException("El total de ingresos no puede ser negativo");
        }
        if (reporte.getTotalGastos() < 0) {
            throw new RuntimeException("El total de gastos no puede ser negativo");
        }
        if (reporte.getGastosPorCategoria() == null) {
            throw new RuntimeException("El mapa de gastos por categoría no puede ser nulo");
        }
    }
}
