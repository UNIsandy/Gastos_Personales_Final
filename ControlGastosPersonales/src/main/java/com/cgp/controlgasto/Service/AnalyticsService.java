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
    int mesActual = LocalDate.now().getMonthValue();
    int anioActual = LocalDate.now().getYear();

    //---------------------------------------
    // Balance general
    //---------------------------------------

    if (totalIngresos > 0) {
        double ahorro = totalIngresos - totalGastos;
        double porcentaje = (ahorro / totalIngresos) * 100;

        if (porcentaje >= 20) {
            consejos.add(new ConsejoDTO(
                    "Excelente ahorro",
                    "Lograste ahorrar el "
                            + String.format("%.1f", porcentaje)
                            + "% de tus ingresos. Sigue así, estás construyendo un buen colchón financiero.",
                    "success",
                    "💰",
                    1
            ));
        } else if (porcentaje >= 10) {
            consejos.add(new ConsejoDTO(
                    "Buen progreso",
                    "Ahorras el " + String.format("%.1f", porcentaje)
                            + "% de tus ingresos. Si reduces solo S/ "
                            + String.format("%.2f", totalIngresos * 0.05)
                            + " en gastos este mes, llegarías al 15% de ahorro.",
                    "info",
                    "📈",
                    2
            ));
        } else if (porcentaje >= 0) {
            double metaAhorro = totalIngresos * 0.10;
            double falta = metaAhorro - ahorro;
            consejos.add(new ConsejoDTO(
                    "Ahorro bajo",
                    "Tu ahorro es solo del " + String.format("%.1f", porcentaje)
                            + "% de tus ingresos. Necesitas reducir unos S/ "
                            + String.format("%.2f", falta)
                            + " en gastos este mes para llegar al 10%. Revisa tus categorías principales.",
                    "warning",
                    "⚠️",
                    1
            ));
        } else {
            double exceso = -ahorro;
            consejos.add(new ConsejoDTO(
                    "Déficit",
                    "Gastaste S/ " + String.format("%.2f", exceso)
                            + " más de lo que ingresaste. Si reduces S/ "
                            + String.format("%.2f", exceso / 15)
                            + " al día en los próximos 15 días, equilibrarías tu balance.",
                    "danger",
                    "🚨",
                    1
            ));
        }
    }

    //---------------------------------------
    // Análisis por categoría
    //---------------------------------------

    if (!gastosPorCategoria.isEmpty() && totalGastos > 0) {
        List<Map.Entry<String, Double>> categoriasOrdenadas = gastosPorCategoria.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .toList();

        for (int i = 0; i < Math.min(3, categoriasOrdenadas.size()); i++) {
            Map.Entry<String, Double> entry = categoriasOrdenadas.get(i);
            String categoria = entry.getKey();
            double monto = entry.getValue();
            double pct = (monto / totalGastos) * 100;

            if (pct >= 25) {
                double ahorroPotencial = monto * 0.15;
                double diario = ahorroPotencial / 20;
                consejos.add(new ConsejoDTO(
                        categoria + " es tu mayor gasto",
                        "Representa el " + String.format("%.1f", pct)
                                + "% de tus gastos (S/ " + String.format("%.2f", monto)
                                + "). Reduciendo S/ " + String.format("%.2f", diario)
                                + " al día en esta categoría, en 20 días ahorrarías S/ "
                                + String.format("%.2f", ahorroPotencial) + ".",
                        "warning",
                        i == 0 ? "🍽️" : (i == 1 ? "🚗" : "🛍️"),
                        1
                ));
            } else if (pct >= 15) {
                double ahorroPotencial = monto * 0.10;
                consejos.add(new ConsejoDTO(
                        "Revisa " + categoria,
                        "Gastaste S/ " + String.format("%.2f", monto)
                                + " en " + categoria.toLowerCase()
                                + " (" + String.format("%.1f", pct)
                                + "% del total). Un pequeño ajuste del 10% te ahorraría S/ "
                                + String.format("%.2f", ahorroPotencial) + " este mes.",
                        "info",
                        "📋",
                        2
                ));
            }
        }
    }

    //---------------------------------------
    // Predicción vs gasto actual
    //---------------------------------------

    if (prediccion > 0 && totalGastos > 0) {
        double gastoDiarioActual = totalGastos / 30.0;
        double gastoDiarioPredicho = prediccion / 30.0;

        if (prediccion > totalGastos * 1.15) {
            double aumento = prediccion - totalGastos;
            double reduccionDiaria = aumento / 30;
            consejos.add(new ConsejoDTO(
                    "Gasto en aumento",
                    "Se prevé que el próximo mes gastes S/ "
                            + String.format("%.2f", aumento)
                            + " más que este mes. Si reduces S/ "
                            + String.format("%.2f", reduccionDiaria)
                            + " al día desde ahora, evitarías este aumento.",
                    "warning",
                    "📊",
                    1
            ));
        }
    }

    //---------------------------------------
    // Comparación con meses anteriores
    //---------------------------------------

    double gastoMesAnterior = transacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
            .filter(t -> {
                int m = t.getFecha().getMonthValue();
                int y = t.getFecha().getYear();
                return m == (mesActual == 1 ? 12 : mesActual - 1)
                        && y == (mesActual == 1 ? anioActual - 1 : anioActual);
            })
            .mapToDouble(Transaccion::getMonto).sum();

    double gastoEsteMes = transacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.GASTO
                    && t.getFecha().getMonthValue() == mesActual
                    && t.getFecha().getYear() == anioActual)
            .mapToDouble(Transaccion::getMonto).sum();

    if (gastoMesAnterior > 0 && gastoEsteMes > 0) {
        double diff = gastoEsteMes - gastoMesAnterior;
        double pctCambio = (diff / gastoMesAnterior) * 100;

        if (pctCambio > 20) {
            double diario = diff / LocalDate.now().lengthOfMonth();
            consejos.add(new ConsejoDTO(
                    "Gasto subió respecto al mes pasado",
                    "Este mes gastas " + String.format("%.0f", pctCambio)
                            + "% más que el mes anterior (S/ " + String.format("%.2f", diario)
                            + " más por día). Identifica qué gastos nuevos aparecieron.",
                    "warning",
                    "📈",
                    2
            ));
        } else if (pctCambio < -15) {
            consejos.add(new ConsejoDTO(
                    "Vas mejor que el mes pasado",
                    "Has reducido tus gastos en un " + String.format("%.0f", Math.abs(pctCambio))
                            + "% comparado con el mes anterior. ¡Sigue así!",
                    "success",
                    "🎉",
                    2
            ));
        }
    }

    //---------------------------------------
    // Pequeños gastos frecuentes
    //---------------------------------------

    long comprasPequenas = transacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.GASTO && t.getMonto() < 20)
            .count();

    if (comprasPequenas >= 10) {
        double totalPequeno = transacciones.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.GASTO && t.getMonto() < 20)
                .mapToDouble(Transaccion::getMonto).sum();
        consejos.add(new ConsejoDTO(
                comprasPequenas >= 15 ? "Muchos gastos pequeños" : "Compras pequeñas frecuentes",
                "Tienes " + comprasPequenas + " gastos menores a S/ 20 que suman S/ "
                        + String.format("%.2f", totalPequeno)
                        + ". Si reduces la mitad, ahorrarías S/ "
                        + String.format("%.2f", totalPequeno * 0.50) + " este mes.",
                "info",
                "🛒",
                2
        ));
    }

    //---------------------------------------
    // Categoría con muchos gastos chicos
    //---------------------------------------

    if (!gastosPorCategoria.isEmpty()) {
        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            if (entry.getValue() >= totalGastos * 0.20 && entry.getValue() >= 100) {
                double ahorro3Dias = entry.getValue() / 30 * 3;
                consejos.add(new ConsejoDTO(
                        "Reto de 3 días para " + entry.getKey(),
                        "Si durante 3 días reduces al máximo el gasto en "
                                + entry.getKey().toLowerCase()
                                + ", podrías ahorrar aproximadamente S/ "
                                + String.format("%.2f", ahorro3Dias)
                                + ". Úsalo para tu meta de ahorro.",
                        "info",
                        "🎯",
                        3
                ));
                break;
            }
        }
    }

    //---------------------------------------
    // Sin ingresos
    //---------------------------------------

    if (totalIngresos == 0) {
        consejos.add(new ConsejoDTO(
                "Registra tus ingresos",
                "Aún no registras ingresos. Sin esta información no puedo darte recomendaciones precisas.",
                "info",
                "💵",
                2
        ));
    }

    if (totalIngresos == 0 && totalGastos > 0) {
        consejos.add(new ConsejoDTO(
                "Gastas sin ingresos",
                "Tienes gastos registrados pero ningún ingreso. Tus metas de ahorro no son sostenibles así.",
                "danger",
                "🚨",
                1
        ));
    }

    consejos.sort(Comparator.comparingInt(ConsejoDTO::getPrioridad));
    return consejos;
    }
}
