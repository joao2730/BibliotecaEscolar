package dao;

import config.DatabaseConnection;
import model.Libro;
import java.sql.*;
import java.util.*;

public class LibroDao {

    public List<Libro> listar() throws SQLException {
        String sql = """
            SELECT l.*, c.nombre AS categoria
            FROM libros l
            JOIN categorias c ON c.id = l.id_categoria
            ORDER BY l.titulo
            """;

        List<Libro> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Libro> buscar(String texto) throws SQLException {
        String sql = """
            SELECT l.*, c.nombre AS categoria
            FROM libros l JOIN categorias c ON c.id=l.id_categoria
            WHERE l.titulo LIKE ? OR l.autor LIKE ? OR l.isbn LIKE ?
            ORDER BY l.titulo
            """;

        List<Libro> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + texto + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void insertar(Libro l) throws SQLException {
        String sql = """
            INSERT INTO libros(titulo,autor,isbn,id_categoria,stock)
            VALUES(?,?,?,?,?)
            """;

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getIsbn());
            ps.setInt(4, l.getIdCategoria());
            ps.setInt(5, l.getStock());
            ps.executeUpdate();
        }
    }

    public void actualizar(Libro l) throws SQLException {
        String sql = """
            UPDATE libros
            SET titulo=?, autor=?, isbn=?, id_categoria=?, stock=?
            WHERE id=?
            """;

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getIsbn());
            ps.setInt(4, l.getIdCategoria());
            ps.setInt(5, l.getStock());
            ps.setInt(6, l.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM libros WHERE id=?";

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<String[]> masPrestados() throws SQLException {
        String sql = """
            SELECT l.titulo, COUNT(p.id) AS cantidad
            FROM libros l LEFT JOIN prestamos p ON p.id_libro=l.id
            GROUP BY l.id, l.titulo
            ORDER BY cantidad DESC
            """;

        List<String[]> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        rs.getString("titulo"),
                        rs.getString("cantidad")
                });
            }
        }
        return lista;
    }

    private Libro mapear(ResultSet rs) throws SQLException {
        return new Libro(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getString("isbn"),
                rs.getInt("id_categoria"),
                rs.getString("categoria"),
                rs.getInt("stock")
        );
    }
}

