package com.cgp.controlgasto.Model;

import java.util.Map;

// Clase DTO para representar el resumen mensual de gastos e ingresos
public class ReporteMensual {

private final int anio;
private final int mes;
private final double totalIngresos;
private final double totalGastos;
private final double balance;
private final Map<String, Double> gastosPorCategoria;

    public ReporteMensual(int anio, int mes, double totalIngresos, double totalGastos,
                          double balance, Map<String, Double> gastosPorCategoria) {
        this.anio = anio;
        this.mes = mes;
        this.totalIngresos = totalIngresos;
        this.totalGastos = totalGastos;
        this.balance = balance;
        this.gastosPorCategoria = gastosPorCategoria;
    }

    // Getters
    public int getAnio() { return anio; }

    public int getMes() { return mes; }

    public double getTotalIngresos() { return totalIngresos; }

    public double getTotalGastos() { return totalGastos; }

    public double getBalance() { return balance; }

    public Map<String, Double> getGastosPorCategoria() { 
        return gastosPorCategoria; 
    }
}
