package com.example.base64;

public class RegistroModel {
    private int id;
    private String imagenBase64;
    private String descripcionTexto;
    private String fechaRegistro;

    public RegistroModel(int id, String imagenBase64, String descripcionTexto, String fechaRegistro) {
        this.id = id;
        this.imagenBase64 = imagenBase64;
        this.descripcionTexto = descripcionTexto;
        this.fechaRegistro = fechaRegistro;
    }

    public int getId() { return id; }
    public String getImagenBase64() { return imagenBase64; }
    public String getDescripcionTexto() { return descripcionTexto; }
    public String getFechaRegistro() { return fechaRegistro; }
}