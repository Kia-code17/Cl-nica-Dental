package Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OdontogramaDAO {

    /** Guarda el estado actual de un diente para un paciente (crea nuevo registro histórico). */
    public void guardarEstadoDiente(int pacienteId, int doctorId, int dienteId, String estado, String notas) throws SQLException {
        String sql = "INSERT INTO odontogramas (paciente_id, doctor_id, diente_id, estado, notas) VALUES (?,?,?,?,?)";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pacienteId);
            ps.setInt(2, doctorId);
            ps.setInt(3, dienteId);
            ps.setString(4, estado);
            ps.setString(5, notas);
            ps.executeUpdate();
        }
    }

    /** Devuelve el estado MÁS RECIENTE de cada diente (1-32) para un paciente. */
    public List<OdontogramaItem> obtenerEstadoActual(int pacienteId) throws SQLException {
        List<OdontogramaItem> lista = new ArrayList<>();
        String sql =
            "SELECT o1.* FROM odontogramas o1 "
            + "INNER JOIN ( "
            + "  SELECT diente_id, MAX(fecha) AS max_fecha FROM odontogramas "
            + "  WHERE paciente_id = ? GROUP BY diente_id "
            + ") o2 ON o1.diente_id = o2.diente_id AND o1.fecha = o2.max_fecha "
            + "WHERE o1.paciente_id = ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pacienteId);
            ps.setInt(2, pacienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<OdontogramaItem> historialDiente(int pacienteId, int dienteId) throws SQLException {
        List<OdontogramaItem> lista = new ArrayList<>();
        String sql = "SELECT * FROM odontogramas WHERE paciente_id = ? AND diente_id = ? ORDER BY fecha DESC";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pacienteId);
            ps.setInt(2, dienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private OdontogramaItem mapear(ResultSet rs) throws SQLException {
        OdontogramaItem o = new OdontogramaItem();
        o.setId(rs.getInt("id"));
        o.setPacienteId(rs.getInt("paciente_id"));
        o.setDoctorId(rs.getInt("doctor_id"));
        o.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        o.setDienteId(rs.getInt("diente_id"));
        o.setEstado(rs.getString("estado"));
        o.setNotas(rs.getString("notas"));
        return o;
    }
}