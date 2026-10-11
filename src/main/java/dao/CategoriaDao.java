package dao;

import config.DatabaseConnection;
import model.Categoria;
import java.sql.*;
import java.util.*;

public class CategoriaDao {

    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categorias ORDER BY nombre";

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Categoria(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                ));
            }
        }
        return lista;
    }

    public void insertar(Categoria c) throws SQLException {
        String sql = "INSERT INTO categorias(nombre,descripcion) VALUES(?,?)";

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.executeUpdate();
        }
    }

    public void actualizar(Categoria c) throws SQLException {
        String sql = """
            UPDATE categorias SET nombre=?, descripcion=? WHERE id=?
            """;

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.setInt(3, c.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categorias WHERE id=?";

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
