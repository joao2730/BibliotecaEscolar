package controller;

import dao.LibroDao;
import model.Libro;
import java.sql.SQLException;
import java.util.List;

public class LibroController {

    private final LibroDao dao = new LibroDao();

    public List<Libro> listar() throws SQLException {
        return dao.listar();
    }

    public List<Libro> buscar(String texto) throws SQLException {
        if (texto == null || texto.isBlank()) {
            return dao.listar();
        }
        return dao.buscar(texto.trim());
    }

    public void guardar(Libro libro) throws SQLException {
        if (libro.getTitulo() == null || libro.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio.");
        }
        if (libro.getAutor() == null || libro.getAutor().isBlank()) {
            throw new IllegalArgumentException("El autor es obligatorio.");
        }
        if (libro.getIdCategoria() <= 0 || libro.getStock() < 0) {
            throw new IllegalArgumentException("Revisa la categoría y el stock.");
        }

        if (libro.getId() == 0) {
            dao.insertar(libro);
        } else {
            dao.actualizar(libro);
        }
    }

    public void eliminar(int id) throws SQLException {
        dao.eliminar(id);
    }

    public List<String[]> masPrestados() throws SQLException {
        return dao.masPrestados();
    }
}
