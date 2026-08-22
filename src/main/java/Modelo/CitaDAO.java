package Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CitaDAO {

    private static final String SELECT_BASE =
        "SELECT c.id, c.paciente_id, p.nombre AS paciente_nombre, c.doctor_id, "
        + "d.nombre AS doctor_nombre, c.fecha, c.hora, c.estado, c.notas "
        + "FROM citas c "
        + "JOIN pacientes p ON p.id = c.paciente_id "
        + "JOIN doctores d ON d.id = c.doctor_id ";

    public int crear(Cita c) throws SQLException {
        String sql = "INSERT INTO citas (paciente_id, doctor_id, fecha, hora, estado, notas) VALUES (?,?,?,?,?,?)";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getPacienteId());
            ps.setInt(2, c.getDoctorId());
            ps.setDate(3, Date.valueOf(c.getFecha()));
            ps.setTime(4, Time.valueOf(c.getHora()));
            ps.setString(5, c.getEstado());
            ps.setString(6, c.getNotas());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void actualizarEstado(int id, String nuevoEstado) throws SQLException {
        String sql = "UPDATE citas SET estado = ? WHERE id = ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void actualizar(Cita c) throws SQLException {
        String sql = "UPDATE citas SET paciente_id=?, doctor_id=?, fecha=?, hora=?, estado=?, notas=? WHERE id=?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.getPacienteId());
            ps.setInt(2, c.getDoctorId());
            ps.setDate(3, Date.valueOf(c.getFecha()));
            ps.setTime(4, Time.valueOf(c.getHora()));
            ps.setString(5, c.getEstado());
            ps.setString(6, c.getNotas());
            ps.setInt(7, c.getId());
            ps.executeUpdate();
        }
    }

    public void cancelar(int id) throws SQLException {
        actualizarEstado(id, "Cancelada");
    }

    /** Valida que el doctor no tenga otra cita a la misma fecha/hora. */
    public boolean existeConflicto(int doctorId, java.time.LocalDate fecha, java.time.LocalTime hora, int excluirCitaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM citas WHERE doctor_id = ? AND fecha = ? AND hora = ? "
                + "AND estado NOT IN ('Cancelada') AND id <> ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setTime(3, Time.valueOf(hora));
            ps.setInt(4, excluirCitaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public List<Cita> listarPorFecha(java.time.LocalDate fecha) throws SQLException {
        List<Cita> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE c.fecha = ? ORDER BY c.hora";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Cita> listarTodas() throws SQLException {
        List<Cita> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY c.fecha DESC, c.hora";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Cita> listarPorPaciente(int pacienteId) throws SQLException {
        List<Cita> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE c.paciente_id = ? ORDER BY c.fecha DESC";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pacienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Historial de atenciones de un doctor: todas sus citas, más recientes primero. */
    public List<Cita> listarPorDoctor(int doctorId) throws SQLException {
        List<Cita> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE c.doctor_id = ? ORDER BY c.fecha DESC, c.hora DESC";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Citas de hoy que aún están en estado "Programada" (pendientes de
     * confirmar). Se usa para el panel de notificaciones.
     */
    public List<Cita> listarPendientesDeConfirmarHoy() throws SQLException {
        List<Cita> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE c.fecha = CURDATE() AND c.estado = 'Programada' ORDER BY c.hora";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /**
     * Citas de hoy cuya hora aún no ha pasado, entre ahora y las próximas
     * {@code horasAdelante} horas, que no estén canceladas ni completadas.
     * Se usa para recordatorios/notificaciones de citas próximas.
     */
    public List<Cita> listarProximas(int horasAdelante) throws SQLException {
        List<Cita> lista = new ArrayList<>();
        String sql = SELECT_BASE
                + "WHERE c.fecha = CURDATE() "
                + "AND c.hora BETWEEN CURTIME() AND ADDTIME(CURTIME(), SEC_TO_TIME(? * 3600)) "
                + "AND c.estado NOT IN ('Cancelada','Completada','Inasistencia') "
                + "ORDER BY c.hora";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, horasAdelante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Cita mapear(ResultSet rs) throws SQLException {
        Cita c = new Cita();
        c.setId(rs.getInt("id"));
        c.setPacienteId(rs.getInt("paciente_id"));
        c.setPacienteNombre(rs.getString("paciente_nombre"));
        c.setDoctorId(rs.getInt("doctor_id"));
        c.setDoctorNombre(rs.getString("doctor_nombre"));
        c.setFecha(rs.getDate("fecha").toLocalDate());
        c.setHora(rs.getTime("hora").toLocalTime());
        c.setEstado(rs.getString("estado"));
        c.setNotas(rs.getString("notas"));
        return c;
    }
}