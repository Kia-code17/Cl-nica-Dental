package Vista;

import Modelo.Doctor;
import Modelo.DoctorDAO;
import Modelo.HistorialMedico;
import Modelo.HistorialMedicoDAO;
import Modelo.Paciente;
import Modelo.PacienteDAO;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Panel de gestión de pacientes: búsqueda, filtros (estado/doctor), listado,
 * formulario de alta/edición con foto, historial médico y exportación a CSV.
 * Usa EstiloUI para mantener el mismo look en todo el sistema.
 */
public class PacientesPanel extends JPanel {

    private static final Path CARPETA_FOTOS = Paths.get("fotos_pacientes");
    private static final Pattern PATRON_EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^[0-9()+\\-\\s]{7,20}$");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final PacienteDAO dao = new PacienteDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final HistorialMedicoDAO historialDAO = new HistorialMedicoDAO();

    private final EstiloUI.TablaSinEdicion modelo = new EstiloUI.TablaSinEdicion(
            new Object[]{"ID", "Nombre", "Cédula", "Nac.", "Teléfono", "Email", "Alergias", "Activo"});
    private final JTable tabla = new JTable(modelo);

    private final JTextField txtBuscar = new JTextField();
    private final JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Todos", "Activos", "Inactivos"});
    private final JComboBox<Object> cbDoctorFiltro = new JComboBox<>();

    private final JTextField txtNombre = new JTextField();
    private final JTextField txtCedula = new JTextField();
    private final JTextField txtNacimiento = new JTextField(); // yyyy-MM-dd
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtDireccion = new JTextField();
    private final JTextArea txtAlergias = new JTextArea(3, 20);

    private final JLabel lblFoto = new JLabel();
    private String fotoSeleccionadaPath = null; // ruta relativa que se guardará en BD

    private int idSeleccionado = -1;

    public PacientesPanel() {
        setLayout(new BorderLayout());
        setBackground(EstiloUI.COLOR_FONDO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        try {
            Files.createDirectories(CARPETA_FOTOS);
        } catch (IOException ignored) { /* si no se puede crear, simplemente no habrá fotos */ }

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirCuerpo(), BorderLayout.CENTER);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        cargarCombosDoctor();
        cargarTabla();
    }

    // ================= ENCABEZADO =================
    private JPanel construirEncabezado() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titulo = new JLabel("Pacientes");
        titulo.setFont(EstiloUI.F_TITULO);
        titulo.setForeground(EstiloUI.COLOR_TEXTO);

        JLabel subtitulo = new JLabel("Consulta, registra y actualiza los datos clínicos de tus pacientes");
        subtitulo.setFont(EstiloUI.F_SUBTITULO);
        subtitulo.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulos.add(titulo);
        titulos.add(Box.createVerticalStrut(2));
        titulos.add(subtitulo);

