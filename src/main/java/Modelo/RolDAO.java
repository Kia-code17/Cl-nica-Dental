package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de solo lectura para la tabla `roles`. Los roles se administran
 * directamente en la base de datos (son fijos: Admin, Doctor,
 * Recepcionista, Asistente), por eso no se exponen métodos de escritura.
 */
public class RolDAO {

    public List<Rol> listarTodos() throws SQLException {
        List<Rol> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion FROM roles ORDER BY id";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Rol(rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion")));
            }
        }
        return lista;
    }

    public Rol buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, descripcion FROM roles WHERE id = ?";
        try (Connection con = new Conexion().getConnetion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Rol(rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion"));
                }
            }
        }
        return null;
    }
}