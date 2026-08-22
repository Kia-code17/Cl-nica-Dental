package Vista;

import Modelo.Rol;
import Modelo.RolDAO;
import Modelo.Usuario;
import Modelo.UsuarioDAO;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Módulo de administración de cuentas: alta, edición, cambio de contraseña,
 * activar/desactivar y asignación de rol (Admin, Doctor, Recepcionista,
 * Asistente). Solo debería quedar visible en el menú para usuarios con
 * rol Admin (ver MainFrame).
 */
public class UsuariosPanel extends JPanel {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();
    private final Usuario usuarioSesion; // quien está usando la app ahora mismo

    private final DefaultTableModel modelo = new DefaultTableModel(
        new Object[]{"ID", "Nombre completo", "Usuario", "Rol", "Activo", "Último acceso"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabla = new JTable(modelo);

    private final JTextField txtNombre = new JTextField();
    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JPasswordField txtConfirmar = new JPasswordField();
    private final JCheckBox chkMostrarPassword = new JCheckBox("Mostrar contraseña");
    private final JComboBox<Rol> cbRol = new JComboBox<>();
    private final JCheckBox chkActivo = new JCheckBox("Cuenta activa");
    private final JLabel lblAyudaRol = new JLabel(" ");
    private final JLabel lblFormTitulo = new JLabel("Registrar nuevo usuario");

    private int idSeleccionado = -1;

    public UsuariosPanel(Usuario usuarioSesion) {
        this.usuarioSesion = usuarioSesion;

        setLayout(new BorderLayout());
        setBackground(EstiloUI.COLOR_FONDO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(construirEncabezado(), BorderLayout.NORTH);
        norte.add(construirLeyendaRoles(), BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            construirPanelTabla(),
            construirPanelFormulario()
        );
        split.setBorder(null);
        split.setOpaque(false);
        split.setDividerSize(10);
        split.setResizeWeight(1.0);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        chkMostrarPassword.setOpaque(false);
        chkMostrarPassword.setFont(EstiloUI.F_CAMPO);
        chkMostrarPassword.addActionListener(e -> alternarVisibilidadPassword());

        cargarRoles();
        cargarTabla();
        limpiar();
    }

    // ================= ENCABEZADO =================

    private JComponent construirEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel titulo = new JLabel("Usuarios y roles");
        titulo.setFont(EstiloUI.F_TITULO);
        titulo.setForeground(EstiloUI.COLOR_TEXTO);

        JLabel subtitulo = new JLabel("Registra cuentas de acceso al sistema y asigna su nivel de permisos");
        subtitulo.setFont(EstiloUI.F_SUBTITULO);
        subtitulo.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(subtitulo);

        panel.add(textos, BorderLayout.WEST);
        return panel;
    }

    /** Tarjeta con los 4 roles y su descripción, para que quede claro qué otorga cada uno. */
    private JComponent construirLeyendaRoles() {
        EstiloUI.TarjetaRedondeada tarjeta = new EstiloUI.TarjetaRedondeada();
        tarjeta.setLayout(new GridLayout(1, 4, 12, 0));
        tarjeta.setBorder(new EmptyBorder(14, 16, 14, 16));

        List<Rol> roles;
        try {
            roles = rolDAO.listarTodos();
        } catch (SQLException ex) {
            mostrarError(ex);
            return tarjeta;
        }
        for (Rol r : roles) {
            JPanel col = new JPanel();
            col.setOpaque(false);
            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

            JLabel nombre = new JLabel(r.getNombre());
            nombre.setFont(EstiloUI.F_LABEL);
            nombre.setForeground(EstiloUI.COLOR_PRIMARIO);
            nombre.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel desc = new JLabel("<html><body style='width:150px'>" + r.getDescripcion() + "</body></html>");
            desc.setFont(EstiloUI.F_SUBTITULO);
            desc.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            desc.setBorder(new EmptyBorder(3, 0, 0, 0));

            col.add(nombre);
            col.add(desc);
            tarjeta.add(col);
        }
        return tarjeta;
    }

    // ================= TABLA =================

    private JComponent construirPanelTabla() {
        tabla.setFont(EstiloUI.F_TABLA);
        tabla.setRowHeight(32);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionBackground(new Color(0xDD, 0xEE, 0xF3));
        tabla.setSelectionForeground(EstiloUI.COLOR_TEXTO);
        tabla.setFillsViewportHeight(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        EstiloUI.estilizarEncabezado(tabla.getTableHeader());

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (!sel) c.setBackground(row % 2 == 0 ? EstiloUI.COLOR_PANEL : EstiloUI.COLOR_FILA_ALT);
                if (col == 4) { // columna Activo
                    setHorizontalAlignment(SwingConstants.CENTER);
                    c.setForeground("Sí".equals(v) ? EstiloUI.COLOR_EXITO : EstiloUI.COLOR_PELIGRO);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    if (!sel) c.setForeground(EstiloUI.COLOR_TEXTO);
                }
                return c;
            }
        };
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        // Anchos fijos para que ningún campo se corte o quede amontonado.
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        tabla.getColumnModel().getColumn(1).setPreferredWidth(190); // Nombre completo
        tabla.getColumnModel().getColumn(2).setPreferredWidth(130); // Usuario
        tabla.getColumnModel().getColumn(3).setPreferredWidth(130); // Rol
        tabla.getColumnModel().getColumn(4).setPreferredWidth(60);  // Activo
        tabla.getColumnModel().getColumn(5).setPreferredWidth(150); // Último acceso

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(EstiloUI.COLOR_BORDE, 1, true));
        scroll.getViewport().setBackground(EstiloUI.COLOR_PANEL);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.setBorder(new EmptyBorder(0, 0, 0, 10));
        contenedor.add(scroll, BorderLayout.CENTER);
        return contenedor;
    }

