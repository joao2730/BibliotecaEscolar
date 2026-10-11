package controller;

import dao.EstudianteDao;
import model.Estudiante;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EstudianteController {

    private final EstudianteDao dao = new EstudianteDao();

    public List<Estudiante> listar() throws SQLException {
        return dao.listar();
    }

    public Optional<Estudiante> buscarPorCorreo(String correo)
            throws SQLException {
        return dao.buscarPorCorreo(correo);
    }

    public void guardar(Estudiante e) throws SQLException {
        if (e.getCorreo() == null || e.getCorreo().isBlank()
                || e.getNombre() == null || e.getNombre().isBlank()
                || e.getCurso() == null || e.getCurso().isBlank()) {
            throw new IllegalArgumentException(
                    "Correo, nombre y curso son obligatorios."
            );
        }

        if (e.getId() == 0) {
            dao.insertar(e);
        } else {
            dao.actualizar(e);
        }
    }

    public void eliminar(int id) throws SQLException {
        dao.eliminar(id);
    }
}

