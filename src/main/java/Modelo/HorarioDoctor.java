package Modelo;

import java.time.LocalTime;

/**
 * Representa un bloque de disponibilidad semanal de un doctor
 * (tabla horarios_doctores). Ej: Dr. Peña, Lunes, 08:00-16:00.
 */
public class HorarioDoctor {
    private int id;
    private int doctorId;
    private String dia; // Lunes, Martes, Miercoles, Jueves, Viernes, Sabado, Domingo
    private LocalTime horaInicio;
    private LocalTime horaFin;

    public HorarioDoctor() {}

    public HorarioDoctor(int doctorId, String dia, LocalTime horaInicio, LocalTime horaFin) {
        this.doctorId = doctorId;
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getDia() { return dia; }
    public void setDia(String dia) { this.dia = dia; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}
