package view;

import controller.LibroController;
import controller.PrestamoController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ReportesFrame extends JFrame {

    private final LibroController libros = new LibroController();
    private final PrestamoController prestamos = new PrestamoController();

    private final DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    private final JComboBox<String> tipo = new JComboBox<>(
            new String[]{
                    "Libros más prestados",
                    "Préstamos activos y vencidos"
            }
    );

    public ReportesFrame() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel superior = new JPanel();
        superior.add(new JLabel("Tipo de reporte:"));
        superior.add(tipo);

        JButton generar = new JButton("Generar reporte");
        superior.add(generar);

        panel.add(superior, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        generar.addActionListener(e -> generarReporte());

        setContentPane(panel);
        generarReporte();
    }

    private void generarReporte() {
        try {
            modelo.setRowCount(0);
            modelo.setColumnCount(0);

            if (tipo.getSelectedIndex() == 0) {
                modelo.setColumnIdentifiers(
                        new Object[]{"Libro", "Cantidad de préstamos"}
                );

                List<String[]> filas = libros.masPrestados();

                for (String[] fila : filas) {
                    modelo.addRow(fila);
                }

            } else {
                modelo.setColumnIdentifiers(
                        new Object[]{
                                "Estudiante", "Libro", "Vencimiento", "Situación"
                        }
                );

                List<String[]> filas = prestamos.activosVencidos();

                for (String[] fila : filas) {
                    modelo.addRow(fila);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
