package com.multicompany.sales_system.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank @Size(max = 255)
    private String nombre;

    @NotBlank @Size(max = 255)
    private String apellido;

    @NotBlank @Size(min = 8, max = 25) // ajusta a tu regla de negocio
    private String cedula;

    @NotBlank @Email @Size(max = 255)
    private String correo;

    @NotBlank @Size(min = 6, max = 255)
    private String contrasena;

    @NotBlank                      // COMPRADOR | VENDEDOR
    private String rol;

    // opcionales (coinciden con columnas)
    @Size(max = 25)  private String telefono;
    @Size(max = 255) private String direccion;
    @Size(max = 2)   private String genero;

    // getters/setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
}
