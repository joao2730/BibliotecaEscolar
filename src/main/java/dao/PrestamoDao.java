package dao;

import config.DatabaseConnection;
import model.Prestamo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDao {

    private static final Object BLOQUEO = new Object();

    // =========================================
    // REGISTRAR PRÉSTAMO
    // =========================================
    public void registrar(int idEstudiante, int idLibro, int dias)
            throws SQLException {

        if (dias < 1) {
            throw new IllegalArgumentException(
                    "Los días deben ser positivos."
            );
        }

        synchronized (BLOQUEO) {

            String stockSql = """
                UPDATE libros
                SET stock = stock - 1
                WHERE id = ? AND stock > 0
                """;

            String prestamoSql = """
                INSERT INTO prestamos
                (id_estudiante, id_libro, fecha_prestamo,
                 fecha_devolucion, devuelto)
                VALUES (?, ?, ?, ?, FALSE)
                """;

            try (Connection con =
                         DatabaseConnection.getInstancia().getConnection()) {

                con.setAutoCommit(false);

                try {
                    int filas;

                    // Descontar una unidad del stock.
                    try (PreparedStatement ps =
                                 con.prepareStatement(stockSql)) {

                        ps.setInt(1, idLibro);
                        filas = ps.executeUpdate();
                    }

                    if (filas != 1) {
                        throw new SQLException(
                                "El libro no existe o no tiene stock."
                        );
                    }

                    LocalDate hoy = LocalDate.now();
                    LocalDate vencimiento = hoy.plusDays(dias);

                    // Registrar el préstamo pendiente.
                    try (PreparedStatement ps =
                                 con.prepareStatement(prestamoSql)) {

                        ps.setInt(1, idEstudiante);
                        ps.setInt(2, idLibro);
                        ps.setDate(3, Date.valueOf(hoy));
                        ps.setDate(4, Date.valueOf(vencimiento));

                        ps.executeUpdate();
                    }

                    con.commit();

                } catch (Exception ex) {
                    con.rollback();

                    if (ex instanceof SQLException sqlEx) {
                        throw sqlEx;
                    }

                    throw new SQLException(
                            "Error al registrar el préstamo.", ex
                    );

                } finally {
                    con.setAutoCommit(true);
                }
            }
        }
    }

    // =========================================
    // DEVOLVER LIBRO
    // =========================================
    public void devolver(int idPrestamo, Integer estudiantePermitido)
            throws SQLException {

        synchronized (BLOQUEO) {

            try (Connection con =
                         DatabaseConnection.getInstancia().getConnection()) {

                con.setAutoCommit(false);

                try {
                    int idLibro;
                    int idEstudiante;
                    boolean yaDevuelto;

                    String consulta = """
                        SELECT id_libro, id_estudiante, devuelto
                        FROM prestamos
                        WHERE id = ?
                        FOR UPDATE
                        """;

                    try (PreparedStatement ps =
                                 con.prepareStatement(consulta)) {

                        ps.setInt(1, idPrestamo);

                        try (ResultSet rs = ps.executeQuery()) {

                            if (!rs.next()) {
                                throw new SQLException(
                                        "Préstamo inexistente."
                                );
                            }

                            idLibro = rs.getInt("id_libro");
                            idEstudiante = rs.getInt("id_estudiante");
                            yaDevuelto = rs.getBoolean("devuelto");
                        }
                    }

                    // Verificar que el estudiante sea propietario
                    // del préstamo cuando corresponda.
                    if (estudiantePermitido != null
                            && idEstudiante != estudiantePermitido) {

                        throw new SQLException(
                                "Solo puedes devolver tus propios préstamos."
                        );
                    }

                    if (yaDevuelto) {
                        throw new SQLException(
                                "El préstamo ya fue devuelto."
                        );
                    }

                    // Marcar como devuelto sin alterar
                    // la fecha de vencimiento original.
                    String actualizar = """
                        UPDATE prestamos
                        SET devuelto = TRUE
                        WHERE id = ? AND devuelto = FALSE
                        """;

                    try (PreparedStatement ps =
                                 con.prepareStatement(actualizar)) {

                        ps.setInt(1, idPrestamo);

                        if (ps.executeUpdate() != 1) {
                            throw new SQLException(
                                    "No se pudo devolver el libro."
                            );
                        }
                    }

                    // Devolver una unidad al stock.
                    try (PreparedStatement ps =
                                 con.prepareStatement(
                                         "UPDATE libros "
                                                 + "SET stock = stock + 1 WHERE id = ?"
                                 )) {

                        ps.setInt(1, idLibro);

                        if (ps.executeUpdate() != 1) {
                            throw new SQLException(
                                    "No se pudo actualizar el stock del libro."
                            );
                        }
                    }

                    con.commit();

                } catch (Exception ex) {
                    con.rollback();

                    if (ex instanceof SQLException sqlEx) {
                        throw sqlEx;
                    }

                    throw new SQLException(
                            "Error al registrar la devolución.", ex
                    );

                } finally {
                    con.setAutoCommit(true);
                }
            }
        }
    }

    // =========================================
    // LISTAR PRÉSTAMOS
    // =========================================
    public List<Prestamo> listar(Integer idEstudiante)
            throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT p.*, e.nombre AS estudiante, l.titulo AS libro
            FROM prestamos p
            JOIN estudiantes e ON e.id = p.id_estudiante
            JOIN libros l ON l.id = p.id_libro
            """);

        if (idEstudiante != null) {
            sql.append(" WHERE p.id_estudiante = ?");
        }

        sql.append(" ORDER BY p.fecha_prestamo DESC");

        List<Prestamo> lista = new ArrayList<>();

        try (Connection con =
                     DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql.toString())) {

            if (idEstudiante != null) {
                ps.setInt(1, idEstudiante);
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Prestamo p = new Prestamo();

                    p.setId(rs.getInt("id"));
                    p.setIdEstudiante(rs.getInt("id_estudiante"));
                    p.setNombreEstudiante(rs.getString("estudiante"));
                    p.setIdLibro(rs.getInt("id_libro"));
                    p.setTituloLibro(rs.getString("libro"));

                    Date fechaPrestamo = rs.getDate("fecha_prestamo");
                    p.setFechaPrestamo(
                            fechaPrestamo == null
                                    ? null
                                    : fechaPrestamo.toLocalDate()
                    );

                    Date fechaDevolucion =
                            rs.getDate("fecha_devolucion");

                    p.setFechaDevolucion(
                            fechaDevolucion == null
                                    ? null
                                    : fechaDevolucion.toLocalDate()
                    );

                    p.setEstado(
                            rs.getBoolean("devuelto")
                                    ? "DEVUELTO"
                                    : "PENDIENTE"
                    );

                    lista.add(p);
                }
            }
        }

        return lista;
    }

    // =========================================
    // LISTAR PRÉSTAMOS VENCIDOS O VIGENTES
    // =========================================
    public List<String[]> activosVencidos()
            throws SQLException {

        String sql = """
            SELECT e.nombre AS estudiante,
                   l.titulo,
                   p.fecha_devolucion,
                   CASE
                       WHEN p.fecha_devolucion < CURDATE()
                       THEN 'VENCIDO'
                       ELSE 'VIGENTE'
                   END AS situacion
            FROM prestamos p
            JOIN estudiantes e ON e.id = p.id_estudiante
            JOIN libros l ON l.id = p.id_libro
            WHERE p.devuelto = FALSE
            ORDER BY p.fecha_devolucion
            """;

        List<String[]> lista = new ArrayList<>();

        try (Connection con =
                     DatabaseConnection.getInstancia().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date fecha = rs.getDate("fecha_devolucion");

                lista.add(new String[]{
                        rs.getString("estudiante"),
                        rs.getString("titulo"),
                        fecha == null ? "" : fecha.toString(),
                        rs.getString("situacion")
                });
            }
        }

        return lista;
    }
}

