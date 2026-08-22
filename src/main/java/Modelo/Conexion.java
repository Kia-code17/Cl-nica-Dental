package Modelo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexion {
    private Connection con;

    public Connection getConnetion() {
        String host = "localhost";
        String port = "3306";
        String database = "clinica_dental";
        String user = "root";
        String password = "";

        // Intenta cargar configuración personalizada desde db.properties si existe
        Properties props = new Properties();
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream input = new FileInputStream(propFile)) {
                props.load(input);
                host = props.getProperty("db.host", host);
                port = props.getProperty("db.port", port);
                database = props.getProperty("db.name", database);
                user = props.getProperty("db.user", user);
                password = props.getProperty("db.password", password);
            } catch (Exception e) {
                System.err.println("Advertencia: No se pudo leer db.properties, usando valores por defecto: " + e.getMessage());
            }
        }

        try {
            String url = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", host, port, database);
            con = DriverManager.getConnection(url, user, password);
            return con;
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos MySQL (" + host + ":" + port + "/" + database + "): " + e.getMessage());
        }
        return null;
    }
}