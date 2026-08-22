package Modelo;

/**
 * Representa un rol del sistema (tabla `roles`): Admin, Doctor,
 * Recepcionista, Asistente. La descripción se usa como texto de ayuda
 * al asignar el rol a un usuario.
 */
public class Rol {
    private int id;
    private String nombre;
    private String descripcion;

    public Rol() {}

    public Rol(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** Usado por JComboBox<Rol> para mostrar el nombre en pantalla. */
    @Override
    public String toString() { return nombre; }
}