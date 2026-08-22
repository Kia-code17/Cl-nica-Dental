package Modelo;

import java.time.LocalDateTime;

public class OdontogramaItem {
    private int id;
    private int pacienteId;
    private int doctorId;
    private LocalDateTime fecha;
    private int dienteId;   // 1-32
    private String estado;  // Sano, Cariado, Obturado, Extraido, Corona, Endodoncia, Implante
    private String notas;

    public OdontogramaItem() {}

    public OdontogramaItem(int dienteId, String estado) {
        this.dienteId = dienteId;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPacienteId() { return pacienteId; }
    public void setPacienteId(int pacienteId) { this.pacienteId = pacienteId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public int getDienteId() { return dienteId; }
    public void setDienteId(int dienteId) { this.dienteId = dienteId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}
