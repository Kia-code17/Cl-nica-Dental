package Modelo;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla horarios_doctores: define el calendario de
 * disponibilidad semanal de cada doctor (día + rango de horas).
 * Se usa para: mostrar el calendario de disponibilidad en DoctoresPanel y
 * para validar en CitasPanel que una cita se agende dentro del horario
 * del doctor.
 */
public class HorarioDoctorDAO {

    /** Traduce LocalDate.getDayOfWeek() al valor en español usado por el ENUM de la BD. */
    public static String diaEnEspanol(DayOfWeek dow) {
        switch (dow) {
            case MONDAY: return "Lunes";
            case TUESDAY: return "Martes";
            case WEDNESDAY: return "Miercoles";
            case THURSDAY: return "Jueves";
            case FRIDAY: return "Viernes";
            case SATURDAY: return "Sabado";
            default: return "Domingo";
        }
    }

    public static final String[] DIAS = {
        "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"
    };

    public int crear(HorarioDoctor h) throws SQLException {
        String sql = "INSERT INTO horarios_doctores (doctor_id, dia, hora_inicio, hora_fin) VALUES (?,?,?,?)";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, h.getDoctorId());
            ps.setString(2, h.getDia());
            ps.setTime(3, Time.valueOf(h.getHoraInicio()));
            ps.setTime(4, Time.valueOf(h.getHoraFin()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM horarios_doctores WHERE id = ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<HorarioDoctor> listarPorDoctor(int doctorId) throws SQLException {
        List<HorarioDoctor> lista = new ArrayList<>();
        String sql = "SELECT * FROM horarios_doctores WHERE doctor_id = ? "
                + "ORDER BY FIELD(dia,'Lunes','Martes','Miercoles','Jueves','Viernes','Sabado','Domingo'), hora_inicio";
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
     * Indica si el doctor tiene, según su horario semanal configurado,
     * disponibilidad para atender en la fecha/hora indicadas.
     * Si el doctor no tiene ningún horario configurado, se asume disponible
     * (para no bloquear el uso del sistema mientras no se haya cargado su horario).
     */
    public boolean estaDisponible(int doctorId, LocalDate fecha, LocalTime hora) throws SQLException {
        List<HorarioDoctor> horarios = listarPorDoctor(doctorId);
        if (horarios.isEmpty()) return true;
        String dia = diaEnEspanol(fecha.getDayOfWeek());
        for (HorarioDoctor h : horarios) {
            if (h.getDia().equals(dia)
                    && !hora.isBefore(h.getHoraInicio())
                    && hora.isBefore(h.getHoraFin())) {
                return true;
            }
        }
        return false;
    }

    private HorarioDoctor mapear(ResultSet rs) throws SQLException {
        HorarioDoctor h = new HorarioDoctor();
        h.setId(rs.getInt("id"));
        h.setDoctorId(rs.getInt("doctor_id"));
        h.setDia(rs.getString("dia"));
        h.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
        h.setHoraFin(rs.getTime("hora_fin").toLocalTime());
        return h;
    }
}
