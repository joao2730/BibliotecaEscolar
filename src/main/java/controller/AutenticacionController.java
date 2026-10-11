package controller;

import dao.UsuarioDao;
import model.Usuario;
import java.sql.SQLException;
import java.util.Optional;

public class AutenticacionController {

    private final UsuarioDao dao = new UsuarioDao();

    public Optional<Usuario> iniciarSesion(
            String correo, String contrasena) throws SQLException {

        if (correo == null || correo.isBlank()
                || contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes ingresar correo y contraseña."
            );
        }

        return dao.autenticar(correo.trim(), contrasena);
    }
}

