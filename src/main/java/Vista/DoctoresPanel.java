package Vista;

import Modelo.DoctorDAO;
import Modelo.Doctor;
import Modelo.HorarioDoctor;
import Modelo.HorarioDoctorDAO;
import Modelo.CitaDAO;
import Modelo.Cita;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DoctoresPanel extends JPanel {

    private final DoctorDAO dao = new DoctorDAO();
    private final EstiloUI.TablaSinEdicion modelo = new EstiloUI.TablaSinEdicion(
        new Object[]{"ID", "Nombre", "Cédula", "Especialidad", "Teléfono", "Email", "Activo"});
    private final JTable tabla = new JTable(modelo);

    private final JTextField txtNombre = new JTextField();
    private final JTextField txtCedula = new JTextField();
    private final JTextField txtEspecialidad = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private int idSeleccionado = -1;

    // ---- Sub-paneles de las otras pestañas (Responsabilidad: Doctores/Citas) ----
    private final DisponibilidadPanel panelDisponibilidad = new DisponibilidadPanel();
    private final HistorialAtencionesPanel panelHistorial = new HistorialAtencionesPanel();

    public DoctoresPanel() {
        setLayout(new BorderLayout(18, 18));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(EstiloUI.COLOR_FONDO);

        add(construirEncabezado(), BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(EstiloUI.F_SECCION);
        pestanas.setOpaque(false);
        pestanas.addTab("Datos del doctor", construirTabDatos());
        pestanas.addTab("Horario de disponibilidad", panelDisponibilidad);
        pestanas.addTab("Historial de atenciones", panelHistorial);

        // Al cambiar de pestaña, refrescar la lista de doctores en los combos
        // de las otras pestañas (por si se creó/editó un doctor en "Datos").
        pestanas.addChangeListener(e -> {
            int idx = pestanas.getSelectedIndex();
            if (idx == 1) panelDisponibilidad.refrescarDoctores();
            if (idx == 2) panelHistorial.refrescarDoctores();
        });

        add(pestanas, BorderLayout.CENTER);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });
        cargarTabla();
    }

    private JPanel construirTabDatos() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 0, 0, 0));
        panel.add(construirTarjetaTabla(), BorderLayout.CENTER);
        panel.add(construirFormulario(), BorderLayout.EAST);
        return panel;
    }

    // ---------------- Encabezado ----------------

    private JPanel construirEncabezado() {
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel titulo = new JLabel("Doctores");
        titulo.setFont(EstiloUI.F_TITULO);
        titulo.setForeground(EstiloUI.COLOR_TEXTO);

        JLabel subtitulo = new JLabel("Personal médico, especialidades, disponibilidad e historial");
        subtitulo.setFont(EstiloUI.F_SUBTITULO);
        subtitulo.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(3));
        textos.add(subtitulo);

        envoltorio.add(textos, BorderLayout.WEST);
        return envoltorio;
    }

    // ---------------- Tabla ----------------

    private JPanel construirTarjetaTabla() {
        EstiloUI.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(6).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = EstiloUI.chipEstado(String.valueOf(v),
                        "Sí".equals(v) ? EstiloUI.COLOR_EXITO : EstiloUI.COLOR_PELIGRO,
                        "Sí".equals(v) ? EstiloUI.COLOR_EXITO_SUAVE : EstiloUI.COLOR_PELIGRO_SUAVE);
                JPanel envoltorio = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                envoltorio.setBackground(sel ? t.getSelectionBackground() : (row % 2 == 0 ? EstiloUI.COLOR_PANEL : EstiloUI.COLOR_FILA_ALT));
                envoltorio.setOpaque(true);
                envoltorio.add(lbl);
                return envoltorio;
            }
        });

        EstiloUI.TarjetaRedondeada tarjeta = new EstiloUI.TarjetaRedondeada();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(4, 0, 0, 0));
        tarjeta.add(EstiloUI.envolverTabla(tabla), BorderLayout.CENTER);
        return tarjeta;
    }

    // ---------------- Formulario ----------------

    private JPanel construirFormulario() {
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.setPreferredSize(new Dimension(340, 0));
        envoltorio.setMinimumSize(new Dimension(300, 0));

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
        JLabel tituloForm = new JLabel("Datos del doctor");
        tituloForm.setFont(EstiloUI.F_SECCION);
        tituloForm.setForeground(EstiloUI.COLOR_TEXTO);
        gbc.insets = new Insets(0, 0, 16, 0);
        tarjeta.add(tituloForm, gbc);
        gbc.insets = new Insets(4, 0, 4, 0);

        row = etiquetaCampo(tarjeta, gbc, row, "Nombre completo", txtNombre);
        row = etiquetaCampo(tarjeta, gbc, row, "Cédula", txtCedula);
        row = etiquetaCampo(tarjeta, gbc, row, "Especialidad", txtEspecialidad);
        row = etiquetaCampo(tarjeta, gbc, row, "Teléfono", txtTelefono);
        row = etiquetaCampo(tarjeta, gbc, row, "Email", txtEmail);

        JButton btnGuardar = EstiloUI.botonSolido("Guardar", EstiloUI.COLOR_PRIMARIO, EstiloUI.COLOR_PRIMARIO_OSC, Color.WHITE);
        JButton btnNuevo = EstiloUI.botonContorno("Nuevo", EstiloUI.COLOR_PRIMARIO);
        JButton btnEliminar = EstiloUI.botonContorno("Desactivar", EstiloUI.COLOR_PELIGRO);
        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());

        JPanel botones = new JPanel();
        botones.setOpaque(false);
        botones.setLayout(new BoxLayout(botones, BoxLayout.Y_AXIS));
        for (JButton b : new JButton[]{btnGuardar, btnNuevo, btnEliminar}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        }
        botones.add(btnGuardar);
        botones.add(Box.createVerticalStrut(8));
        botones.add(btnNuevo);
        botones.add(Box.createVerticalStrut(8));
        botones.add(btnEliminar);

        gbc.gridy = row++;
        gbc.insets = new Insets(12, 0, 0, 0);
        tarjeta.add(botones, gbc);

        envoltorio.add(tarjeta, BorderLayout.NORTH);
        return envoltorio;
    }

    private int etiquetaCampo(JPanel panel, GridBagConstraints gbc, int row, String etiqueta, JTextField campo) {
        EstiloUI.estilizarCampo(campo);
        gbc.gridy = row++;
        gbc.insets = new Insets(10, 0, 4, 0);
        panel.add(EstiloUI.crearEtiqueta(etiqueta), gbc);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(campo, gbc);
        return row;
    }

    // ---------------- Lógica (sin cambios funcionales) ----------------

    private void cargarTabla() {
        try {
            List<Doctor> lista = dao.listarTodos();
            modelo.setRowCount(0);
            for (Doctor d : lista) {
                modelo.addRow(new Object[]{d.getId(), d.getNombre(), d.getCedula(), d.getEspecialidad(),
                    d.getTelefono(), d.getEmail(), d.isActivo() ? "Sí" : "No"});
            }
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        idSeleccionado = (int) modelo.getValueAt(fila, 0);
        txtNombre.setText((String) modelo.getValueAt(fila, 1));
        txtCedula.setText((String) modelo.getValueAt(fila, 2));
        txtEspecialidad.setText((String) modelo.getValueAt(fila, 3));
        txtTelefono.setText((String) modelo.getValueAt(fila, 4));
        txtEmail.setText((String) modelo.getValueAt(fila, 5));
    }

    private void guardar() {
        if (txtNombre.getText().trim().isEmpty() || txtCedula.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y cédula son obligatorios.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Doctor d = new Doctor();
        d.setId(idSeleccionado);
        d.setNombre(txtNombre.getText().trim());
        d.setCedula(txtCedula.getText().trim());
        d.setEspecialidad(txtEspecialidad.getText().trim());
        d.setTelefono(txtTelefono.getText().trim());
        d.setEmail(txtEmail.getText().trim());
        d.setActivo(true);
        try {
            if (idSeleccionado == -1) dao.crear(d); else dao.actualizar(d);
            limpiar();
            cargarTabla();
            panelDisponibilidad.refrescarDoctores();
            panelHistorial.refrescarDoctores();
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void eliminar() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un doctor.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            dao.eliminar(idSeleccionado);
            limpiar();
            cargarTabla();
            panelDisponibilidad.refrescarDoctores();
            panelHistorial.refrescarDoctores();
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void limpiar() {
        idSeleccionado = -1;
        txtNombre.setText(""); txtCedula.setText(""); txtEspecialidad.setText("");
        txtTelefono.setText(""); txtEmail.setText("");
        tabla.clearSelection();
    }

    private static void mostrarError(SQLException ex) {
        JOptionPane.showMessageDialog(null, "Error de base de datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ========================================================================
    // PESTAÑA: Horario de disponibilidad
    // ========================================================================
    private static class DisponibilidadPanel extends JPanel {
        private final DoctorDAO doctorDAO = new DoctorDAO();
        private final HorarioDoctorDAO horarioDAO = new HorarioDoctorDAO();

        private final JComboBox<Doctor> cbDoctor = new JComboBox<>();
        private final JComboBox<String> cbDia = new JComboBox<>(HorarioDoctorDAO.DIAS);
        private final JTextField txtInicio = new JTextField();
        private final JTextField txtFin = new JTextField();

        private final EstiloUI.TablaSinEdicion modeloHorarios = new EstiloUI.TablaSinEdicion(
            new Object[]{"ID", "Día", "Desde", "Hasta"});
        private final JTable tablaHorarios = new JTable(modeloHorarios);
        private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

        DisponibilidadPanel() {
            setLayout(new BorderLayout(18, 18));
            setOpaque(false);
            setBorder(new EmptyBorder(14, 0, 0, 0));

            add(construirFormularioSuperior(), BorderLayout.NORTH);
            add(construirTabla(), BorderLayout.CENTER);

            cbDoctor.addActionListener(e -> cargarHorarios());
            refrescarDoctores();
        }

        private JComponent construirFormularioSuperior() {
            EstiloUI.TarjetaRedondeada tarjeta = new EstiloUI.TarjetaRedondeada();
            tarjeta.setLayout(new GridBagLayout());
            tarjeta.setBorder(new EmptyBorder(18, 18, 18, 18));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 8, 4, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridy = 0;

            EstiloUI.estilizarCampo(cbDoctor);
            EstiloUI.estilizarCampo(cbDia);
            EstiloUI.estilizarCampo(txtInicio);
            EstiloUI.estilizarCampo(txtFin);
            txtInicio.setToolTipText("Formato HH:mm, ej. 08:00");
            txtFin.setToolTipText("Formato HH:mm, ej. 16:00");

            gbc.gridx = 0; gbc.weightx = 2; agregarConEtiqueta(tarjeta, gbc, "Doctor", cbDoctor);
            gbc.gridx = 1; gbc.weightx = 1; agregarConEtiqueta(tarjeta, gbc, "Día", cbDia);
            gbc.gridx = 2; gbc.weightx = 1; agregarConEtiqueta(tarjeta, gbc, "Desde (HH:mm)", txtInicio);
            gbc.gridx = 3; gbc.weightx = 1; agregarConEtiqueta(tarjeta, gbc, "Hasta (HH:mm)", txtFin);

            JButton btnAgregar = EstiloUI.botonSolido("Agregar bloque", EstiloUI.COLOR_PRIMARIO, EstiloUI.COLOR_PRIMARIO_OSC, Color.WHITE);
            btnAgregar.addActionListener(e -> agregarHorario());
            JButton btnQuitar = EstiloUI.botonContorno("Quitar seleccionado", EstiloUI.COLOR_PELIGRO);
            btnQuitar.addActionListener(e -> quitarHorario());

            gbc.gridx = 4; gbc.weightx = 1;
            gbc.gridy = 1;
            tarjeta.add(btnAgregar, gbc);
            gbc.gridy = 2;
            tarjeta.add(btnQuitar, gbc);

            return tarjeta;
        }

        private void agregarConEtiqueta(JPanel panel, GridBagConstraints gbc, String etiqueta, JComponent campo) {
            JPanel columna = new JPanel(new BorderLayout(0, 4));
            columna.setOpaque(false);
            columna.add(EstiloUI.crearEtiqueta(etiqueta), BorderLayout.NORTH);
            columna.add(campo, BorderLayout.CENTER);
            panel.add(columna, gbc);
        }

        private JComponent construirTabla() {
            EstiloUI.estilizarTabla(tablaHorarios);
            EstiloUI.TarjetaRedondeada tarjeta = new EstiloUI.TarjetaRedondeada();
            tarjeta.setLayout(new BorderLayout(0, 10));
            tarjeta.setBorder(new EmptyBorder(18, 18, 18, 18));

            JLabel titulo = new JLabel("Calendario semanal de disponibilidad");
            titulo.setFont(EstiloUI.F_SECCION);
            titulo.setForeground(EstiloUI.COLOR_TEXTO);
            tarjeta.add(titulo, BorderLayout.NORTH);
            tarjeta.add(EstiloUI.envolverTabla(tablaHorarios), BorderLayout.CENTER);
            return tarjeta;
        }

        void refrescarDoctores() {
            Doctor seleccionActual = (Doctor) cbDoctor.getSelectedItem();
            try {
                cbDoctor.removeAllItems();
                List<Doctor> doctores = doctorDAO.listarActivos();
                for (Doctor d : doctores) cbDoctor.addItem(d);
                if (seleccionActual != null) {
                    for (Doctor d : doctores) {
                        if (d.getId() == seleccionActual.getId()) { cbDoctor.setSelectedItem(d); break; }
                    }
                }
            } catch (SQLException ex) {
                mostrarError(ex);
            }
            cargarHorarios();
        }

        private void cargarHorarios() {
            Doctor d = (Doctor) cbDoctor.getSelectedItem();
            modeloHorarios.setRowCount(0);
            if (d == null) return;
            try {
                for (HorarioDoctor h : horarioDAO.listarPorDoctor(d.getId())) {
                    modeloHorarios.addRow(new Object[]{
                        h.getId(), h.getDia(), h.getHoraInicio().format(FMT_HORA), h.getHoraFin().format(FMT_HORA)
                    });
                }
            } catch (SQLException ex) {
                mostrarError(ex);
            }
        }

        private void agregarHorario() {
            Doctor d = (Doctor) cbDoctor.getSelectedItem();
            if (d == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un doctor.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            LocalTime inicio, fin;
            try {
                inicio = LocalTime.parse(txtInicio.getText().trim(), FMT_HORA);
                fin = LocalTime.parse(txtFin.getText().trim(), FMT_HORA);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Formato de hora inválido. Usa HH:mm (ej. 08:00).", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!fin.isAfter(inicio)) {
                JOptionPane.showMessageDialog(this, "La hora de fin debe ser posterior a la de inicio.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                HorarioDoctor h = new HorarioDoctor(d.getId(), (String) cbDia.getSelectedItem(), inicio, fin);
                horarioDAO.crear(h);
                txtInicio.setText("");
                txtFin.setText("");
                cargarHorarios();
            } catch (SQLException ex) {
                mostrarError(ex);
            }
        }

        private void quitarHorario() {
            int fila = tablaHorarios.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(this, "Selecciona un bloque de horario en la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) modeloHorarios.getValueAt(fila, 0);
            try {
                horarioDAO.eliminar(id);
                cargarHorarios();
            } catch (SQLException ex) {
                mostrarError(ex);
            }
        }
    }

    // ========================================================================
    // PESTAÑA: Historial de atenciones (por doctor)
    // ========================================================================
    private static class HistorialAtencionesPanel extends JPanel {
        private final DoctorDAO doctorDAO = new DoctorDAO();
        private final CitaDAO citaDAO = new CitaDAO();

        private final JComboBox<Doctor> cbDoctor = new JComboBox<>();
        private final EstiloUI.TablaSinEdicion modeloHistorial = new EstiloUI.TablaSinEdicion(
            new Object[]{"ID", "Paciente", "Fecha", "Hora", "Estado", "Notas"});
        private final JTable tablaHistorial = new JTable(modeloHistorial);
        private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ISO_LOCAL_DATE;
        private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

        HistorialAtencionesPanel() {
            setLayout(new BorderLayout(14, 14));
            setOpaque(false);
            setBorder(new EmptyBorder(14, 0, 0, 0));

            JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            filtro.setOpaque(false);
            EstiloUI.estilizarCampo(cbDoctor);
            cbDoctor.setPreferredSize(new Dimension(280, 42));
            filtro.add(EstiloUI.crearEtiqueta("Doctor:"));
            filtro.add(cbDoctor);
            add(filtro, BorderLayout.NORTH);

            EstiloUI.estilizarTabla(tablaHistorial);
            EstiloUI.TarjetaRedondeada tarjeta = new EstiloUI.TarjetaRedondeada();
            tarjeta.setLayout(new BorderLayout());
            tarjeta.add(EstiloUI.envolverTabla(tablaHistorial), BorderLayout.CENTER);
            add(tarjeta, BorderLayout.CENTER);

            cbDoctor.addActionListener(e -> cargarHistorial());
            refrescarDoctores();
        }

        void refrescarDoctores() {
            Doctor seleccionActual = (Doctor) cbDoctor.getSelectedItem();
            try {
                cbDoctor.removeAllItems();
                List<Doctor> doctores = doctorDAO.listarTodos();
                for (Doctor d : doctores) cbDoctor.addItem(d);
                if (seleccionActual != null) {
                    for (Doctor d : doctores) {
                        if (d.getId() == seleccionActual.getId()) { cbDoctor.setSelectedItem(d); break; }
                    }
                }
            } catch (SQLException ex) {
                mostrarError(ex);
            }
            cargarHistorial();
        }

        private void cargarHistorial() {
            Doctor d = (Doctor) cbDoctor.getSelectedItem();
            modeloHistorial.setRowCount(0);
            if (d == null) return;
            try {
                List<Cita> citas = citaDAO.listarPorDoctor(d.getId());
                for (Cita c : citas) {
                    modeloHistorial.addRow(new Object[]{
                        c.getId(), c.getPacienteNombre(), c.getFecha().format(FMT_FECHA),
                        c.getHora().format(FMT_HORA), c.getEstado(), c.getNotas()
                    });
                }
            } catch (SQLException ex) {
                mostrarError(ex);
            }
        }
    }
}
