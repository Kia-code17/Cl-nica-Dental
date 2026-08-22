package Modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pago registrado contra una factura (una factura puede tener varios pagos parciales). */
public class Pago {
    private int id;
    private int facturaId;
    private LocalDateTime fecha;
    private BigDecimal monto;
    private String metodo; // Efectivo, Tarjeta, Transferencia, Cheque

    public Pago() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFacturaId() { return facturaId; }
    public void setFacturaId(int facturaId) { this.facturaId = facturaId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
}
