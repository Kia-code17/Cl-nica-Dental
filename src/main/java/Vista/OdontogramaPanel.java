package Vista;

import Modelo.DoctorDAO;
import Modelo.OdontogramaDAO;
import Modelo.PacienteDAO;
import Modelo.Doctor;
import Modelo.OdontogramaItem;
import Modelo.Paciente;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representación visual profesional del odontograma: 32 piezas dentales
 * (numeración universal 1-32) como botones coloreados por estado.
 * Click en un diente -> seleccionar nuevo estado -> se guarda como
 * registro histórico en la tabla `odontogramas`.
 *
 * Diseño responsivo: usa GridBagLayout + JScrollPane para adaptarse
 * a distintos tamaños de ventana, con una paleta de colores clínica
 * y tipografía consistente.
 */
public class OdontogramaPanel extends JPanel {

    private final OdontogramaDAO odontogramaDAO = new OdontogramaDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private final JComboBox<Paciente> cbPaciente = new JComboBox<>();
    private final JComboBox<Doctor> cbDoctor = new JComboBox<>();
    private final Map<Integer, JButton> botonesDiente = new HashMap<>();
    private final Map<Integer, String> estadoActual = new HashMap<>();

    // ---------- Paleta y tipografía ----------
    private static final Color COLOR_FONDO        = new Color(0xF5, 0xF7, 0xFA);
    private static final Color COLOR_PANEL         = Color.WHITE;
    private static final Color COLOR_BORDE         = new Color(0xE0, 0xE4, 0xEA);
    private static final Color COLOR_PRIMARIO      = new Color(0x1F, 0x6F, 0x8B);
    private static final Color COLOR_PRIMARIO_OSC  = new Color(0x14, 0x4F, 0x63);
    private static final Color COLOR_TEXTO         = new Color(0x2E, 0x33, 0x38);
    private static final Color COLOR_TEXTO_SUAVE   = new Color(0x6B, 0x74, 0x7C);

    private static final Font FUENTE_TITULO   = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FUENTE_SUBT     = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FUENTE_LABEL    = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FUENTE_DIENTE   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FUENTE_LEYENDA  = new Font("Segoe UI", Font.PLAIN, 12);

    private static final String[] ESTADOS = {
        "Sano", "Cariado", "Obturado", "Extraido", "Corona", "Endodoncia", "Implante"
    };

    // Arcada superior: 1-16 (izq. a der.) / Arcada inferior: 17-32
    private static final int[] ARCADA_SUPERIOR = range(1, 16);
    private static final int[] ARCADA_INFERIOR = range(17, 32);

    private final JLabel lblPacienteInfo = new JLabel(" ");

    public OdontogramaPanel() {
        setLayout(new BorderLayout());
        setBackground(COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirContenidoCentral(), BorderLayout.CENTER);

        cargarCombos();
        cbPaciente.addActionListener(e -> cargarOdontograma());
    }

    private static int[] range(int a, int b) {
        int[] r = new int[b - a + 1];
        for (int i = 0; i < r.length; i++) r[i] = a + i;
        return r;
    }

    // ================= ENCABEZADO =================

    private JPanel construirEncabezado() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel titulo = new JLabel("Odontograma del paciente");
        titulo.setFont(FUENTE_TITULO);
        titulo.setForeground(COLOR_TEXTO);

        JLabel subtitulo = new JLabel("Numeración universal · 32 piezas dentales");
        subtitulo.setFont(FUENTE_SUBT);
        subtitulo.setForeground(COLOR_TEXTO_SUAVE);

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulos.add(titulo);
        titulos.add(Box.createVerticalStrut(2));
        titulos.add(subtitulo);

