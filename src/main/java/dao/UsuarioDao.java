package dao;

import config.DatabaseConnection;
import model.Usuario;
import java.sql.*;
import java.util.Optional;

public class UsuarioDao {

    public Optional<Usuario> autenticar(
            String correo, String contrasena) throws SQLException {

        String sql = """
            SELECT id, nombre, rut, correo, contraseña, rol
            FROM usuarios
            WHERE correo = ? AND `contraseña` = ?
            """;

        try (Connection con =
                     DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo);
            ps.setString(2, contrasena);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("correo"),
                            rs.getString("contraseña"),
                            rs.getString("rol"),
                            rs.getString("rut")
                    );
                    return Optional.of(u);
                }
            }
        }
        return Optional.empty();
    }
}

