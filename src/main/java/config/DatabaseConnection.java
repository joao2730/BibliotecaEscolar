package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static DatabaseConnection instancia;

    private final String url =
            "jdbc:mysql://localhost:3306/biblioteca";

    private final String usuario = "root";
    private final String contrasena = "root";

    private DatabaseConnection() {
    }

    public static synchronized DatabaseConnection getInstancia() {
        if (instancia == null) {
            instancia = new DatabaseConnection();
        }
        return instancia;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                url, usuario, contrasena
        );
    }
}

