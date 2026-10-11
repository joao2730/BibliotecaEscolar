package app;

import view.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ex) {
                System.out.println(
                        "Se utilizará el diseño predeterminado."
                );
            }

            new LoginFrame().setVisible(true);
        });
    }
}

