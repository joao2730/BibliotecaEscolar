package view;

import model.Usuario;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame(Usuario usuario) {
        setTitle("Biblioteca Escolar - " + usuario.getNombre());
        setSize(950, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();

        pestanas.addTab(
                "Catálogo",
                new LibrosFrame(usuario).getContentPane()
        );

        boolean bibliotecario =
                "ADMIN".equalsIgnoreCase(usuario.getRol())
                        || "BIBLIOTECARIO".equalsIgnoreCase(usuario.getRol());

        if (bibliotecario) {
            pestanas.addTab(
                    "Estudiantes",
                    new EstudiantesFrame().getContentPane()
            );
            pestanas.addTab(
                    "Préstamos",
                    new PrestamosFrame(usuario).getContentPane()
            );
            pestanas.addTab(
                    "Reportes",
                    new ReportesFrame().getContentPane()
            );
        } else {
            pestanas.addTab(
                    "Mis préstamos",
                    new PrestamosFrame(usuario).getContentPane()
            );
        }

        add(pestanas, BorderLayout.CENTER);

        JMenuBar barra = new JMenuBar();
        JMenu sesion = new JMenu("Sesión");
        JMenuItem cerrar = new JMenuItem("Cerrar sesión");

        cerrar.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        sesion.add(cerrar);
        barra.add(sesion);
        setJMenuBar(barra);
    }
}

