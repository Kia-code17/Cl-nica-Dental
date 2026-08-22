package Modelo;

/** Categoría de accesorios/materiales de inventario (ej: "Ortodoncia", "Anestesia"). */
public class Categoria {
    private int id;
    private String nombre;
    private String descripcion;

    public Categoria() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** Para que aparezca legible dentro de un JComboBox<Categoria>. */
    @Override
    public String toString() { return nombre; }
}
