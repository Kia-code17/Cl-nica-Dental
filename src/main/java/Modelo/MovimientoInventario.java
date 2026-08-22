package Modelo;

import java.time.LocalDateTime;

/** Fila del historial de entradas/salidas de un accesorio (tabla movimientos_inventario). */
public class MovimientoInventario {
    private int id;
    private int accesorioId;
    private String accesorioNombre; // de JOIN
    private String tipo; // Entrada, Salida
    private int cantidad;
    private LocalDateTime fecha;
    private String nota;
    private Integer facturaId; // si la salida vino de una venta/factura

    public MovimientoInventario() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAccesorioId() { return accesorioId; }
    public void setAccesorioId(int accesorioId) { this.accesorioId = accesorioId; }

    public String getAccesorioNombre() { return accesorioNombre; }
    public void setAccesorioNombre(String accesorioNombre) { this.accesorioNombre = accesorioNombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }

    public Integer getFacturaId() { return facturaId; }
    public void setFacturaId(Integer facturaId) { this.facturaId = facturaId; }
}
