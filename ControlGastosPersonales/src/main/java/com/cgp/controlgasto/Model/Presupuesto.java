package com.cgp.controlgasto.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "presupuestos")
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double montoLimite;

    private int mes;
    private int anio;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Constructor vacío
    public Presupuesto() {}

    // Constructor
    public Presupuesto(double montoLimite, int mes, int anio, Usuario usuario) {
        this.montoLimite = montoLimite;
        this.mes = mes;
        this.anio = anio;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Long getId() { return id; }

    public double getMontoLimite() { return montoLimite; }
    public void setMontoLimite(double montoLimite) { this.montoLimite = montoLimite; }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}