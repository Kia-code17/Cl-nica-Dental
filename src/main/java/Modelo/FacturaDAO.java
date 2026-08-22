package Modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAO {

    private static final String SELECT_BASE =
        "SELECT f.id, f.paciente_id, p.nombre AS paciente_nombre, f.cita_id, f.fecha, "
        + "f.subtotal, f.descuento_porcentaje, f.descuento_monto, f.impuesto_porcentaje, f.impuesto_monto, "
        + "f.total, f.estado_pago, f.metodo_pago_preferido, "
        + "COALESCE((SELECT SUM(pg.monto) FROM pagos pg WHERE pg.factura_id = f.id), 0) AS pagado "
        + "FROM facturas f JOIN pacientes p ON p.id = f.paciente_id ";

    private final AccesorioDAO accesorioDAO = new AccesorioDAO();

    /**
     * Crea una factura con una o más líneas de detalle (servicios/tratamientos y/o productos de
     * inventario), calcula subtotal/descuento/impuesto/total, y si alguna línea es un producto de
     * inventario descuenta el stock correspondiente dentro de la MISMA transacción (si el stock no
     * alcanza, se revierte todo y no se crea la factura).
     *
     * @param pacienteId paciente al que se factura
     * @param citaId cita relacionada (opcional, puede ser null)
     * @param items líneas de la factura (al menos una)
     * @param descuentoPorcentaje 0-100, aplicado sobre el subtotal
     * @param impuestoPorcentaje 0-100, aplicado sobre (subtotal - descuento)
     * @param metodoPagoPreferido método de pago indicado al generar la factura (informativo)
     */
    public int crear(int pacienteId, Integer citaId, List<DetalleFactura> items,
                      BigDecimal descuentoPorcentaje, BigDecimal impuestoPorcentaje,
                      String metodoPagoPreferido) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new SQLException("La factura debe tener al menos un concepto (servicio o producto).");
        }
        BigDecimal descPct = descuentoPorcentaje == null ? BigDecimal.ZERO : descuentoPorcentaje;
        BigDecimal impPct = impuestoPorcentaje == null ? BigDecimal.ZERO : impuestoPorcentaje;
        if (descPct.compareTo(BigDecimal.ZERO) < 0 || descPct.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new SQLException("El descuento debe estar entre 0 y 100%.");
        }
        if (impPct.compareTo(BigDecimal.ZERO) < 0 || impPct.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new SQLException("El impuesto debe estar entre 0 y 100%.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleFactura it : items) {
            if (it.getCantidad() <= 0) {
                throw new SQLException("La cantidad de \"" + it.getDescripcion() + "\" debe ser mayor a cero.");
            }
            if (it.getPrecioUnitario() == null || it.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                throw new SQLException("El precio de \"" + it.getDescripcion() + "\" debe ser mayor a cero.");
            }
            subtotal = subtotal.add(it.getSubtotalLinea());
        }

        BigDecimal descuentoMonto = subtotal.multiply(descPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal base = subtotal.subtract(descuentoMonto);
        BigDecimal impuestoMonto = base.multiply(impPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(impuestoMonto);

        Connection con = new Conexion().getConnetion();
        try {
            con.setAutoCommit(false);

            int facturaId;
            String sqlFactura = "INSERT INTO facturas "
                    + "(paciente_id, cita_id, subtotal, descuento_porcentaje, descuento_monto, "
                    + "impuesto_porcentaje, impuesto_monto, total, estado_pago, metodo_pago_preferido) "
                    + "VALUES (?,?,?,?,?,?,?,?,'Pendiente',?)";
            try (PreparedStatement ps = con.prepareStatement(sqlFactura, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, pacienteId);
                if (citaId != null) ps.setInt(2, citaId); else ps.setNull(2, Types.INTEGER);
                ps.setBigDecimal(3, subtotal);
                ps.setBigDecimal(4, descPct);
                ps.setBigDecimal(5, descuentoMonto);
                ps.setBigDecimal(6, impPct);
                ps.setBigDecimal(7, impuestoMonto);
                ps.setBigDecimal(8, total);
                ps.setString(9, metodoPagoPreferido);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    facturaId = rs.getInt(1);
                }
            }

            for (DetalleFactura it : items) {
                Integer tratamientoId = null;
                if (!it.esProductoInventario()) {
                    // Línea de servicio/tratamiento
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO tratamientos (cita_id, descripcion, costo) VALUES (?,?,?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        if (citaId != null) ps.setInt(1, citaId); else ps.setNull(1, Types.INTEGER);
                        ps.setString(2, it.getDescripcion());
                        ps.setBigDecimal(3, it.getPrecioUnitario());
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            rs.next();
                            tratamientoId = rs.getInt(1);
                        }
                    }
                } else {
                    // Línea de producto de inventario: valida y descuenta stock en esta misma transacción
                    accesorioDAO.descontarStockPorVenta(con, it.getAccesorioId(), it.getCantidad(), facturaId);
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO detalles_factura (factura_id, tratamiento_id, accesorio_id, descripcion, cantidad, precio) "
                        + "VALUES (?,?,?,?,?,?)")) {
                    ps.setInt(1, facturaId);
                    if (tratamientoId != null) ps.setInt(2, tratamientoId); else ps.setNull(2, Types.INTEGER);
                    if (it.getAccesorioId() != null) ps.setInt(3, it.getAccesorioId()); else ps.setNull(3, Types.INTEGER);
                    ps.setString(4, it.getDescripcion());
                    ps.setInt(5, it.getCantidad());
                    ps.setBigDecimal(6, it.getPrecioUnitario());
                    ps.executeUpdate();
                }
            }

            con.commit();
            return facturaId;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
            con.close();
        }
    }

    public void registrarPago(int facturaId, BigDecimal monto, String metodo) throws SQLException {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("El monto del pago debe ser mayor a cero.");
        }
        Connection con = new Conexion().getConnetion();
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO pagos (factura_id, monto, metodo) VALUES (?,?,?)")) {
                ps.setInt(1, facturaId);
                ps.setBigDecimal(2, monto);
                ps.setString(3, metodo);
                ps.executeUpdate();
            }

            BigDecimal total = null, pagado = null;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT total FROM facturas WHERE id = ?")) {
                ps.setInt(1, facturaId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) total = rs.getBigDecimal(1);
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COALESCE(SUM(monto),0) FROM pagos WHERE factura_id = ?")) {
                ps.setInt(1, facturaId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) pagado = rs.getBigDecimal(1);
                }
            }

            String nuevoEstado = "Pendiente";
            if (total != null && pagado != null) {
                if (pagado.compareTo(total) >= 0) nuevoEstado = "Pagada";
                else if (pagado.compareTo(BigDecimal.ZERO) > 0) nuevoEstado = "Parcial";
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE facturas SET estado_pago = ? WHERE id = ?")) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, facturaId);
                ps.executeUpdate();
            }

            con.commit();
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
            con.close();
        }
    }

    public List<Factura> listarTodas() throws SQLException {
        List<Factura> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY f.fecha DESC";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Factura> listarPorPaciente(int pacienteId) throws SQLException {
        List<Factura> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE f.paciente_id = ? ORDER BY f.fecha DESC";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pacienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Factura obtenerPorId(int facturaId) throws SQLException {
        String sql = SELECT_BASE + "WHERE f.id = ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, facturaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    /** Líneas de detalle de una factura (servicios y/o productos), para mostrarlas o imprimir el comprobante. */
    public List<DetalleFactura> obtenerDetalle(int facturaId) throws SQLException {
        List<DetalleFactura> lista = new ArrayList<>();
        String sql = "SELECT id, tratamiento_id, accesorio_id, descripcion, cantidad, precio "
                + "FROM detalles_factura WHERE factura_id = ? ORDER BY id";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, facturaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleFactura d = new DetalleFactura();
                    d.setId(rs.getInt("id"));
                    int tratId = rs.getInt("tratamiento_id");
                    d.setTratamientoId(rs.wasNull() ? null : tratId);
                    int accId = rs.getInt("accesorio_id");
                    d.setAccesorioId(rs.wasNull() ? null : accId);
                    d.setDescripcion(rs.getString("descripcion"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    /** Pagos registrados para una factura, para mostrarlos o imprimir el comprobante. */
    public List<Pago> obtenerPagos(int facturaId) throws SQLException {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT id, factura_id, fecha, monto, metodo FROM pagos WHERE factura_id = ? ORDER BY fecha";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, facturaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pago p = new Pago();
                    p.setId(rs.getInt("id"));
                    p.setFacturaId(rs.getInt("factura_id"));
                    p.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    p.setMonto(rs.getBigDecimal("monto"));
                    p.setMetodo(rs.getString("metodo"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    private Factura mapear(ResultSet rs) throws SQLException {
        Factura f = new Factura();
        f.setId(rs.getInt("id"));
        f.setPacienteId(rs.getInt("paciente_id"));
        f.setPacienteNombre(rs.getString("paciente_nombre"));
        int citaId = rs.getInt("cita_id");
        f.setCitaId(rs.wasNull() ? null : citaId);
        f.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        f.setSubtotal(rs.getBigDecimal("subtotal"));
        f.setDescuentoPorcentaje(rs.getBigDecimal("descuento_porcentaje"));
        f.setDescuentoMonto(rs.getBigDecimal("descuento_monto"));
        f.setImpuestoPorcentaje(rs.getBigDecimal("impuesto_porcentaje"));
        f.setImpuestoMonto(rs.getBigDecimal("impuesto_monto"));
        f.setTotal(rs.getBigDecimal("total"));
        f.setEstadoPago(rs.getString("estado_pago"));
        f.setMetodoPagoPreferido(rs.getString("metodo_pago_preferido"));
        BigDecimal pagado = rs.getBigDecimal("pagado");
        if (pagado == null) pagado = BigDecimal.ZERO;
        f.setPagado(pagado);
        f.setSaldoPendiente(f.getTotal().subtract(pagado));
        return f;
    }
}
