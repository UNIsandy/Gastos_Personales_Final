package com.cgp.controlgasto.dto;

public class ConsejoDTO {
    private String titulo;
    private String descripcion;
    private String tipo;
    private String icono;
    private int prioridad;

    public ConsejoDTO() {
    }

    public ConsejoDTO(String titulo, String descripcion,
                      String tipo, String icono, int prioridad) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.icono = icono;
        this.prioridad = prioridad;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }
}
