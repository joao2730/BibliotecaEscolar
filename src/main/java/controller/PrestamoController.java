package controller;

import dao.PrestamoDao;
import model.Prestamo;
import java.sql.SQLException;
import java.util.List;

public class PrestamoController {

    private final PrestamoDao dao = new PrestamoDao();

    public void registrar(int idEstudiante, int idLibro, int dias)
            throws SQLException {
        if (idEstudiante <= 0 || idLibro <= 0 || dias < 1) {
            throw new IllegalArgumentException("Datos del préstamo inválidos.");
        }
        dao.registrar(idEstudiante, idLibro, dias);
    }

    public void devolver(int idPrestamo, Integer estudiantePermitido)
            throws SQLException {
        dao.devolver(idPrestamo, estudiantePermitido);
    }

    public List<Prestamo> listar(Integer idEstudiante) throws SQLException {
        return dao.listar(idEstudiante);
    }

    public List<String[]> activosVencidos() throws SQLException {
        return dao.activosVencidos();
    }
}

