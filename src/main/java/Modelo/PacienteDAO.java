package Modelo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla `pacientes`.
 * CRUD completo + búsqueda + filtros (estado, doctor) + validación de cédula
 * duplicada + exportación a CSV.
 */
public class PacienteDAO {

    public int crear(Paciente p) {
        String sql = "INSERT INTO pacientes (nombre, cedula, fecha_nacimiento, telefono, "
                + "email, direccion, alergias, foto_url, activo) VALUES (?,?,?,?,?,?,?,?,?)";
        Conexion cn = new Conexion();
        try (Connection con = cn.getConnetion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCedula());
            ps.setDate(3, p.getFechaNacimiento() != null ? Date.valueOf(p.getFechaNacimiento()) : null);
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getEmail());
            ps.setString(6, p.getDireccion());
            ps.setString(7, p.getAlergias());
            ps.setString(8, p.getFotoUrl());
            ps.setBoolean(9, p.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return -1;
    }

    public void actualizar(Paciente p) {
        String sql = "UPDATE pacientes SET nombre=?, cedula=?, fecha_nacimiento=?, telefono=?, "
                + "email=?, direccion=?, alergias=?, foto_url=?, activo=? WHERE id=?";
        Conexion cn = new Conexion();
        try (Connection con = cn.getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCedula());
            ps.setDate(3, p.getFechaNacimiento() != null ? Date.valueOf(p.getFechaNacimiento()) : null);
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getEmail());
            ps.setString(6, p.getDireccion());
            ps.setString(7, p.getAlergias());
            ps.setString(8, p.getFotoUrl());
            ps.setBoolean(9, p.isActivo());
            ps.setInt(10, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    public void eliminar(int id) {
        // Baja lógica: se recomienda no borrar físicamente por integridad con citas/facturas
        String sql = "UPDATE pacientes SET activo = FALSE WHERE id = ?";
        Conexion cn = new Conexion();
        try (Connection con = cn.getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    /** Reactiva a un paciente que estaba en baja lógica. */
    public void reactivar(int id) {
        String sql = "UPDATE pacientes SET activo = TRUE WHERE id = ?";
        Conexion cn = new Conexion();
        try (Connection con = cn.getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    public Paciente buscarPorId(int id) {
        String sql = "SELECT * FROM pacientes WHERE id = ?";
        Conexion cn = new Conexion();
        try (Connection con = cn.getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public List<Paciente> listarTodos() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM pacientes ORDER BY nombre";
        Conexion cn = new Conexion();
        try (Connection con = cn.getConnetion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return lista;
    }

    public List<Paciente> buscar(String termino) {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM pacientes WHERE nombre LIKE ? OR cedula LIKE ? OR telefono LIKE ? ORDER BY nombre";
        Conexion cn = new Conexion();
        try (Connection con = cn.getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + termino + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return lista;
    }

    /**
     * Filtra pacientes por estado y opcionalmente por texto de búsqueda.
     * @param activo true = solo activos, false = solo inactivos, null = todos
     * @param termino texto de búsqueda (nombre/cédula/teléfono), puede ser null o vacío
     */
    public List<Paciente> listarPorEstado(Boolean activo, String termino) {
        List<Paciente> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM pacientes WHERE 1=1");
        if (activo != null) sql.append(" AND activo = ?");
        boolean hayTermino = termino != null && !termino.trim().isEmpty();
        if (hayTermino) sql.append(" AND (nombre LIKE ? OR cedula LIKE ? OR telefono LIKE ?)");
        sql.append(" ORDER BY nombre");

        Conexion cn = new Conexion();
        try (Connection con = cn.getConnetion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            if (activo != null) ps.setBoolean(idx++, activo);
            if (hayTermino) {
                String like = "%" + termino.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return lista;
    }

    /** Pacientes que tienen al menos una cita (pasada o futura) con el doctor indicado. */
    public List<Paciente> listarPorDoctor(int doctorId) {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT p.* FROM pacientes p "
                + "INNER JOIN citas c ON c.paciente_id = p.id "
                + "WHERE c.doctor_id = ? ORDER BY p.nombre";
        Conexion cn = new Conexion();
        try (Connection con = cn.getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return lista;
    }

    /** true si ya existe un paciente con esa cédula (excluyendo, si aplica, el propio id que se está editando). */
    public boolean existeCedula(String cedula, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM pacientes WHERE cedula = ? AND id <> ?";
        Conexion cn = new Conexion();
        try (Connection con = cn.getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cedula);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return false;
    }

    /** Exporta la lista dada a un archivo CSV (separado por comas, UTF-8). */
    public void exportarCSV(List<Paciente> lista, String rutaArchivo) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            bw.write("ID,Nombre,Cedula,FechaNacimiento,Telefono,Email,Direccion,Alergias,Activo");
            bw.newLine();
            for (Paciente p : lista) {
                bw.write(String.join(",",
                        String.valueOf(p.getId()),
                        csv(p.getNombre()),
                        csv(p.getCedula()),
                        p.getFechaNacimiento() != null ? p.getFechaNacimiento().toString() : "",
                        csv(p.getTelefono()),
                        csv(p.getEmail()),
                        csv(p.getDireccion()),
                        csv(p.getAlergias()),
                        p.isActivo() ? "Si" : "No"
                ));
                bw.newLine();
            }
        }
    }

    private String csv(String valor) {
        if (valor == null) return "";
        String limpio = valor.replace("\"", "\"\"");
        return "\"" + limpio + "\"";
    }

    private Paciente mapear(ResultSet rs) throws SQLException {
        Paciente p = new Paciente();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setCedula(rs.getString("cedula"));
        Date fn = rs.getDate("fecha_nacimiento");
        if (fn != null) p.setFechaNacimiento(fn.toLocalDate());
        p.setTelefono(rs.getString("telefono"));
        p.setEmail(rs.getString("email"));
        p.setDireccion(rs.getString("direccion"));
        p.setAlergias(rs.getString("alergias"));
        p.setFotoUrl(rs.getString("foto_url"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }
}
