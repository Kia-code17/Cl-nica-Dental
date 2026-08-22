package Modelo;

import java.math.BigDecimal;

/**
 * Línea de una factura: puede ser un servicio/tratamiento (accesorioId == null)
 * o un producto de inventario (accesorioId != null), en cuyo caso al facturarse
 * descuenta stock automáticamente (ver FacturaDAO.crear / AccesorioDAO.descontarStockPorVenta).
 */
public class DetalleFactura {
    private int id;
    private Integer tratamientoId;
    private Integer accesorioId;
    private String descripcion;
    private int cantidad;
    private BigDecimal precioUnitario;

    public DetalleFactura() {}

    public DetalleFactura(String descripcion, int cantidad, BigDecimal precioUnitario, Integer accesorioId) {
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.accesorioId = accesorioId;
    }

    public boolean esProductoInventario() {
        return accesorioId != null;
    }

    public BigDecimal getSubtotalLinea() {
        if (precioUnitario == null) return BigDecimal.ZERO;
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getTratamientoId() { return tratamientoId; }
    public void setTratamientoId(Integer tratamientoId) { this.tratamientoId = tratamientoId; }

    public Integer getAccesorioId() { return accesorioId; }
    public void setAccesorioId(Integer accesorioId) { this.accesorioId = accesorioId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
