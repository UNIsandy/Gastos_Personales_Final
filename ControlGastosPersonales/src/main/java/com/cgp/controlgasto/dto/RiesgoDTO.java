package com.cgp.controlgasto.dto;

import java.util.List;

public class RiesgoDTO {
    private boolean riesgo;
    private String titulo;
    private String mensaje;
    private List<String> recomendaciones;

    public RiesgoDTO() {}

    public RiesgoDTO(boolean riesgo, String titulo, String mensaje, List<String> recomendaciones) {
        this.riesgo = riesgo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.recomendaciones = recomendaciones;
    }

    public boolean isRiesgo() { return riesgo; }
    public void setRiesgo(boolean riesgo) { this.riesgo = riesgo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public List<String> getRecomendaciones() { return recomendaciones; }
    public void setRecomendaciones(List<String> recomendaciones) { this.recomendaciones = recomendaciones; }
}
