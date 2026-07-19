package com.cgp.controlgasto.Model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "metas_ahorro")
public class MetaAhorro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Double montoObjetivo;
    private Double montoActual;
    private LocalDate fechaLimite;
    private LocalDate fechaCreacion; 
    private boolean activa;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public MetaAhorro() {}

    public MetaAhorro(String nombre, Double montoObjetivo, LocalDate fechaLimite, Usuario usuario) {
        this.nombre = nombre;
        this.montoObjetivo = montoObjetivo;
        this.montoActual = 0.0;
        this.fechaLimite = fechaLimite;
        this.fechaCreacion = LocalDate.now();
        this.activa = true;
        this.usuario = usuario;
    }

    public double getProgreso() {
        if (montoObjetivo == null || montoObjetivo <= 0) return 0;
        return Math.min(100.0, (montoActual / montoObjetivo) * 100);
    }

    public String getNivel() {
        double pct = getProgreso();
        if (pct >= 100) return "ORO";
        if (pct >= 50) return "PLATA";
        return "BRONCE";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Double getMontoObjetivo() { return montoObjetivo; }
    public void setMontoObjetivo(Double montoObjetivo) { this.montoObjetivo = montoObjetivo; }
    public Double getMontoActual() { return montoActual; }
    public void setMontoActual(Double montoActual) { this.montoActual = montoActual; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }    
}
