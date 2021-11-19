package com.grupoadec.appsavephotosqlite;

import android.graphics.Bitmap;

public class ModelContacto {

    private Integer id;
    private String nombre;
    private String telefono;
    private String image;
    private String longitud;
    private String latitud;

    public ModelContacto(Integer id, String nombre, String telefono, String image, String longitud, String latitud) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.image = image;
        this.longitud = longitud;
        this.latitud = latitud;
    }

    public ModelContacto(){};

    public Integer getId() {return id;}

    public void setId(Integer id) {this.id = id;}

    public String getNombre() {return nombre;}

    public void setNombre(String nombre) {this.nombre = nombre;}

    public String getTelefono() {return telefono;}

    public void setTelefono(String telefono) {this.telefono = telefono;}

    public String getImage() {return image;}

    public void setImage(String image) {this.image = image;}

    public String getLongitud() {return longitud;}

    public void setLongitud(String longitud) {this.longitud = longitud;}

    public String getLatitud() {return latitud;}

    public void setLatitud(String latitud) {this.latitud = latitud;}
}
