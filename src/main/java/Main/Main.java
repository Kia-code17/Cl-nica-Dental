package Main;

import Vista.LoginFrame;

import javax.swing.*;

/**
 * Punto de entrada del sistema.
 * Arranca mostrando la ventana de Login; tras autenticar, abre el Dashboard (MainFrame).
 */
public class Main {
    public static void main(String[] args) {
        // Apariencia nativa del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo aplicar el Look & Feel del sistema: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
