package Modelo;

public class Usuario {
    private int id;
    private String usuario;
    private String passwordHash;
    private String nombreCompleto;
    private int rolId;
    private String rolNombre; // solo lectura, viene de JOIN
    private boolean activo;
    private String ultimoLogin; // solo lectura, viene de JOIN; null si nunca ha iniciado sesión

    public Usuario() {}

    public Usuario(int id, String usuario, String nombreCompleto, int rolId, String rolNombre, boolean activo) {
        this.id = id;
        this.usuario = usuario;
        this.nombreCompleto = nombreCompleto;
        this.rolId = rolId;
        this.rolNombre = rolNombre;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public int getRolId() { return rolId; }
    public void setRolId(int rolId) { this.rolId = rolId; }
    public String getRolNombre() { return rolNombre; }
    public void setRolNombre(String rolNombre) { this.rolNombre = rolNombre; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(String ultimoLogin) { this.ultimoLogin = ultimoLogin; }

    @Override
    public String toString() { return nombreCompleto + " (" + usuario + ")"; }
}