        contenedor.add(titulos, BorderLayout.WEST);
        contenedor.add(construirBarraBusqueda(), BorderLayout.SOUTH);
        return contenedor;
    }

    private JPanel construirBarraBusqueda() {
        EstiloUI.TarjetaRedondeada tarjeta = new EstiloUI.TarjetaRedondeada();
        tarjeta.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        tarjeta.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lbl = EstiloUI.crearEtiqueta("Buscar (nombre, cédula o teléfono)");
        EstiloUI.estilizarCampo(txtBuscar);
        txtBuscar.setPreferredSize(new Dimension(220, 36));
        txtBuscar.addActionListener(e -> cargarTabla());

        JButton btnBuscar = EstiloUI.botonSolido("Buscar", EstiloUI.COLOR_PRIMARIO, EstiloUI.COLOR_PRIMARIO_OSC, Color.WHITE);
        btnBuscar.addActionListener(e -> cargarTabla());

        JButton btnLimpiar = EstiloUI.botonContorno("Ver todos", EstiloUI.COLOR_PRIMARIO);
        btnLimpiar.addActionListener(e -> {
            txtBuscar.setText("");
            cbEstado.setSelectedIndex(0);
            cbDoctorFiltro.setSelectedIndex(0);
            cargarTabla();
        });

        JLabel lblEstado = EstiloUI.crearEtiqueta("Estado");
        cbEstado.setPreferredSize(new Dimension(120, 36));
        cbEstado.addActionListener(e -> cargarTabla());

        JLabel lblDoctor = EstiloUI.crearEtiqueta("Doctor");
        cbDoctorFiltro.setPreferredSize(new Dimension(200, 36));
        cbDoctorFiltro.addActionListener(e -> cargarTabla());

        JButton btnExportar = EstiloUI.botonContorno("Exportar CSV", EstiloUI.COLOR_ACENTO);
        btnExportar.addActionListener(e -> exportarCsv());

        tarjeta.add(lbl);
        tarjeta.add(txtBuscar);
        tarjeta.add(btnBuscar);
        tarjeta.add(lblEstado);
        tarjeta.add(cbEstado);
        tarjeta.add(lblDoctor);
        tarjeta.add(cbDoctorFiltro);
        tarjeta.add(btnLimpiar);
        tarjeta.add(btnExportar);
        return tarjeta;
    }

    // ================= CUERPO (tabla + formulario) =================
    private JComponent construirCuerpo() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, construirTarjetaTabla(), construirTarjetaFormulario());
        split.setResizeWeight(0.62);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setOpaque(false);
        split.setDividerSize(14);
        split.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override public void paint(Graphics g) { /* divisor invisible */ }
                };
            }
        });
        return split;
    }

    private JPanel construirTarjetaTabla() {
        EstiloUI.TarjetaRedondeada tarjeta = new EstiloUI.TarjetaRedondeada();
        tarjeta.setLayout(new BorderLayout(0, 10));
        tarjeta.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel titulo = new JLabel("Listado de pacientes");
        titulo.setFont(EstiloUI.F_SECCION);
        titulo.setForeground(EstiloUI.COLOR_TEXTO);

        estilizarTabla();
        tarjeta.add(titulo, BorderLayout.NORTH);
        tarjeta.add(EstiloUI.envolverTabla(tabla), BorderLayout.CENTER);
        return tarjeta;
    }

    private void estilizarTabla() {
        EstiloUI.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(7).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = EstiloUI.chipEstado(String.valueOf(v),
                        "Sí".equals(v) ? EstiloUI.COLOR_EXITO : EstiloUI.COLOR_PELIGRO,
                        "Sí".equals(v) ? EstiloUI.COLOR_EXITO_SUAVE : EstiloUI.COLOR_PELIGRO_SUAVE);
                JPanel envoltorio = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                envoltorio.setOpaque(sel);
                if (sel) envoltorio.setBackground(t.getSelectionBackground());
                else envoltorio.setBackground(row % 2 == 0 ? EstiloUI.COLOR_PANEL : EstiloUI.COLOR_FILA_ALT);
                envoltorio.add(lbl);
                return envoltorio;
            }
        });
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(70);
    }

    private JPanel construirTarjetaFormulario() {
        EstiloUI.TarjetaRedondeada tarjeta = new EstiloUI.TarjetaRedondeada();
        tarjeta.setLayout(new BorderLayout(0, 12));
        tarjeta.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel titulo = new JLabel("Datos del paciente");
        titulo.setFont(EstiloUI.F_SECCION);
        titulo.setForeground(EstiloUI.COLOR_TEXTO);

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        int row = 0;

        row = agregarFilaFoto(campos, gbc, row);
        row = agregarCampo(campos, gbc, row, "Nombre completo", txtNombre);
        row = agregarCampo(campos, gbc, row, "Cédula", txtCedula);
        row = agregarCampo(campos, gbc, row, "Fecha de nacimiento (aaaa-mm-dd)", txtNacimiento);
        row = agregarCampo(campos, gbc, row, "Teléfono", txtTelefono);
        row = agregarCampo(campos, gbc, row, "Email", txtEmail);
        row = agregarCampo(campos, gbc, row, "Dirección", txtDireccion);

        gbc.gridy = row++;
        campos.add(EstiloUI.crearEtiqueta("Alergias"), gbc);
        gbc.gridy = row++;
        txtAlergias.setFont(EstiloUI.F_CAMPO);
        txtAlergias.setLineWrap(true);
        txtAlergias.setWrapStyleWord(true);
        txtAlergias.setBorder(new EmptyBorder(7, 9, 7, 9));
        JScrollPane scrollAlergias = new JScrollPane(txtAlergias);
        scrollAlergias.setBorder(new EstiloUI.BordeRedondeado(EstiloUI.COLOR_BORDE, 8));
        campos.add(scrollAlergias, gbc);

        JScrollPane scrollFormulario = new JScrollPane(campos);
        scrollFormulario.setBorder(null);
        scrollFormulario.setOpaque(false);
        scrollFormulario.getViewport().setOpaque(false);
        scrollFormulario.getVerticalScrollBar().setUnitIncrement(14);

        JPanel botonesFila1 = new JPanel(new GridLayout(1, 3, 8, 0));
        botonesFila1.setOpaque(false);
        JButton btnNuevo = EstiloUI.botonContorno("Nuevo", EstiloUI.COLOR_PRIMARIO);
        JButton btnGuardar = EstiloUI.botonSolido("Guardar", EstiloUI.COLOR_PRIMARIO, EstiloUI.COLOR_PRIMARIO_OSC, Color.WHITE);
        JButton btnEliminar = EstiloUI.botonContorno("Eliminar", EstiloUI.COLOR_PELIGRO);
        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        botonesFila1.add(btnNuevo);
        botonesFila1.add(btnGuardar);
        botonesFila1.add(btnEliminar);

        JPanel botonesFila2 = new JPanel(new GridLayout(1, 1, 8, 0));
        botonesFila2.setOpaque(false);
        JButton btnHistorial = EstiloUI.botonContorno("Historial médico", EstiloUI.COLOR_ACENTO);
        btnHistorial.addActionListener(e -> abrirHistorialMedico());
        botonesFila2.add(btnHistorial);

        JPanel botones = new JPanel(new BorderLayout(0, 8));
        botones.setOpaque(false);
        botones.add(botonesFila1, BorderLayout.NORTH);
        botones.add(botonesFila2, BorderLayout.SOUTH);

        tarjeta.add(titulo, BorderLayout.NORTH);
        tarjeta.add(scrollFormulario, BorderLayout.CENTER);
        tarjeta.add(botones, BorderLayout.SOUTH);
        return tarjeta;
    }

    private int agregarFilaFoto(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridy = row++;
        panel.add(EstiloUI.crearEtiqueta("Foto del paciente"), gbc);

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        fila.setOpaque(false);

        lblFoto.setPreferredSize(new Dimension(64, 64));
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblFoto.setBorder(new EstiloUI.BordeRedondeado(EstiloUI.COLOR_BORDE, 8));
        mostrarFotoPorDefecto();

        JButton btnElegir = EstiloUI.botonContorno("Elegir foto...", EstiloUI.COLOR_PRIMARIO);
        btnElegir.addActionListener(e -> elegirFoto());

        fila.add(lblFoto);
        fila.add(btnElegir);

        gbc.gridy = row++;
        panel.add(fila, gbc);
        return row;
    }

    // ================= FOTO =================
    private void elegirFoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes (jpg, jpeg, png)", "jpg", "jpeg", "png"));
        int resultado = chooser.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) return;

        File origen = chooser.getSelectedFile();
        try {
            String extension = obtenerExtension(origen.getName());
            String nombreDestino = "paciente_" + System.currentTimeMillis() + extension;
            Path destino = CARPETA_FOTOS.resolve(nombreDestino);
            Files.copy(origen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            fotoSeleccionadaPath = destino.toString();
            mostrarFoto(destino);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la imagen:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        int punto = nombreArchivo.lastIndexOf('.');
        return punto >= 0 ? nombreArchivo.substring(punto) : ".jpg";
    }

    private void mostrarFoto(Path ruta) {
        try {
            BufferedImage img = ImageIO.read(ruta.toFile());
            if (img == null) { mostrarFotoPorDefecto(); return; }
            Image escalada = img.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            lblFoto.setIcon(new ImageIcon(escalada));
            lblFoto.setText(null);
        } catch (IOException ex) {
            mostrarFotoPorDefecto();
        }
    }

    private void mostrarFotoPorDefecto() {
        lblFoto.setIcon(null);
        lblFoto.setText("Sin foto");
        lblFoto.setFont(EstiloUI.F_SUBTITULO);
        lblFoto.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);
    }

    // ================= HISTORIAL MÉDICO =================
    private void abrirHistorialMedico() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un paciente de la tabla primero.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Paciente p = dao.buscarPorId(idSeleccionado);
        if (p == null) return;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Historial médico — " + p.getNombre());
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(640, 480);
        dialog.setLocationRelativeTo(this);

        EstiloUI.TablaSinEdicion modeloHist = new EstiloUI.TablaSinEdicion(new Object[]{"Fecha", "Doctor", "Notas"});
        JTable tablaHist = new JTable(modeloHist);
        EstiloUI.estilizarTabla(tablaHist);

        Runnable recargar = () -> {
            modeloHist.setRowCount(0);
            try {
                List<HistorialMedico> lista = historialDAO.listarPorPaciente(idSeleccionado);
                DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                for (HistorialMedico h : lista) {
                    modeloHist.addRow(new Object[]{
                            h.getFecha() != null ? h.getFecha().format(fmtFecha) : "",
                            h.getDoctorNombre(),
                            h.getNotas()
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error al cargar historial:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        JPanel panelNuevo = new JPanel(new BorderLayout(8, 8));
        panelNuevo.setBorder(new EmptyBorder(10, 0, 0, 0));

        JComboBox<Doctor> cbDoctor = new JComboBox<>();
        try {
            for (Doctor d : doctorDAO.listarActivos()) cbDoctor.addItem(d);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog, "No se pudieron cargar los doctores:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        JTextArea txtNotas = new JTextArea(3, 20);
        txtNotas.setLineWrap(true);
        txtNotas.setWrapStyleWord(true);
        JScrollPane scrollNotas = new JScrollPane(txtNotas);

        JButton btnAgregar = EstiloUI.botonSolido("Agregar entrada", EstiloUI.COLOR_PRIMARIO, EstiloUI.COLOR_PRIMARIO_OSC, Color.WHITE);
        btnAgregar.addActionListener(e -> {
            Doctor d = (Doctor) cbDoctor.getSelectedItem();
            String notas = txtNotas.getText().trim();
            if (d == null) {
                JOptionPane.showMessageDialog(dialog, "Selecciona un doctor.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (notas.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Escribe una nota clínica.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                historialDAO.crear(new HistorialMedico(idSeleccionado, d.getId(), notas));
                txtNotas.setText("");
                recargar.run();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error al guardar:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel filaDoctor = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filaDoctor.add(EstiloUI.crearEtiqueta("Doctor"));
        filaDoctor.add(cbDoctor);

        panelNuevo.add(filaDoctor, BorderLayout.NORTH);
        panelNuevo.add(scrollNotas, BorderLayout.CENTER);
        panelNuevo.add(btnAgregar, BorderLayout.SOUTH);

        dialog.add(EstiloUI.envolverTabla(tablaHist), BorderLayout.CENTER);
        dialog.add(panelNuevo, BorderLayout.SOUTH);

        recargar.run();
        dialog.setVisible(true);
    }

    // ================= EXPORTAR CSV =================
    private void exportarCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("pacientes.csv"));
        int resultado = chooser.showSaveDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) return;

        List<Paciente> lista = obtenerListaFiltrada();
        try {
            dao.exportarCSV(lista, chooser.getSelectedFile().getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Exportado correctamente (" + lista.size() + " pacientes).",
                    "Exportar CSV", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= FILTROS / CARGA DE DATOS =================
    private void cargarCombosDoctor() {
        cbDoctorFiltro.removeAllItems();
        cbDoctorFiltro.addItem("Todos");
        try {
            for (Doctor d : doctorDAO.listarActivos()) cbDoctorFiltro.addItem(d);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los doctores:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int agregarCampo(JPanel panel, GridBagConstraints gbc, int row, String etiqueta, JTextField campo) {
        gbc.gridy = row++;
        panel.add(EstiloUI.crearEtiqueta(etiqueta), gbc);
        gbc.gridy = row++;
        EstiloUI.estilizarCampo(campo);
        panel.add(campo, gbc);
        return row;
    }

    private List<Paciente> obtenerListaFiltrada() {
        Boolean estado = null;
        if (cbEstado.getSelectedIndex() == 1) estado = Boolean.TRUE;
        else if (cbEstado.getSelectedIndex() == 2) estado = Boolean.FALSE;

        Object doctorSel = cbDoctorFiltro.getSelectedItem();
        if (doctorSel instanceof Doctor) {
            // El filtro por doctor manda: se combina con el texto de búsqueda de forma local.
            List<Paciente> porDoctor = dao.listarPorDoctor(((Doctor) doctorSel).getId());
            String termino = txtBuscar.getText().trim().toLowerCase();
            if (termino.isEmpty() && estado == null) return porDoctor;
            List<Paciente> filtrado = new java.util.ArrayList<>();
            for (Paciente p : porDoctor) {
                boolean coincideTexto = termino.isEmpty()
                        || p.getNombre().toLowerCase().contains(termino)
                        || (p.getCedula() != null && p.getCedula().toLowerCase().contains(termino));
                boolean coincideEstado = estado == null || p.isActivo() == estado;
                if (coincideTexto && coincideEstado) filtrado.add(p);
            }
            return filtrado;
        }
        return dao.listarPorEstado(estado, txtBuscar.getText());
    }

    private void cargarTabla() {
        List<Paciente> lista = obtenerListaFiltrada();
        modelo.setRowCount(0);
        for (Paciente p : lista) {
            modelo.addRow(new Object[]{
                    p.getId(), p.getNombre(), p.getCedula(),
                    p.getFechaNacimiento() != null ? p.getFechaNacimiento().format(FMT) : "",
                    p.getTelefono(), p.getEmail(), p.getAlergias(),
                    p.isActivo() ? "Sí" : "No"
            });
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        idSeleccionado = (int) modelo.getValueAt(fila, 0);
        Paciente p = dao.buscarPorId(idSeleccionado);
        if (p == null) return;
        txtNombre.setText(p.getNombre());
        txtCedula.setText(p.getCedula());
        txtNacimiento.setText(p.getFechaNacimiento() != null ? p.getFechaNacimiento().format(FMT) : "");
        txtTelefono.setText(p.getTelefono());
        txtEmail.setText(p.getEmail());
        txtDireccion.setText(p.getDireccion());
        txtAlergias.setText(p.getAlergias());
        fotoSeleccionadaPath = p.getFotoUrl();
        if (fotoSeleccionadaPath != null && !fotoSeleccionadaPath.isEmpty()) {
            mostrarFoto(Paths.get(fotoSeleccionadaPath));
        } else {
            mostrarFotoPorDefecto();
        }
    }

    // ================= VALIDACIÓN Y GUARDADO =================
    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String cedula = txtCedula.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();

        if (nombre.isEmpty() || cedula.isEmpty()) {
            mostrarValidacion("Nombre y cédula son obligatorios.");
            return;
        }
        if (dao.existeCedula(cedula, idSeleccionado)) {
            mostrarValidacion("Ya existe otro paciente registrado con esa cédula.");
            return;
        }
        if (!telefono.isEmpty() && !PATRON_TELEFONO.matcher(telefono).matches()) {
            mostrarValidacion("El teléfono no tiene un formato válido.");
            return;
        }
        if (!email.isEmpty() && !PATRON_EMAIL.matcher(email).matches()) {
            mostrarValidacion("El email no tiene un formato válido.");
            return;
        }

        Paciente p = new Paciente();
        p.setId(idSeleccionado);
        p.setNombre(nombre);
        p.setCedula(cedula);
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setDireccion(txtDireccion.getText().trim());
        p.setAlergias(txtAlergias.getText().trim());
        p.setFotoUrl(fotoSeleccionadaPath);
        p.setActivo(true);

        if (!txtNacimiento.getText().trim().isEmpty()) {
            try {
                LocalDate fecha = LocalDate.parse(txtNacimiento.getText().trim(), FMT);
                if (fecha.isAfter(LocalDate.now())) {
                    mostrarValidacion("La fecha de nacimiento no puede ser futura.");
                    return;
                }
                p.setFechaNacimiento(fecha);
            } catch (Exception ex) {
                mostrarValidacion("Formato de fecha inválido. Usa aaaa-mm-dd.");
                return;
            }
        }

        if (idSeleccionado == -1) {
            dao.crear(p);
        } else {
            dao.actualizar(p);
        }
        limpiarFormulario();
        cargarTabla();
    }

    private void mostrarValidacion(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    private void eliminar() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un paciente de la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Desactivar este paciente?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        dao.eliminar(idSeleccionado);
        limpiarFormulario();
        cargarTabla();
    }

    private void limpiarFormulario() {
        idSeleccionado = -1;
        txtNombre.setText("");
        txtCedula.setText("");
        txtNacimiento.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtDireccion.setText("");
        txtAlergias.setText("");
        fotoSeleccionadaPath = null;
        mostrarFotoPorDefecto();
        tabla.clearSelection();
    }
}
