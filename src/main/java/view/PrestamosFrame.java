package view;

import controller.EstudianteController;
import controller.LibroController;
import controller.PrestamoController;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PrestamosFrame extends JFrame {

    private final Usuario usuario;
    private final PrestamoController prestamos = new PrestamoController();
    private final EstudianteController estudiantes = new EstudianteController();
    private final LibroController libros = new LibroController();

    private Integer idEstudianteSesion;

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{
                    "ID", "Estudiante", "Libro", "Fecha préstamo",
                    "Vencimiento", "Devolución", "Devuelto"
            }, 0
    ) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    public PrestamosFrame(Usuario usuario) {
        this.usuario = usuario;

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel botones = new JPanel();
        JButton registrar = new JButton("Registrar préstamo");
        JButton devolver = new JButton("Registrar devolución");
        JButton actualizar = new JButton("Actualizar");

        botones.add(registrar);
        botones.add(devolver);
        botones.add(actualizar);
        panel.add(botones, BorderLayout.SOUTH);

        registrar.addActionListener(e -> registrarPrestamo());
        devolver.addActionListener(e -> devolverPrestamo());
        actualizar.addActionListener(e -> cargar());

        setContentPane(panel);
        resolverEstudiante();
        cargar();
    }

    private boolean esBibliotecario() {
        return "ADMIN".equalsIgnoreCase(usuario.getRol())
                || "BIBLIOTECARIO".equalsIgnoreCase(usuario.getRol());
    }

    private void resolverEstudiante() {
        if (esBibliotecario()) {
            return;
        }

        try {
            // Primero buscamos al estudiante por el correo de la sesión.
            Optional<Estudiante> encontrado = Optional.empty();

            if (usuario.getCorreo() != null
                    && !usuario.getCorreo().isBlank()) {
                encontrado = estudiantes.buscarPorCorreo(
                        usuario.getCorreo()
                );
            }

            // Si no se encontró por correo, intentamos por RUT.
            if (encontrado.isEmpty()
                    && usuario.getRut() != null
                    && !usuario.getRut().isBlank()) {
                encontrado = estudiantes.buscarPorCorreo(
                        usuario.getRut()
                );
            }

            if (encontrado.isPresent()) {
                idEstudianteSesion = encontrado.get().getId();
            } else {
                idEstudianteSesion = null;

                JOptionPane.showMessageDialog(
                        this,
                        "No se encontró un estudiante asociado a tu cuenta. "
                                + "Verifica que tu correo o RUT esté registrado "
                                + "en la tabla estudiantes."
                );
            }

        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void cargar() {
        try {
            modelo.setRowCount(0);

            List<Prestamo> lista = prestamos.listar(
                    esBibliotecario() ? null : idEstudianteSesion
            );

            for (Prestamo p : lista) {
                modelo.addRow(new Object[]{
                        p.getId(),
                        p.getNombreEstudiante(),
                        p.getTituloLibro(),
                        p.getFechaPrestamo(),
                        p.getFechaVencimiento(),
                        p.getFechaDevolucion() == null
                                ? "-" : p.getFechaDevolucion(),
                        p.getEstado()
                });
            }
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void registrarPrestamo() {
        if (!esBibliotecario() && idEstudianteSesion == null) {
            JOptionPane.showMessageDialog(
                    this, "Tu cuenta no está asociada a un estudiante."
            );
            return;
        }

        try {
            List<Libro> disponibles = libros.listar().stream()
                    .filter(l -> l.getStock() > 0)
                    .toList();

            if (disponibles.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this, "No hay libros disponibles."
                );
                return;
            }

            JComboBox<Libro> comboLibros =
                    new JComboBox<>(disponibles.toArray(new Libro[0]));

            JComboBox<Estudiante> comboEstudiantes = null;

            JPanel formulario = new JPanel(new GridLayout(0, 2, 5, 5));

            if (esBibliotecario()) {
                List<Estudiante> listaEstudiantes = estudiantes.listar();

                if (listaEstudiantes.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            this, "Primero debes registrar estudiantes."
                    );
                    return;
                }

                comboEstudiantes = new JComboBox<>(
                        listaEstudiantes.toArray(new Estudiante[0])
                );

                formulario.add(new JLabel("Estudiante:"));
                formulario.add(comboEstudiantes);
            }

            formulario.add(new JLabel("Libro:"));
            formulario.add(comboLibros);

            JSpinner dias = new JSpinner(
                    new SpinnerNumberModel(7, 1, 60, 1)
            );

            formulario.add(new JLabel("Días de préstamo:"));
            formulario.add(dias);

            int opcion = JOptionPane.showConfirmDialog(
                    this, formulario, "Nuevo préstamo",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (opcion != JOptionPane.OK_OPTION) return;

            Libro libro = (Libro) comboLibros.getSelectedItem();

            int idEstudiante;

            if (esBibliotecario()) {
                Estudiante seleccionado =
                        (Estudiante) comboEstudiantes.getSelectedItem();
                idEstudiante = seleccionado.getId();
            } else {
                idEstudiante = idEstudianteSesion;
            }

            int plazo = (Integer) dias.getValue();

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            final int estudianteFinal = idEstudiante;
            final int libroFinal = libro.getId();

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    prestamos.registrar(
                            estudianteFinal, libroFinal, plazo
                    );
                    return null;
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());

                    try {
                        get();
                        cargar();
                        JOptionPane.showMessageDialog(
                                PrestamosFrame.this, "Préstamo registrado."
                        );
                    } catch (Exception ex) {
                        error(ex.getCause() == null ? ex : ex.getCause());
                    }
                }
            }.execute();

        } catch (SQLException ex) {
            error(ex);
        }
    }


    private void devolverPrestamo() {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(
                    this, "Selecciona un préstamo de la tabla."
            );
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);

        int idPrestamo = ((Number) modelo.getValueAt(
                filaModelo, 0
        )).intValue();

        String estado = String.valueOf(
                modelo.getValueAt(filaModelo, 6)
        ).trim();

        // Solo impedir la devolución si ya está devuelto.
        if ("DEVUELTO".equalsIgnoreCase(estado)) {
            JOptionPane.showMessageDialog(
                    this, "Este préstamo ya fue devuelto."
            );
            return;
        }

        Integer estudiantePermitido =
                esBibliotecario() ? null : idEstudianteSesion;

        if (!esBibliotecario() && estudiantePermitido == null) {
            JOptionPane.showMessageDialog(
                    this, "Tu cuenta no está asociada a un estudiante."
            );
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                System.out.println(
                        "Intentando devolver préstamo ID: " + idPrestamo
                );

                prestamos.devolver(idPrestamo, estudiantePermitido);
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());

                try {
                    get();
                    cargar();

                    JOptionPane.showMessageDialog(
                            PrestamosFrame.this,
                            "Devolución registrada correctamente."
                    );

                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null
                            ? ex.getCause() : ex;

                    error(causa);
                }
            }
        }.execute();
    }


    private void error(Throwable ex) {
        JOptionPane.showMessageDialog(
                this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
        );
    }
}

