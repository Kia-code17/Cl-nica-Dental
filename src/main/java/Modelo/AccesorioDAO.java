package Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccesorioDAO {

    private static final String SELECT_BASE =
        "SELECT a.id, a.nombre, a.descripcion, a.precio_costo, a.precio_venta, a.stock, a.stock_minimo, "
        + "a.categoria_id, c.nombre AS categoria_nombre "
        + "FROM accesorios a LEFT JOIN categorias c ON c.id = a.categoria_id ";

    public int crear(Accesorio a) throws SQLException {
        String sql = "INSERT INTO accesorios (nombre, descripcion, precio_costo, precio_venta, stock, stock_minimo, categoria_id) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getNombre());
            ps.setString(2, a.getDescripcion());
            ps.setBigDecimal(3, a.getPrecioCosto());
            ps.setBigDecimal(4, a.getPrecioVenta());
            ps.setInt(5, a.getStock());
            ps.setInt(6, a.getStockMinimo());
            if (a.getCategoriaId() != null) ps.setInt(7, a.getCategoriaId()); else ps.setNull(7, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void actualizar(Accesorio a) throws SQLException {
        String sql = "UPDATE accesorios SET nombre=?, descripcion=?, precio_costo=?, precio_venta=?, "
                + "stock=?, stock_minimo=?, categoria_id=? WHERE id=?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, a.getNombre());
            ps.setString(2, a.getDescripcion());
            ps.setBigDecimal(3, a.getPrecioCosto());
            ps.setBigDecimal(4, a.getPrecioVenta());
            ps.setInt(5, a.getStock());
            ps.setInt(6, a.getStockMinimo());
            if (a.getCategoriaId() != null) ps.setInt(7, a.getCategoriaId()); else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, a.getId());
            ps.executeUpdate();
        }
    }

    /** Registra un movimiento de inventario manual (entrada/salida) y ajusta el stock en la misma transacción. */
    public void registrarMovimiento(int accesorioId, String tipo, int cantidad, String nota) throws SQLException {
        Connection con = new Conexion().getConnetion();
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO movimientos_inventario (accesorio_id, tipo, cantidad, nota) VALUES (?,?,?,?)")) {
                ps.setInt(1, accesorioId);
                ps.setString(2, tipo);
                ps.setInt(3, cantidad);
                ps.setString(4, nota);
                ps.executeUpdate();
            }
            String ajuste = "Entrada".equals(tipo) ? "stock = stock + ?" : "stock = stock - ?";
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE accesorios SET " + ajuste + " WHERE id = ?")) {
                ps.setInt(1, cantidad);
                ps.setInt(2, accesorioId);
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

    /**
     * Descuenta stock por una línea de venta/factura, dentro de la MISMA transacción/conexión
     * que está usando FacturaDAO.crear (no abre ni cierra la conexión). Valida que haya stock
     * suficiente y deja registrado el movimiento con el id de la factura para trazabilidad.
     */
    public void descontarStockPorVenta(Connection con, int accesorioId, int cantidad, int facturaId) throws SQLException {
        int stockActual;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT stock FROM accesorios WHERE id = ? FOR UPDATE")) {
            ps.setInt(1, accesorioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("El producto de inventario seleccionado ya no existe.");
                stockActual = rs.getInt("stock");
            }
        }
        if (stockActual < cantidad) {
            throw new SQLException("Stock insuficiente para el producto (ID " + accesorioId
                    + "). Disponible: " + stockActual + ", solicitado: " + cantidad + ".");
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE accesorios SET stock = stock - ? WHERE id = ?")) {
            ps.setInt(1, cantidad);
            ps.setInt(2, accesorioId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO movimientos_inventario (accesorio_id, tipo, cantidad, nota, factura_id) "
                + "VALUES (?, 'Salida', ?, ?, ?)")) {
            ps.setInt(1, accesorioId);
            ps.setInt(2, cantidad);
            ps.setString(3, "Venta - factura #" + facturaId);
            ps.setInt(4, facturaId);
            ps.executeUpdate();
        }
    }

    public List<Accesorio> listarTodos() throws SQLException {
        List<Accesorio> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY a.nombre";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Accesorio> listarStockBajo() throws SQLException {
        List<Accesorio> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE a.stock <= a.stock_minimo ORDER BY a.nombre";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Accesorio> listarPorCategoria(int categoriaId) throws SQLException {
        List<Accesorio> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE a.categoria_id = ? ORDER BY a.nombre";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Historial completo de movimientos de un accesorio específico, más reciente primero. */
    public List<MovimientoInventario> listarMovimientos(int accesorioId) throws SQLException {
        String sql = "SELECT m.id, m.accesorio_id, a.nombre AS accesorio_nombre, m.tipo, m.cantidad, "
                + "m.fecha, m.nota, m.factura_id "
                + "FROM movimientos_inventario m JOIN accesorios a ON a.id = m.accesorio_id "
                + "WHERE m.accesorio_id = ? ORDER BY m.fecha DESC";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accesorioId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapearMovimientos(rs);
            }
        }
    }

    /** Últimos N movimientos de inventario de todos los accesorios (para revisión general). */
    public List<MovimientoInventario> listarMovimientosRecientes(int limite) throws SQLException {
        String sql = "SELECT m.id, m.accesorio_id, a.nombre AS accesorio_nombre, m.tipo, m.cantidad, "
                + "m.fecha, m.nota, m.factura_id "
                + "FROM movimientos_inventario m JOIN accesorios a ON a.id = m.accesorio_id "
                + "ORDER BY m.fecha DESC LIMIT ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                return mapearMovimientos(rs);
            }
        }
    }

    private List<MovimientoInventario> mapearMovimientos(ResultSet rs) throws SQLException {
        List<MovimientoInventario> lista = new ArrayList<>();
        while (rs.next()) {
            MovimientoInventario m = new MovimientoInventario();
            m.setId(rs.getInt("id"));
            m.setAccesorioId(rs.getInt("accesorio_id"));
            m.setAccesorioNombre(rs.getString("accesorio_nombre"));
            m.setTipo(rs.getString("tipo"));
            m.setCantidad(rs.getInt("cantidad"));
            m.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
            m.setNota(rs.getString("nota"));
            int facturaId = rs.getInt("factura_id");
            m.setFacturaId(rs.wasNull() ? null : facturaId);
            lista.add(m);
        }
        return lista;
    }

    private Accesorio mapear(ResultSet rs) throws SQLException {
        Accesorio a = new Accesorio();
        a.setId(rs.getInt("id"));
        a.setNombre(rs.getString("nombre"));
        a.setDescripcion(rs.getString("descripcion"));
        a.setPrecioCosto(rs.getBigDecimal("precio_costo"));
        a.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        a.setStock(rs.getInt("stock"));
        a.setStockMinimo(rs.getInt("stock_minimo"));
        int categoriaId = rs.getInt("categoria_id");
        a.setCategoriaId(rs.wasNull() ? null : categoriaId);
        a.setCategoriaNombre(rs.getString("categoria_nombre"));
        return a;
    }
}
