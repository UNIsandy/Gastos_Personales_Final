package com.cgp.controlgasto.Service;

import com.cgp.controlgasto.Model.TipoTransaccion;
import com.cgp.controlgasto.Model.Transaccion;
import com.cgp.controlgasto.Repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import com.cgp.controlgasto.dto.ConsejoDTO;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final TransaccionRepository repository;

    public AnalyticsService(TransaccionRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> obtenerDashboard(Long usuarioId) {
        List<Transaccion> transacciones = repository.findByUsuarioId(usuarioId);
        Map<String, Object> dashboard = new HashMap<>();
        int anioActual = LocalDate.now().getYear();

        double totalIngresos = transacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
            .mapToDouble(Transaccion::getMonto).sum();

        double totalGastos = transacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
            .mapToDouble(Transaccion::getMonto).sum();

        dashboard.put("totalIngresos", totalIngresos);
        dashboard.put("totalGastos", totalGastos);
        dashboard.put("balance", totalIngresos - totalGastos);

        Map<String, Double> gastosPorCategoria = transacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
            .collect(Collectors.groupingBy(
                t -> t.getCategoria().getNombre(),
                Collectors.summingDouble(Transaccion::getMonto)
            ));
        dashboard.put("gastosPorCategoria", gastosPorCategoria);

        double prediccion = predecirGastoMensual(transacciones);
        dashboard.put("prediccionProximoMes", prediccion);

        // Gasto del mes actual para calcular tendencia
        int mesActual = LocalDate.now().getMonthValue();
        double gastoMesActual = transacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.GASTO
                && t.getFecha().getMonthValue() == mesActual
                && t.getFecha().getYear() == anioActual)
            .mapToDouble(Transaccion::getMonto).sum();
        dashboard.put("gastoMesActual", gastoMesActual);

        // Tendencia: 😬 si prediccion > gastoMesActual*1.2, 🔥 si gastoMesActual > prediccion*1.2, ✅ normal
        String tendencia;
        if (gastoMesActual > 0 && prediccion > 0 && gastoMesActual > prediccion * 1.2) {
            tendencia = "🔥";
        } else if (prediccion > 0 && gastoMesActual > 0 && prediccion > gastoMesActual * 1.2) {
            tendencia = "😬";
        } else if (prediccion > 0 && gastoMesActual == 0) {
            tendencia = "😬";
        } else if (prediccion == 0 && gastoMesActual > 0) {
            tendencia = "🔥";
        } else {
            tendencia = "✅";
        }
        dashboard.put("tendencia", tendencia);

        // Gastos e ingresos por mes para gráfico de líneas (últimos 12 meses)
        dashboard.put("gastosPorMes", gastosPorMes(usuarioId, anioActual));
        dashboard.put("ingresosPorMes", ingresosPorMes(usuarioId, anioActual));

        dashboard.put("consejos", generarConsejos(
            transacciones,
            totalIngresos,
            totalGastos,
            gastosPorCategoria,
            prediccion
        ));

        return dashboard;
    }

    public double predecirGastoMensual(List<Transaccion> transacciones) {
        List<Transaccion> gastos = transacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
            .toList();

        if (gastos.isEmpty()) return 0;

        int totalMeses = 3;
        double suma = 0;
        int mesesConDatos = 0;

        for (int i = 0; i < totalMeses; i++) {
            LocalDate inicio = LocalDate.now().minusMonths(i + 1).withDayOfMonth(1);
            LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());

            int mes = inicio.getMonthValue();
            int anio = inicio.getYear();

            double totalMes = gastos.stream()
                .filter(t -> t.getFecha().getMonthValue() == mes && t.getFecha().getYear() == anio)
                .mapToDouble(Transaccion::getMonto)
                .sum();

            if (totalMes > 0) {
                suma += totalMes;
                mesesConDatos++;
            }
        }

        return mesesConDatos > 0 ? suma / mesesConDatos : 0;
    }

    public Map<Integer, Double> gastosPorMes(Long usuarioId, int anio) {
        List<Transaccion> gastos = repository.findByUsuarioId(usuarioId).stream()
            .filter(t -> t.getTipo() == TipoTransaccion.GASTO && t.getFecha().getYear() == anio)
            .toList();

        Map<Integer, Double> porMes = new HashMap<>();
        for (int m = 1; m <= 12; m++) {
            final int mes = m;
            double total = gastos.stream()
                .filter(t -> t.getFecha().getMonthValue() == mes)
                .mapToDouble(Transaccion::getMonto)
                .sum();
            porMes.put(mes, total);
        }
        return porMes;
    }

    public Map<Integer, Double> ingresosPorMes(Long usuarioId, int anio) {
        List<Transaccion> ingresos = repository.findByUsuarioId(usuarioId).stream()
            .filter(t -> t.getTipo() == TipoTransaccion.INGRESO && t.getFecha().getYear() == anio)
            .toList();

        Map<Integer, Double> porMes = new HashMap<>();
        for (int m = 1; m <= 12; m++) {
            final int mes = m;
            double total = ingresos.stream()
                .filter(t -> t.getFecha().getMonthValue() == mes)
                .mapToDouble(Transaccion::getMonto)
                .sum();
            porMes.put(mes, total);
        }
        return porMes;
    }

    private List<ConsejoDTO> generarConsejos(
        List<Transaccion> transacciones,
        double totalIngresos,
        double totalGastos,
        Map<String, Double> gastosPorCategoria,
        double prediccion) {

    List<ConsejoDTO> consejos = new ArrayList<>();

    //---------------------------------------
    // Balance
    //---------------------------------------

    if (totalIngresos > 0) {

        double ahorro = totalIngresos - totalGastos;
        double porcentaje = (ahorro / totalIngresos) * 100;

        if (porcentaje >= 20) {

            consejos.add(new ConsejoDTO(
                    "Excelente ahorro",
                    "Lograste ahorrar el "
                            + String.format("%.1f", porcentaje)
                            + "% de tus ingresos.",
                    "success",
                    "💰",
                    1
            ));

        } else if (porcentaje >= 10) {

            consejos.add(new ConsejoDTO(
                    "Buen progreso",
                    "Podrías incrementar un poco más tu ahorro para alcanzar el 20%.",
                    "info",
                    "📈",
                    2
            ));

        } else if (porcentaje >= 0) {

            consejos.add(new ConsejoDTO(
                    "Ahorro bajo",
                    "Tu capacidad de ahorro es baja. Revisa tus gastos principales.",
                    "warning",
                    "⚠️",
                    1
            ));

        } else {

            consejos.add(new ConsejoDTO(
                    "Déficit",
                    "Este mes gastaste más dinero del que ingresó.",
                    "danger",
                    "🚨",
                    1
            ));

        }

    }

    //---------------------------------------
    // Categoría con mayor gasto
    //---------------------------------------

    if (!gastosPorCategoria.isEmpty()) {

        Map.Entry<String, Double> mayor =
                gastosPorCategoria.entrySet()
                        .stream()
                        .max(Map.Entry.comparingByValue())
                        .orElse(null);

        if (mayor != null) {

            double porcentaje =
                    (mayor.getValue() / totalGastos) * 100;

            if (porcentaje >= 35) {

                consejos.add(new ConsejoDTO(
                        "Mayor gasto",
                        "La categoría "
                                + mayor.getKey()
                                + " representa el "
                                + String.format("%.1f", porcentaje)
                                + "% de tus gastos. Reduciendo un 10% ahorrarías aproximadamente S/"
                                + String.format("%.2f", mayor.getValue() * 0.10),
                        "warning",
                        "🍽️",
                        1
                ));

            }

        }

    }

    //---------------------------------------
    // Predicción
    //---------------------------------------

    if (prediccion > totalGastos * 1.10 && totalGastos > 0) {

        consejos.add(new ConsejoDTO(
                "Predicción",
                "Según tu comportamiento podrías gastar S/"
                        + String.format("%.2f", prediccion)
                        + " el próximo mes.",
                "info",
                "📊",
                3
        ));

    }

    //---------------------------------------
    // Compras pequeñas
    //---------------------------------------

    long comprasPequenas =
            transacciones.stream()
                    .filter(t ->
                            t.getTipo() == TipoTransaccion.GASTO
                                    && t.getMonto() < 20)
                    .count();

    if (comprasPequenas >= 10) {

        consejos.add(new ConsejoDTO(
                "Compras frecuentes",
                "Detectamos muchas compras pequeñas. Revisa estos gastos porque suelen acumularse.",
                "warning",
                "🛒",
                2
        ));

    }

    //---------------------------------------
    // Sin ingresos
    //---------------------------------------

    if (totalIngresos == 0) {

        consejos.add(new ConsejoDTO(
                "Registra ingresos",
                "Aún no registras ingresos este mes. Esto permitirá generar mejores recomendaciones.",
                "info",
                "💵",
                2
        ));

    }

    consejos.sort(Comparator.comparingInt(ConsejoDTO::getPrioridad));

    return consejos;

        }
}
