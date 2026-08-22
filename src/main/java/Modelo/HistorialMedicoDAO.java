package Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos para la tabla `historiales_medicos` (notas clínicas generales por paciente). */
public class HistorialMedicoDAO {

    public void crear(HistorialMedico h) throws SQLException {
        String sql = "INSERT INTO historiales_medicos (paciente_id, doctor_id, notas) VALUES (?,?,?)";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, h.getPacienteId());
            ps.setInt(2, h.getDoctorId());
            ps.setString(3, h.getNotas());
            ps.executeUpdate();
        }
    }

    /** Historial completo de un paciente, más reciente primero, con el nombre del doctor incluido. */
    public List<HistorialMedico> listarPorPaciente(int pacienteId) throws SQLException {
        List<HistorialMedico> lista = new ArrayList<>();
        String sql = "SELECT hm.*, d.nombre AS doctor_nombre FROM historiales_medicos hm "
                + "INNER JOIN doctores d ON d.id = hm.doctor_id "
                + "WHERE hm.paciente_id = ? ORDER BY hm.fecha DESC";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pacienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialMedico h = new HistorialMedico();
                    h.setId(rs.getInt("id"));
                    h.setPacienteId(rs.getInt("paciente_id"));
                    h.setDoctorId(rs.getInt("doctor_id"));
                    h.setDoctorNombre(rs.getString("doctor_nombre"));
                    h.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    h.setNotas(rs.getString("notas"));
                    lista.add(h);
                }
            }
        }
        return lista;
    }
}
