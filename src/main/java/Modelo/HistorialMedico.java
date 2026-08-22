package Modelo;

import java.time.LocalDateTime;

/** Modelo de datos para la tabla `historiales_medicos` (historial médico general del paciente). */
public class HistorialMedico {
    private int id;
    private int pacienteId;
    private int doctorId;
    private String doctorNombre; // solo para mostrar en tabla, se llena al hacer JOIN
    private LocalDateTime fecha;
    private String notas;

    public HistorialMedico() {}

    public HistorialMedico(int pacienteId, int doctorId, String notas) {
        this.pacienteId = pacienteId;
        this.doctorId = doctorId;
        this.notas = notas;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPacienteId() { return pacienteId; }
    public void setPacienteId(int pacienteId) { this.pacienteId = pacienteId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getDoctorNombre() { return doctorNombre; }
    public void setDoctorNombre(String doctorNombre) { this.doctorNombre = doctorNombre; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}
