package dao;

import config.DatabaseConnection;
import model.Estudiante;
import java.sql.*;
import java.util.*;

public class EstudianteDao {

    public List<Estudiante> listar() throws SQLException {
        List<Estudiante> lista = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes ORDER BY nombre";

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<Estudiante> buscarPorCorreo(String correo)
            throws SQLException {

        String sql = """
        SELECT id, rut, nombre, curso, correo
        FROM estudiantes
        WHERE LOWER(TRIM(correo)) = LOWER(TRIM(?))
        """;

        try (Connection con =
                     DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }

        return Optional.empty();
    }

    public void insertar(Estudiante e) throws SQLException {
        String sql = """
            INSERT INTO estudiantes(rut,nombre,curso,correo)
            VALUES(?,?,?,?)
            """;

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getRut());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            ps.executeUpdate();
        }
    }

    public void actualizar(Estudiante e) throws SQLException {
        String sql = """
            UPDATE estudiantes
            SET rut=?, nombre=?, curso=?, correo=?
            WHERE id=?
            """;

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getRut());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            ps.setInt(5, e.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM estudiantes WHERE id=?";

        try (Connection con = DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Estudiante mapear(ResultSet rs) throws SQLException {
        return new Estudiante(
                rs.getInt("id"),
                rs.getString("rut"),
                rs.getString("nombre"),
                rs.getString("curso"),
                rs.getString("correo")
        );
    }
}

