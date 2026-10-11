package view;

import controller.AutenticacionController;
import model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Optional;

public class LoginFrame extends JFrame {

    private final JTextField correo = new JTextField(20);
    private final JPasswordField contrasena = new JPasswordField(20);
    private final AutenticacionController controller =
            new AutenticacionController();

    public LoginFrame() {
        setTitle("Biblioteca Escolar - Iniciar sesión");
        setSize(400, 220);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Correo:"));
        panel.add(correo);

        panel.add(new JLabel("Contraseña:"));
        panel.add(contrasena);

        JButton ingresar = new JButton("Ingresar");
        panel.add(new JLabel());
        panel.add(ingresar);

        ingresar.addActionListener(e -> iniciarSesion());
        getRootPane().setDefaultButton(ingresar);

        add(panel);
    }

    private void iniciarSesion() {
        try {
            Optional<Usuario> resultado = controller.iniciarSesion(
                    correo.getText(),
                    new String(contrasena.getPassword())
            );

            if (resultado.isPresent()) {
                new MainFrame(resultado.get()).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Correo o contraseña incorrectos."
                );
            }

        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error de acceso",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

