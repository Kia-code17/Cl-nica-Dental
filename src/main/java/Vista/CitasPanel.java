package Vista;

import Modelo.CitaDAO;
import Modelo.DoctorDAO;
import Modelo.HorarioDoctorDAO;
import Modelo.PacienteDAO;
import Modelo.Cita;
import Modelo.Doctor;
import Modelo.Paciente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CitasPanel extends JPanel {

    private final CitaDAO citaDAO = new CitaDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final HorarioDoctorDAO horarioDAO = new HorarioDoctorDAO();

    private final EstiloUI.TablaSinEdicion modelo = new EstiloUI.TablaSinEdicion(
        new Object[]{"ID", "Paciente", "Doctor", "Fecha", "Hora", "Estado", "Notas"});
    private final JTable tabla = new JTable(modelo);

    private final JComboBox<Paciente> cbPaciente = new JComboBox<>();
    private final JComboBox<Doctor> cbDoctor = new JComboBox<>();
    private final JTextField txtFecha = new JTextField();
    private final JTextField txtHora = new JTextField();
    private final JComboBox<String> cbEstado = new JComboBox<>(
        new String[]{"Programada", "Confirmada", "Completada", "Cancelada", "Inasistencia"});
    private final JTextArea txtNotas = new JTextArea(3, 18);
    private int idSeleccionado = -1;

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    public CitasPanel() {
        setLayout(new BorderLayout(18, 18));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(EstiloUI.COLOR_FONDO);

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirTarjetaTabla(), BorderLayout.CENTER);
        add(construirFormularioConScroll(), BorderLayout.EAST);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        cargarCombos();
        cargarTabla();
    }

    // ---------------- Encabezado ----------------

    private JPanel construirEncabezado() {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);

        JLabel titulo = new JLabel("Citas");
        titulo.setFont(EstiloUI.F_TITULO);
        titulo.setForeground(EstiloUI.COLOR_TEXTO);

        JLabel subtitulo = new JLabel("Agenda y disponibilidad de citas");
        subtitulo.setFont(EstiloUI.F_SUBTITULO);
        subtitulo.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(3));
        textos.add(subtitulo);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        JButton btnNotificaciones = EstiloUI.botonContorno("Notificaciones", EstiloUI.COLOR_ALERTA);
        btnNotificaciones.addActionListener(e -> mostrarNotificaciones());
        JButton btnHoy = EstiloUI.botonContorno("Citas de hoy", EstiloUI.COLOR_PRIMARIO);
        btnHoy.addActionListener(e -> cargarPorFecha(LocalDate.now()));
        JButton btnTodas = EstiloUI.botonContorno("Ver todas", EstiloUI.COLOR_PRIMARIO);
        btnTodas.addActionListener(e -> cargarTabla());
        acciones.add(btnNotificaciones);
        acciones.add(btnHoy);
        acciones.add(btnTodas);

        fila.add(textos, BorderLayout.WEST);
        fila.add(acciones, BorderLayout.EAST);
        return fila;
    }

    // ---------------- Tabla ----------------

    private JPanel construirTarjetaTabla() {
        EstiloUI.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Color texto, fondo;
                String estado = String.valueOf(v);
                switch (estado) {
                    case "Completada": texto = EstiloUI.COLOR_EXITO; fondo = EstiloUI.COLOR_EXITO_SUAVE; break;
                    case "Cancelada": case "Inasistencia": texto = EstiloUI.COLOR_PELIGRO; fondo = EstiloUI.COLOR_PELIGRO_SUAVE; break;
                    case "Confirmada": texto = EstiloUI.COLOR_PRIMARIO_OSC; fondo = EstiloUI.COLOR_PRIMARIO_SUAVE; break;
                    default: texto = EstiloUI.COLOR_ALERTA; fondo = EstiloUI.COLOR_ALERTA_SUAVE;
                }
                JLabel lbl = EstiloUI.chipEstado(estado, texto, fondo);
                JPanel envoltorio = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
                envoltorio.setBackground(sel ? t.getSelectionBackground() : (row % 2 == 0 ? EstiloUI.COLOR_PANEL : EstiloUI.COLOR_FILA_ALT));
                envoltorio.setOpaque(true);
                envoltorio.add(lbl);
                return envoltorio;
            }
        });

        EstiloUI.TarjetaRedondeada tarjeta = new EstiloUI.TarjetaRedondeada();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.add(EstiloUI.envolverTabla(tabla), BorderLayout.CENTER);
        return tarjeta;
    }

    // ---------------- Formulario ----------------

    /**
     * Envuelve el formulario en un JScrollPane. Antes se agregaba
     * directamente al BorderLayout.EAST sin scroll, así que si la ventana
     * no era lo bastante alta, los últimos campos y botones (p.ej.
     * "Cancelar cita") quedaban cortados y no había forma de llegar a ellos.
     */
    private JComponent construirFormularioConScroll() {
        JComponent formulario = construirFormulario();

        JScrollPane scroll = new JScrollPane(formulario,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel construirFormulario() {
        // OJO: no usar setPreferredSize(new Dimension(340, 0)) aquí. Un
        // JScrollPane calcula cuánto puede scrollear a partir de
        // getPreferredSize() del contenido; si el alto preferido queda fijo
        // en 0, el scroll se corta casi de inmediato aunque el contenido
        // real sea más alto. Por eso solo se fija el ANCHO sobrescribiendo
        // getPreferredSize(), y el alto se deja que lo calcule el layout.
        JPanel envoltorio = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(340, d.height);
            }
        };
        envoltorio.setOpaque(false);

        EstiloUI.TarjetaRedondeada tarjeta = new EstiloUI.TarjetaRedondeada();
        tarjeta.setLayout(new GridBagLayout());
        tarjeta.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        int row = 0;
        gbc.gridy = row++;
        JLabel tituloForm = new JLabel("Nueva / Editar cita");
        tituloForm.setFont(EstiloUI.F_SECCION);
        tituloForm.setForeground(EstiloUI.COLOR_TEXTO);
        gbc.insets = new Insets(0, 0, 16, 0);
        tarjeta.add(tituloForm, gbc);
        gbc.insets = new Insets(4, 0, 4, 0);

        EstiloUI.estilizarCampo(cbPaciente);
        EstiloUI.estilizarCampo(cbDoctor);
        EstiloUI.estilizarCampo(txtFecha);
        EstiloUI.estilizarCampo(txtHora);
        EstiloUI.estilizarCampo(cbEstado);

        row = etiquetaCampo(tarjeta, gbc, row, "Paciente", cbPaciente);
        row = etiquetaCampo(tarjeta, gbc, row, "Doctor", cbDoctor);
        row = etiquetaCampo(tarjeta, gbc, row, "Fecha (aaaa-mm-dd)", txtFecha);
        row = etiquetaCampo(tarjeta, gbc, row, "Hora (HH:mm)", txtHora);
        row = etiquetaCampo(tarjeta, gbc, row, "Estado", cbEstado);

        gbc.gridy = row++;
        tarjeta.add(EstiloUI.crearEtiqueta("Notas"), gbc);

        txtNotas.setFont(EstiloUI.F_CAMPO);
        txtNotas.setLineWrap(true);
        txtNotas.setWrapStyleWord(true);
        txtNotas.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane scrollNotas = new JScrollPane(txtNotas);
        scrollNotas.setBorder(new EstiloUI.BordeRedondeado(EstiloUI.COLOR_BORDE, 8));
        gbc.gridy = row++;
        gbc.insets = new Insets(6, 0, 16, 0);
        tarjeta.add(scrollNotas, gbc);
        gbc.insets = new Insets(4, 0, 4, 0);

        JButton btnGuardar = EstiloUI.botonSolido("Guardar cita", EstiloUI.COLOR_PRIMARIO, EstiloUI.COLOR_PRIMARIO_OSC, Color.WHITE);
        JButton btnNueva = EstiloUI.botonContorno("Nueva", EstiloUI.COLOR_PRIMARIO);
        JButton btnCancelar = EstiloUI.botonContorno("Cancelar cita", EstiloUI.COLOR_PELIGRO);
        btnNueva.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> cancelarCita());

        JPanel botones = new JPanel();
        botones.setOpaque(false);
        botones.setLayout(new BoxLayout(botones, BoxLayout.Y_AXIS));
        for (JButton b : new JButton[]{btnGuardar, btnNueva, btnCancelar}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        }
        botones.add(btnGuardar);
        botones.add(Box.createVerticalStrut(8));
        botones.add(btnNueva);
        botones.add(Box.createVerticalStrut(8));
        botones.add(btnCancelar);

        gbc.gridy = row++;
        gbc.insets = new Insets(8, 0, 0, 0);
        tarjeta.add(botones, gbc);

        envoltorio.add(tarjeta, BorderLayout.NORTH);
        return envoltorio;
    }

    private int etiquetaCampo(JPanel panel, GridBagConstraints gbc, int row, String etiqueta, JComponent campo) {
        gbc.gridy = row++;
        gbc.insets = new Insets(10, 0, 4, 0);
        panel.add(EstiloUI.crearEtiqueta(etiqueta), gbc);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(campo, gbc);
        return row;
    }

    // ---------------- Lógica (sin cambios funcionales) ----------------

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

    private void cargarTabla() {
        try {
            llenarTabla(citaDAO.listarTodas());
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void cargarPorFecha(LocalDate fecha) {
        try {
            llenarTabla(citaDAO.listarPorFecha(fecha));
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void llenarTabla(List<Cita> lista) {
        modelo.setRowCount(0);
        for (Cita c : lista) {
            modelo.addRow(new Object[]{
                c.getId(), c.getPacienteNombre(), c.getDoctorNombre(),
                c.getFecha().format(FMT_FECHA), c.getHora().format(FMT_HORA),
                c.getEstado(), c.getNotas()
            });
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        idSeleccionado = (int) modelo.getValueAt(fila, 0);
        txtFecha.setText((String) modelo.getValueAt(fila, 3));
        txtHora.setText((String) modelo.getValueAt(fila, 4));
        cbEstado.setSelectedItem(modelo.getValueAt(fila, 5));
        txtNotas.setText((String) modelo.getValueAt(fila, 6));
    }

    private void guardar() {
        Paciente p = (Paciente) cbPaciente.getSelectedItem();
        Doctor d = (Doctor) cbDoctor.getSelectedItem();
        if (p == null || d == null || txtFecha.getText().trim().isEmpty() || txtHora.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa paciente, doctor, fecha y hora.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate fecha;
        LocalTime hora;
        try {
            fecha = LocalDate.parse(txtFecha.getText().trim(), FMT_FECHA);
            hora = LocalTime.parse(txtHora.getText().trim(), FMT_HORA);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha/hora inválido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (citaDAO.existeConflicto(d.getId(), fecha, hora, idSeleccionado)) {
                JOptionPane.showMessageDialog(this, "El doctor ya tiene una cita a esa fecha/hora.", "Conflicto de horario", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!horarioDAO.estaDisponible(d.getId(), fecha, hora)) {
                int resp = JOptionPane.showConfirmDialog(this,
                        "Esa fecha/hora está fuera del horario de disponibilidad configurado para " + d.getNombre()
                                + ".\n¿Deseas agendar la cita de todas formas?",
                        "Fuera de horario", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (resp != JOptionPane.YES_OPTION) return;
            }
            Cita c = new Cita();
            c.setId(idSeleccionado);
            c.setPacienteId(p.getId());
            c.setDoctorId(d.getId());
            c.setFecha(fecha);
            c.setHora(hora);
            c.setEstado((String) cbEstado.getSelectedItem());
            c.setNotas(txtNotas.getText().trim());

            if (idSeleccionado == -1) citaDAO.crear(c); else citaDAO.actualizar(c);
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void cancelarCita() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una cita.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            citaDAO.cancelar(idSeleccionado);
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void limpiar() {
        idSeleccionado = -1;
        txtFecha.setText("");
        txtHora.setText("");
        txtNotas.setText("");
        cbEstado.setSelectedIndex(0);
        tabla.clearSelection();
    }

    private void mostrarError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---------------- Notificaciones ----------------

    /**
     * Muestra un panel con dos tipos de aviso:
     *  1) Citas de hoy que siguen "Programada" (pendientes de confirmar).
     *  2) Citas de las próximas 2 horas que ya están agendadas (recordatorio).
     */
    private void mostrarNotificaciones() {
        try {
            List<Cita> pendientesConfirmar = citaDAO.listarPendientesDeConfirmarHoy();
            List<Cita> proximas = citaDAO.listarProximas(2);

            JPanel contenido = new JPanel();
            contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
            contenido.setBackground(EstiloUI.COLOR_PANEL);
            contenido.setBorder(new EmptyBorder(6, 6, 6, 6));

            contenido.add(seccionNotificacion(
                "Pendientes de confirmar hoy (" + pendientesConfirmar.size() + ")",
                pendientesConfirmar, EstiloUI.COLOR_ALERTA));
            contenido.add(Box.createVerticalStrut(14));
            contenido.add(seccionNotificacion(
                "Próximas en las siguientes 2 horas (" + proximas.size() + ")",
                proximas, EstiloUI.COLOR_PRIMARIO));

            if (pendientesConfirmar.isEmpty() && proximas.isEmpty()) {
                JLabel sinAvisos = new JLabel("No hay notificaciones pendientes por ahora.");
                sinAvisos.setFont(EstiloUI.F_CAMPO);
                sinAvisos.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);
                contenido.add(sinAvisos);
            }

            JScrollPane scroll = new JScrollPane(contenido);
            scroll.setBorder(null);
            scroll.setPreferredSize(new Dimension(420, 320));
            JOptionPane.showMessageDialog(this, scroll, "Notificaciones de citas", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private JPanel seccionNotificacion(String titulo, List<Cita> citas, Color color) {
        JPanel seccion = new JPanel();
        seccion.setLayout(new BoxLayout(seccion, BoxLayout.Y_AXIS));
        seccion.setOpaque(false);
        seccion.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(EstiloUI.F_SECCION);
        lblTitulo.setForeground(color);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        seccion.add(lblTitulo);
        seccion.add(Box.createVerticalStrut(6));

        if (citas.isEmpty()) {
            JLabel vacio = new JLabel("Sin elementos.");
            vacio.setFont(EstiloUI.F_CAMPO);
            vacio.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);
            vacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            seccion.add(vacio);
            return seccion;
        }

        for (Cita c : citas) {
            JLabel item = new JLabel(String.format("%s — %s con %s (%s)",
                c.getHora().format(FMT_HORA), c.getPacienteNombre(), c.getDoctorNombre(), c.getEstado()));
            item.setFont(EstiloUI.F_CAMPO);
            item.setForeground(EstiloUI.COLOR_TEXTO);
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.setBorder(new EmptyBorder(3, 4, 3, 4));
            seccion.add(item);
        }
        return seccion;
    }
}