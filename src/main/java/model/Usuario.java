package model;

public class Usuario {

    private int id;
    private String nombre;
    private String correo;
    private String contrasena;
    private String rol;
    private String rut;
    private boolean activo;

    // Constructor utilizado por UsuarioDao
    public Usuario(
            int id,
            String nombre,
            String correo,
            String contrasena,
            String rol,
            String rut) {

        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
        this.rut = rut;
        this.activo = true;
    }

    // Constructor alternativo
    public Usuario(
            int id,
            String nombre,
            String correo,
            String contrasena,
            String rol,
            String rut,
            boolean activo) {

        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
        this.rut = rut;
        this.activo = activo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

