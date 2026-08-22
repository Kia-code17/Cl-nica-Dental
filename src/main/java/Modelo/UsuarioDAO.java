package Modelo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    /** Autentica contra la BD aceptando tanto contraseñas en texto plano como con hash SHA-256. */
    public Usuario autenticar(String usuario, String passwordPlano) throws SQLException {
        String hash = sha256(passwordPlano);
        String sql = "SELECT u.id, u.usuario, u.nombre_completo, u.rol_id, r.nombre AS rol_nombre, u.activo "
                + "FROM usuarios u JOIN roles r ON r.id = u.rol_id "
                + "WHERE u.usuario = ? AND (u.password_hash = ? OR u.password_hash = ?) AND u.activo = TRUE";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, hash);
            ps.setString(3, passwordPlano);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getInt("id"), rs.getString("usuario"), rs.getString("nombre_completo"),
                        rs.getInt("rol_id"), rs.getString("rol_nombre"), rs.getBoolean("activo")
                    );
                }
            }
        }
        return null;
    }

    public void registrarUltimoLogin(int usuarioId) throws SQLException {
        String sql = "UPDATE usuarios SET ultimo_login = NOW() WHERE id = ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------
    //  CRUD para el módulo "Usuarios y roles"
    // ------------------------------------------------------------------

    /** Lista todos los usuarios con su rol, ordenados por nombre. */
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id, u.usuario, u.nombre_completo, u.rol_id, r.nombre AS rol_nombre, "
                + "u.activo, u.ultimo_login "
                + "FROM usuarios u JOIN roles r ON r.id = u.rol_id "
                + "ORDER BY u.nombre_completo";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario(
                    rs.getInt("id"), rs.getString("usuario"), rs.getString("nombre_completo"),
                    rs.getInt("rol_id"), rs.getString("rol_nombre"), rs.getBoolean("activo")
                );
                Timestamp ultimo = rs.getTimestamp("ultimo_login");
                u.setUltimoLogin(ultimo != null ? ultimo.toString() : null);
                lista.add(u);
            }
        }
        return lista;
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT u.id, u.usuario, u.nombre_completo, u.rol_id, r.nombre AS rol_nombre, "
                + "u.activo, u.ultimo_login "
                + "FROM usuarios u JOIN roles r ON r.id = u.rol_id WHERE u.id = ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(
                        rs.getInt("id"), rs.getString("usuario"), rs.getString("nombre_completo"),
                        rs.getInt("rol_id"), rs.getString("rol_nombre"), rs.getBoolean("activo")
                    );
                    Timestamp ultimo = rs.getTimestamp("ultimo_login");
                    u.setUltimoLogin(ultimo != null ? ultimo.toString() : null);
                    return u;
                }
            }
        }
        return null;
    }

    /**
     * Verifica si un nombre de usuario ya está en uso.
     * @param excluirId si no es null, ignora esa fila (útil al editar el propio usuario).
     */
    public boolean existeUsuario(String usuario, Integer excluirId) throws SQLException {
        String sql = excluirId == null
            ? "SELECT COUNT(*) FROM usuarios WHERE usuario = ?"
            : "SELECT COUNT(*) FROM usuarios WHERE usuario = ? AND id <> ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            if (excluirId != null) ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Crea un usuario nuevo con la contraseña en texto plano (se hashea aquí antes de guardar). */
    public void crear(Usuario u, String passwordPlano) throws SQLException {
        String sql = "INSERT INTO usuarios (usuario, password_hash, nombre_completo, rol_id, activo) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getUsuario());
            ps.setString(2, sha256(passwordPlano));
            ps.setString(3, u.getNombreCompleto());
            ps.setInt(4, u.getRolId());
            ps.setBoolean(5, u.isActivo());
            ps.executeUpdate();
        }
    }

    /** Actualiza nombre, usuario, rol y estado. No toca la contraseña (usar actualizarPassword para eso). */
    public void actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuarios SET usuario = ?, nombre_completo = ?, rol_id = ?, activo = ? WHERE id = ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getUsuario());
            ps.setString(2, u.getNombreCompleto());
            ps.setInt(3, u.getRolId());
            ps.setBoolean(4, u.isActivo());
            ps.setInt(5, u.getId());
            ps.executeUpdate();
        }
    }

    public void actualizarPassword(int usuarioId, String passwordPlano) throws SQLException {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE id = ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sha256(passwordPlano));
            ps.setInt(2, usuarioId);
            ps.executeUpdate();
        }
    }

    public void cambiarEstado(int usuarioId, boolean activo) throws SQLException {
        String sql = "UPDATE usuarios SET activo = ? WHERE id = ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, usuarioId);
            ps.executeUpdate();
        }
    }

    private String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(texto.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}