    // ================= FORMULARIO =================

    private JComponent construirPanelFormulario() {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 12));
        tarjeta.setBackground(EstiloUI.COLOR_PANEL);
        tarjeta.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(EstiloUI.COLOR_BORDE, 1, true),
            new EmptyBorder(18, 18, 18, 18)));
        tarjeta.setMinimumSize(new Dimension(320, 0));
        tarjeta.setPreferredSize(new Dimension(360, 0));

        lblFormTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFormTitulo.setForeground(EstiloUI.COLOR_TEXTO);

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;

        estilizarCampo(txtNombre);
        estilizarCampo(txtUsuario);
        estilizarCampo(txtPassword);
        estilizarCampo(txtConfirmar);
        estilizarCampo(cbRol);

        int row = 0;
        row = campo(campos, gbc, row, "Nombre completo", txtNombre);
        row = campo(campos, gbc, row, "Nombre de usuario", txtUsuario);
        row = campo(campos, gbc, row, "Contraseña", txtPassword);
        row = campo(campos, gbc, row, "Confirmar contraseña", txtConfirmar);

        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 14, 0);
        campos.add(chkMostrarPassword, gbc);

        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 4, 0);
        campos.add(etiqueta("Rol"), gbc);
        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 2, 0);
        campos.add(cbRol, gbc);
        lblAyudaRol.setFont(EstiloUI.F_SUBTITULO);
        lblAyudaRol.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);
        cbRol.addActionListener(e -> actualizarAyudaRol());
        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 14, 0);
        campos.add(lblAyudaRol, gbc);

        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 16, 0);
        chkActivo.setOpaque(false);
        chkActivo.setFont(EstiloUI.F_CAMPO);
        chkActivo.setForeground(EstiloUI.COLOR_TEXTO);
        campos.add(chkActivo, gbc);

        JPanel botones = new JPanel(new GridLayout(2, 1, 0, 8));
        botones.setOpaque(false);
        JButton btnGuardar = EstiloUI.botonSolido("Guardar usuario", EstiloUI.COLOR_PRIMARIO, EstiloUI.COLOR_PRIMARIO_OSC, Color.WHITE);
        JPanel filaSecundaria = new JPanel(new GridLayout(1, 2, 8, 0));
        filaSecundaria.setOpaque(false);
        JButton btnNuevo = EstiloUI.botonContorno("Nuevo", EstiloUI.COLOR_PRIMARIO);
        JButton btnEstado = EstiloUI.botonContorno("Desactivar", EstiloUI.COLOR_PELIGRO);
        filaSecundaria.add(btnNuevo);
        filaSecundaria.add(btnEstado);
        botones.add(btnGuardar);
        botones.add(filaSecundaria);

        btnGuardar.addActionListener(e -> guardar());
        btnNuevo.addActionListener(e -> limpiar());
        btnEstado.addActionListener(e -> alternarEstado(btnEstado));

        JScrollPane scrollCampos = new JScrollPane(campos);
        scrollCampos.setBorder(null);
        scrollCampos.setOpaque(false);
        scrollCampos.getViewport().setOpaque(false);
        scrollCampos.getVerticalScrollBar().setUnitIncrement(14);

        tarjeta.add(lblFormTitulo, BorderLayout.NORTH);
        tarjeta.add(scrollCampos, BorderLayout.CENTER);
        tarjeta.add(botones, BorderLayout.SOUTH);
        return tarjeta;
    }

    private int campo(JPanel panel, GridBagConstraints gbc, int row, String etiqueta, JComponent campo) {
        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(etiqueta(etiqueta), gbc);
        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 12, 0);
        panel.add(campo, gbc);
        return row;
    }

    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(EstiloUI.F_LABEL);
        lbl.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);
        return lbl;
    }

    private void estilizarCampo(JComponent campo) {
        campo.setFont(EstiloUI.F_CAMPO);
        Border borde = new CompoundBorder(
            BorderFactory.createLineBorder(EstiloUI.COLOR_BORDE, 1, true),
            new EmptyBorder(6, 8, 6, 8));
        if (campo instanceof JTextField) {
            ((JTextField) campo).setBorder(borde);
        } else if (campo instanceof JComboBox) {
            campo.setBorder(new CompoundBorder(BorderFactory.createLineBorder(EstiloUI.COLOR_BORDE, 1, true), new EmptyBorder(2, 4, 2, 4)));
        }
        campo.setPreferredSize(new Dimension(campo.getPreferredSize().width, 34));
    }

    private void alternarVisibilidadPassword() {
        char eco = chkMostrarPassword.isSelected() ? (char) 0 : '•';
        txtPassword.setEchoChar(eco);
        txtConfirmar.setEchoChar(eco);
    }

    private void actualizarAyudaRol() {
        Rol r = (Rol) cbRol.getSelectedItem();
        lblAyudaRol.setText(r != null ? r.getDescripcion() : " ");
    }

    // ================= LÓGICA =================

    private void cargarRoles() {
        try {
            cbRol.removeAllItems();
            for (Rol r : rolDAO.listarTodos()) cbRol.addItem(r);
            actualizarAyudaRol();
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);
            for (Usuario u : usuarioDAO.listarTodos()) {
                modelo.addRow(new Object[]{
                    u.getId(), u.getNombreCompleto(), u.getUsuario(), u.getRolNombre(),
                    u.isActivo() ? "Sí" : "No",
                    u.getUltimoLogin() != null ? u.getUltimoLogin() : "Nunca"
                });
            }
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        idSeleccionado = (int) modelo.getValueAt(fila, 0);
        try {
            Usuario u = usuarioDAO.buscarPorId(idSeleccionado);
            if (u == null) return;
            txtNombre.setText(u.getNombreCompleto());
            txtUsuario.setText(u.getUsuario());
            txtPassword.setText("");
            txtConfirmar.setText("");
            chkActivo.setSelected(u.isActivo());
            for (int i = 0; i < cbRol.getItemCount(); i++) {
                if (cbRol.getItemAt(i).getId() == u.getRolId()) { cbRol.setSelectedIndex(i); break; }
            }
            lblFormTitulo.setText("Editando: " + u.getNombreCompleto());
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String pass = new String(txtPassword.getPassword());
        String confirmar = new String(txtConfirmar.getPassword());
        Rol rol = (Rol) cbRol.getSelectedItem();

        if (nombre.isEmpty() || usuario.isEmpty() || rol == null) {
            JOptionPane.showMessageDialog(this, "Nombre, usuario y rol son obligatorios.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (idSeleccionado == -1 && pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Define una contraseña para la nueva cuenta.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!pass.isEmpty() && !pass.equals(confirmar)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (usuarioDAO.existeUsuario(usuario, idSeleccionado == -1 ? null : idSeleccionado)) {
                JOptionPane.showMessageDialog(this, "Ese nombre de usuario ya está en uso.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Usuario u = new Usuario();
            u.setId(idSeleccionado);
            u.setNombreCompleto(nombre);
            u.setUsuario(usuario);
            u.setRolId(rol.getId());
            u.setActivo(chkActivo.isSelected());

            if (idSeleccionado == -1) {
                usuarioDAO.crear(u, pass);
            } else {
                usuarioDAO.actualizar(u);
                if (!pass.isEmpty()) usuarioDAO.actualizarPassword(idSeleccionado, pass);
            }
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void alternarEstado(JButton boton) {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (idSeleccionado == usuarioSesion.getId()) {
            JOptionPane.showMessageDialog(this, "No puedes desactivar tu propia cuenta mientras la usas.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            boolean nuevoEstado = !chkActivo.isSelected();
            usuarioDAO.cambiarEstado(idSeleccionado, nuevoEstado);
            chkActivo.setSelected(nuevoEstado);
            cargarTabla();
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void limpiar() {
        idSeleccionado = -1;
        txtNombre.setText("");
        txtUsuario.setText("");
        txtPassword.setText("");
        txtConfirmar.setText("");
        chkActivo.setSelected(true);
        if (cbRol.getItemCount() > 0) cbRol.setSelectedIndex(0);
        lblFormTitulo.setText("Registrar nuevo usuario");
        tabla.clearSelection();
    }

    private void mostrarError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}