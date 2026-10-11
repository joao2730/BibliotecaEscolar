package view;

import controller.LibroController;
import dao.CategoriaDao;
import model.Categoria;
import model.Libro;
import model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class LibrosFrame extends JFrame {

    private final LibroController controller = new LibroController();
    private final CategoriaDao categoriaDAO = new CategoriaDao();
    private final Usuario usuario;
    private final JTextField buscar = new JTextField(18);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Título", "Autor", "ISBN", "Categoría", "Stock"}, 0
    ) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    public LibrosFrame(Usuario usuario) {
        this.usuario = usuario;

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel superior = new JPanel();
        superior.add(new JLabel("Buscar:"));
        superior.add(buscar);

        JButton botonBuscar = new JButton("Buscar");
        JButton actualizar = new JButton("Actualizar");
        superior.add(botonBuscar);
        superior.add(actualizar);
        panel.add(superior, BorderLayout.NORTH);

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel botones = new JPanel();

        if (esBibliotecario()) {
            JButton nuevo = new JButton("Nuevo libro");
            JButton editar = new JButton("Editar");
            JButton eliminar = new JButton("Eliminar");

            botones.add(nuevo);
            botones.add(editar);
            botones.add(eliminar);

            nuevo.addActionListener(e -> editarLibro(null));
            editar.addActionListener(e -> {
                Libro libro = seleccionado();
                if (libro != null) editarLibro(libro);
            });
            eliminar.addActionListener(e -> eliminarLibro());
        }

        panel.add(botones, BorderLayout.SOUTH);

        botonBuscar.addActionListener(e -> cargar());
        actualizar.addActionListener(e -> {
            buscar.setText("");
            cargar();
        });

        setContentPane(panel);
        cargar();
    }

    private boolean esBibliotecario() {
        return "ADMIN".equalsIgnoreCase(usuario.getRol())
                || "BIBLIOTECARIO".equalsIgnoreCase(usuario.getRol());
    }

    private void cargar() {
        try {
            modelo.setRowCount(0);

            List<Libro> libros = controller.buscar(buscar.getText());

            for (Libro l : libros) {
                modelo.addRow(new Object[]{
                        l.getId(), l.getTitulo(), l.getAutor(),
                        l.getIsbn(), l.getCategoria(), l.getStock()
                });
            }
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private Libro seleccionado() {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro.");
            return null;
        }

        int id = (int) modelo.getValueAt(
                tabla.convertRowIndexToModel(fila), 0
        );

        try {
            for (Libro libro : controller.listar()) {
                if (libro.getId() == id) return libro;
            }
        } catch (SQLException ex) {
            mostrarError(ex);
        }

        return null;
    }

    private void editarLibro(Libro libro) {
        try {
            List<Categoria> categorias = categoriaDAO.listar();

            if (categorias.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this, "Primero debes registrar una categoría en MySQL."
                );
                return;
            }

            JTextField titulo = new JTextField(
                    libro == null ? "" : libro.getTitulo()
            );
            JTextField autor = new JTextField(
                    libro == null ? "" : libro.getAutor()
            );
            JTextField isbn = new JTextField(
                    libro == null ? "" : libro.getIsbn()
            );

            JComboBox<Categoria> categoria =
                    new JComboBox<>(categorias.toArray(new Categoria[0]));

            JSpinner stock = new JSpinner(
                    new SpinnerNumberModel(
                            libro == null ? 0 : libro.getStock(),
                            0, 100000, 1
                    )
            );

            if (libro != null) {
                for (Categoria c : categorias) {
                    if (c.getId() == libro.getIdCategoria()) {
                        categoria.setSelectedItem(c);
                        break;
                    }
                }
            }

            JPanel formulario = new JPanel(new GridLayout(0, 2, 5, 5));
            formulario.add(new JLabel("Título:"));
            formulario.add(titulo);
            formulario.add(new JLabel("Autor:"));
            formulario.add(autor);
            formulario.add(new JLabel("ISBN:"));
            formulario.add(isbn);
            formulario.add(new JLabel("Categoría:"));
            formulario.add(categoria);
            formulario.add(new JLabel("Stock:"));
            formulario.add(stock);

            int opcion = JOptionPane.showConfirmDialog(
                    this, formulario,
                    libro == null ? "Nuevo libro" : "Editar libro",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (opcion != JOptionPane.OK_OPTION) return;

            Categoria seleccionada = (Categoria) categoria.getSelectedItem();
            Libro l = libro == null ? new Libro() : libro;

            l.setTitulo(titulo.getText().trim());
            l.setAutor(autor.getText().trim());
            l.setIsbn(isbn.getText().trim().isEmpty()
                    ? null : isbn.getText().trim());
            l.setIdCategoria(seleccionada.getId());
            l.setCategoria(seleccionada.getNombre());
            l.setStock((Integer) stock.getValue());

            controller.guardar(l);
            cargar();

        } catch (SQLException | IllegalArgumentException ex) {
            mostrarError(ex);
        }
    }

    private void eliminarLibro() {
        Libro libro = seleccionado();
        if (libro == null) return;

        int opcion = JOptionPane.showConfirmDialog(
                this, "¿Eliminar el libro " + libro.getTitulo() + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION
        );

        if (opcion == JOptionPane.YES_OPTION) {
            try {
                controller.eliminar(libro.getId());
                cargar();
            } catch (SQLException ex) {
                mostrarError(ex);
            }
        }
    }

    private void mostrarError(Exception ex) {
        JOptionPane.showMessageDialog(
                this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
        );
    }
}

