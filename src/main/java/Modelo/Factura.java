package Modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Factura {
    private int id;
    private int pacienteId;
    private String pacienteNombre; // de JOIN
    private Integer citaId; // cita relacionada (opcional)
    private LocalDateTime fecha;
    private BigDecimal subtotal;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal descuentoMonto;
    private BigDecimal impuestoPorcentaje;
    private BigDecimal impuestoMonto;
    private BigDecimal total;
    private String estadoPago; // Pendiente, Pagada, Parcial, Anulada
    private String metodoPagoPreferido; // método de pago indicado al generar la factura
    private BigDecimal pagado; // de JOIN/subconsulta con pagos
    private BigDecimal saldoPendiente; // total - pagado

    public Factura() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPacienteId() { return pacienteId; }
    public void setPacienteId(int pacienteId) { this.pacienteId = pacienteId; }

    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }

    public Integer getCitaId() { return citaId; }
    public void setCitaId(Integer citaId) { this.citaId = citaId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDescuentoPorcentaje() { return descuentoPorcentaje; }
    public void setDescuentoPorcentaje(BigDecimal descuentoPorcentaje) { this.descuentoPorcentaje = descuentoPorcentaje; }

    public BigDecimal getDescuentoMonto() { return descuentoMonto; }
    public void setDescuentoMonto(BigDecimal descuentoMonto) { this.descuentoMonto = descuentoMonto; }

    public BigDecimal getImpuestoPorcentaje() { return impuestoPorcentaje; }
    public void setImpuestoPorcentaje(BigDecimal impuestoPorcentaje) { this.impuestoPorcentaje = impuestoPorcentaje; }

    public BigDecimal getImpuestoMonto() { return impuestoMonto; }
    public void setImpuestoMonto(BigDecimal impuestoMonto) { this.impuestoMonto = impuestoMonto; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }

    public String getMetodoPagoPreferido() { return metodoPagoPreferido; }
    public void setMetodoPagoPreferido(String metodoPagoPreferido) { this.metodoPagoPreferido = metodoPagoPreferido; }

    public BigDecimal getPagado() { return pagado; }
    public void setPagado(BigDecimal pagado) { this.pagado = pagado; }

    public BigDecimal getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(BigDecimal saldoPendiente) { this.saldoPendiente = saldoPendiente; }
}
