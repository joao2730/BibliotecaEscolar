package view;

import controller.EstudianteController;
import model.Estudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class EstudiantesFrame extends JFrame {

    private final EstudianteController controller =
            new EstudianteController();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "RUT", "Nombre", "Curso", "Correo"}, 0
    ) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    public EstudiantesFrame() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel botones = new JPanel();
        JButton nuevo = new JButton("Nuevo");
        JButton editar = new JButton("Editar");
        JButton eliminar = new JButton("Eliminar");
        JButton actualizar = new JButton("Actualizar");

        botones.add(nuevo);
        botones.add(editar);
        botones.add(eliminar);
        botones.add(actualizar);
        panel.add(botones, BorderLayout.SOUTH);

        nuevo.addActionListener(e -> editarEstudiante(null));
        editar.addActionListener(e -> {
            Estudiante estudiante = seleccionado();
            if (estudiante != null) editarEstudiante(estudiante);
        });
        eliminar.addActionListener(e -> eliminarEstudiante());
        actualizar.addActionListener(e -> cargar());

        setContentPane(panel);
        cargar();
    }

    private void cargar() {
        try {
            modelo.setRowCount(0);

            for (Estudiante e : controller.listar()) {
                modelo.addRow(new Object[]{
                        e.getId(), e.getRut(), e.getNombre(),
                        e.getCurso(), e.getCorreo()
                });
            }
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private Estudiante seleccionado() {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un estudiante.");
            return null;
        }

        int id = (int) modelo.getValueAt(
                tabla.convertRowIndexToModel(fila), 0
        );

        try {
            for (Estudiante e : controller.listar()) {
                if (e.getId() == id) return e;
            }
        } catch (SQLException ex) {
            error(ex);
        }

        return null;
    }

    private void editarEstudiante(Estudiante estudiante) {
        JTextField rut = new JTextField(
                estudiante == null ? "" : estudiante.getRut()
        );
        JTextField nombre = new JTextField(
                estudiante == null ? "" : estudiante.getNombre()
        );
        JTextField curso = new JTextField(
                estudiante == null ? "" : estudiante.getCurso()
        );
        JTextField correo = new JTextField(
                estudiante == null ? "" : estudiante.getCorreo()
        );

        JPanel formulario = new JPanel(new GridLayout(0, 2, 5, 5));
        formulario.add(new JLabel("RUT:"));
        formulario.add(rut);
        formulario.add(new JLabel("Nombre:"));
        formulario.add(nombre);
        formulario.add(new JLabel("Curso:"));
        formulario.add(curso);
        formulario.add(new JLabel("Correo:"));
        formulario.add(correo);

        int opcion = JOptionPane.showConfirmDialog(
                this, formulario,
                estudiante == null ? "Nuevo estudiante" : "Editar estudiante",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (opcion != JOptionPane.OK_OPTION) return;

        Estudiante e = estudiante == null ? new Estudiante() : estudiante;
        e.setRut(rut.getText().trim());
        e.setNombre(nombre.getText().trim());
        e.setCurso(curso.getText().trim());
        e.setCorreo(correo.getText().trim().isEmpty()
                ? null : correo.getText().trim());

        try {
            controller.guardar(e);
            cargar();
        } catch (SQLException | IllegalArgumentException ex) {
            error(ex);
        }
    }

    private void eliminarEstudiante() {
        Estudiante e = seleccionado();
        if (e == null) return;

        int opcion = JOptionPane.showConfirmDialog(
                this, "¿Eliminar a " + e.getNombre() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION
        );

        if (opcion == JOptionPane.YES_OPTION) {
            try {
                controller.eliminar(e.getId());
                cargar();
            } catch (SQLException ex) {
                error(ex);
            }
        }
    }

    private void error(Exception ex) {
        JOptionPane.showMessageDialog(
                this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
        );
    }
}