        contenedor.add(titulos, BorderLayout.WEST);
        contenedor.add(construirTarjetaSeleccion(), BorderLayout.SOUTH);
        return contenedor;
    }

    private JPanel construirTarjetaSeleccion() {
        JPanel tarjeta = new TarjetaRedondeada();
        tarjeta.setLayout(new GridBagLayout());
        tarjeta.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        estilizarCombo(cbPaciente);
        estilizarCombo(cbDoctor);
        lblPacienteInfo.setFont(FUENTE_SUBT);
        lblPacienteInfo.setForeground(COLOR_TEXTO_SUAVE);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        tarjeta.add(etiqueta("Paciente"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        tarjeta.add(cbPaciente, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        tarjeta.add(etiqueta("Doctor que registra"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        tarjeta.add(cbDoctor, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4; gbc.weightx = 1;
        gbc.insets = new Insets(8, 6, 0, 6);
        tarjeta.add(lblPacienteInfo, gbc);

        return tarjeta;
    }

    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_LABEL);
        lbl.setForeground(COLOR_TEXTO);
        return lbl;
    }

    private void estilizarCombo(JComboBox<?> combo) {
        combo.setFont(FUENTE_SUBT);
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        combo.setPreferredSize(new Dimension(220, 32));
    }

    // ================= CONTENIDO CENTRAL =================

    private JComponent construirContenidoCentral() {
        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JPanel tarjetaOdontograma = new TarjetaRedondeada();
        tarjetaOdontograma.setLayout(new BorderLayout(0, 10));
        tarjetaOdontograma.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel tituloArcadas = new JLabel("Estado dental por pieza");
        tituloArcadas.setFont(FUENTE_LABEL);
        tituloArcadas.setForeground(COLOR_TEXTO);

        JPanel arcadas = new JPanel();
        arcadas.setOpaque(false);
        arcadas.setLayout(new BoxLayout(arcadas, BoxLayout.Y_AXIS));
        arcadas.add(construirFilaArcada("Arcada superior", ARCADA_SUPERIOR));
        arcadas.add(Box.createVerticalStrut(22));
        arcadas.add(construirFilaArcada("Arcada inferior", ARCADA_INFERIOR));

        JScrollPane scroll = new JScrollPane(arcadas);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        tarjetaOdontograma.add(tituloArcadas, BorderLayout.NORTH);
        tarjetaOdontograma.add(scroll, BorderLayout.CENTER);

        contenido.add(tarjetaOdontograma);
        contenido.add(Box.createVerticalStrut(14));
        contenido.add(construirLeyenda());

        return contenido;
    }

    private JPanel construirFilaArcada(String titulo, int[] dientes) {
        JPanel contenedor = new JPanel(new BorderLayout(0, 8));
        contenedor.setOpaque(false);

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(FUENTE_SUBT);
        lbl.setForeground(COLOR_TEXTO_SUAVE);
        contenedor.add(lbl, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, dientes.length, 8, 8));
        grid.setOpaque(false);
        for (int diente : dientes) {
            grid.add(construirBotonDiente(diente));
        }
        contenedor.add(grid, BorderLayout.CENTER);
        return contenedor;
    }

    private JButton construirBotonDiente(int diente) {
        JButton btn = new JButton(String.valueOf(diente));
        btn.setFont(FUENTE_DIENTE);
        btn.setFocusPainted(false);
        btn.setBackground(colorParaEstado("Sano"));
        btn.setForeground(COLOR_TEXTO);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1, true));
        btn.setPreferredSize(new Dimension(46, 46));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Pieza #" + diente + " — clic para actualizar estado");
        btn.addActionListener(e -> cambiarEstadoDiente(diente));
        botonesDiente.put(diente, btn);
        estadoActual.put(diente, "Sano");
        return btn;
    }

    private JPanel construirLeyenda() {
        JPanel tarjeta = new TarjetaRedondeada();
        tarjeta.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        for (String estado : ESTADOS) {
            tarjeta.add(construirChipLeyenda(estado));
        }
        return tarjeta;
    }

    private JPanel construirChipLeyenda(String estado) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chip.setOpaque(false);

        JPanel swatch = new JPanel();
        swatch.setPreferredSize(new Dimension(14, 14));
        swatch.setBackground(colorParaEstado(estado));
        swatch.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1, true));

        JLabel lbl = new JLabel(estado);
        lbl.setFont(FUENTE_LEYENDA);
        lbl.setForeground(COLOR_TEXTO);

        chip.add(swatch);
        chip.add(lbl);
        return chip;
    }

    // ================= LÓGICA =================

    private Color colorParaEstado(String estado) {
        switch (estado) {
            case "Sano":       return new Color(0xC8, 0xE6, 0xC9);
            case "Cariado":    return new Color(0xEF, 0x9A, 0x9A);
            case "Obturado":   return new Color(0xFF, 0xE0, 0x82);
            case "Extraido":   return new Color(0xBD, 0xBD, 0xBD);
            case "Corona":     return new Color(0xB3, 0xE5, 0xFC);
            case "Endodoncia": return new Color(0xCE, 0x93, 0xD8);
            case "Implante":   return new Color(0x80, 0xCB, 0xC4);
            default:           return Color.WHITE;
        }
    }

    private void cargarCombos() {
        try {
            cbPaciente.removeAllItems();
            for (Paciente p : pacienteDAO.listarTodos()) cbPaciente.addItem(p);
            cbDoctor.removeAllItems();
            for (Doctor d : doctorDAO.listarActivos()) cbDoctor.addItem(d);
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void cargarOdontograma() {
        Paciente p = (Paciente) cbPaciente.getSelectedItem();
        if (p == null) {
            lblPacienteInfo.setText(" ");
            return;
        }
        lblPacienteInfo.setText("Mostrando historial clínico más reciente de " + p);
        try {
            // Reinicia todo a "Sano" y luego aplica los estados guardados más recientes
            for (int i = 1; i <= 32; i++) {
                estadoActual.put(i, "Sano");
                botonesDiente.get(i).setBackground(colorParaEstado("Sano"));
            }
            List<OdontogramaItem> items = odontogramaDAO.obtenerEstadoActual(p.getId());
            for (OdontogramaItem it : items) {
                estadoActual.put(it.getDienteId(), it.getEstado());
                JButton btn = botonesDiente.get(it.getDienteId());
                if (btn != null) btn.setBackground(colorParaEstado(it.getEstado()));
            }
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void cambiarEstadoDiente(int diente) {
        Paciente p = (Paciente) cbPaciente.getSelectedItem();
        Doctor d = (Doctor) cbDoctor.getSelectedItem();
        if (p == null || d == null) {
            JOptionPane.showMessageDialog(this, "Selecciona paciente y doctor primero.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nuevoEstado = (String) JOptionPane.showInputDialog(this,
            "Nuevo estado para el diente #" + diente + ":", "Actualizar diente",
            JOptionPane.QUESTION_MESSAGE, null, ESTADOS, estadoActual.get(diente));
        if (nuevoEstado == null) return;

        String notas = JOptionPane.showInputDialog(this, "Notas clínicas (opcional):", "");

        try {
            odontogramaDAO.guardarEstadoDiente(p.getId(), d.getId(), diente, nuevoEstado, notas);
            estadoActual.put(diente, nuevoEstado);
            botonesDiente.get(diente).setBackground(colorParaEstado(nuevoEstado));
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void mostrarError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ================= COMPONENTE AUXILIAR =================

    /** Panel con esquinas redondeadas y fondo blanco, estilo "tarjeta" (card). */
    private static class TarjetaRedondeada extends JPanel {
        TarjetaRedondeada() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR_PANEL);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
            g2.setColor(COLOR_BORDE);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}