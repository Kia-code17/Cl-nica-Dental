package Modelo;

public class Doctor {
    private int id;
    private String nombre;
    private String cedula;
    private String especialidad;
    private String telefono;
    private String email;
    private boolean activo;

    public Doctor() {}

    public Doctor(int id, String nombre, String cedula, String especialidad,
                  String telefono, String email, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.cedula = cedula;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.email = email;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() { return "Dr(a). " + nombre + " - " + especialidad; }
}
