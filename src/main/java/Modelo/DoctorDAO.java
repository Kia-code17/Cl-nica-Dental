package Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    public int crear(Doctor d) throws SQLException {
        String sql = "INSERT INTO doctores (nombre, cedula, especialidad, telefono, email, activo) "
                + "VALUES (?,?,?,?,?,?)";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getNombre());
            ps.setString(2, d.getCedula());
            ps.setString(3, d.getEspecialidad());
            ps.setString(4, d.getTelefono());
            ps.setString(5, d.getEmail());
            ps.setBoolean(6, d.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void actualizar(Doctor d) throws SQLException {
        String sql = "UPDATE doctores SET nombre=?, cedula=?, especialidad=?, telefono=?, email=?, activo=? WHERE id=?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, d.getNombre());
            ps.setString(2, d.getCedula());
            ps.setString(3, d.getEspecialidad());
            ps.setString(4, d.getTelefono());
            ps.setString(5, d.getEmail());
            ps.setBoolean(6, d.isActivo());
            ps.setInt(7, d.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "UPDATE doctores SET activo = FALSE WHERE id = ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Doctor> listarTodos() throws SQLException {
        List<Doctor> lista = new ArrayList<>();
        String sql = "SELECT * FROM doctores ORDER BY nombre";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Doctor> listarActivos() throws SQLException {
        List<Doctor> lista = new ArrayList<>();
        String sql = "SELECT * FROM doctores WHERE activo = TRUE ORDER BY nombre";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private Doctor mapear(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setId(rs.getInt("id"));
        d.setNombre(rs.getString("nombre"));
        d.setCedula(rs.getString("cedula"));
        d.setEspecialidad(rs.getString("especialidad"));
        d.setTelefono(rs.getString("telefono"));
        d.setEmail(rs.getString("email"));
        d.setActivo(rs.getBoolean("activo"));
        return d;
    }
}