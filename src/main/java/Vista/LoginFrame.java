package Vista;
import Modelo.UsuarioDAO;
import Modelo.Usuario;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
/**
* Pantalla de inicio de sesión. Diseño alineado al resto del sistema
* (misma paleta y tipografía de EstiloUI) en vez de un formulario genérico.
*/
public class LoginFrame extends JFrame {
private final JTextField txtUsuario = new JTextField(18);
private final JPasswordField txtPassword = new JPasswordField(18);
private final JCheckBox chkMostrar = new JCheckBox("Mostrar contraseña");
private final JLabel lblMensaje = new JLabel(" ");
private final UsuarioDAO usuarioDAO = new UsuarioDAO();
public LoginFrame() {
setTitle("Clínica Dental - Iniciar sesión");
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setSize(980, 620);
setMinimumSize(new Dimension(760, 560));
setLocationRelativeTo(null);
JPanel raiz = new JPanel(new BorderLayout());
raiz.setBackground(EstiloUI.COLOR_PANEL);
raiz.add(construirPanelMarca(), BorderLayout.WEST);
raiz.add(construirPanelFormulario(), BorderLayout.CENTER);
setContentPane(raiz);
}
// ---------- Panel izquierdo: marca / branding ----------
private JComponent construirPanelMarca() {
JPanel panel = new JPanel(new GridBagLayout()) {
@Override
protected void paintComponent(Graphics g) {
super.paintComponent(g);
Graphics2D g2 = (Graphics2D) g.create();
g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
// Degradado verde
GradientPaint gp = new GradientPaint(
0, 0, EstiloUI.COLOR_PRIMARIO_OSC,
0, getHeight(), EstiloUI.COLOR_PRIMARIO
);
g2.setPaint(gp);
g2.fillRect(0, 0, getWidth(), getHeight());
g2.dispose();
}
};
panel.setPreferredSize(new Dimension(380, 0));
panel.setOpaque(true);
JPanel contenido = new JPanel();
contenido.setOpaque(false);
contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
EstiloUI.IconoMonograma icono = new EstiloUI.IconoMonograma("CD", EstiloUI.COLOR_ACENTO, 56);
icono.setAlignmentX(Component.LEFT_ALIGNMENT);
JLabel titulo = new JLabel("Clínica Dental");
titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
titulo.setForeground(Color.WHITE);
titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
titulo.setBorder(new EmptyBorder(18, 0, 4, 0));
JLabel subtitulo = new JLabel("<html><body style='width:220px'>Sistema integral de gestión clínica: "
+ "pacientes, citas, facturación e inventario.</body></html>");
subtitulo.setFont(EstiloUI.F_SUBTITULO);
subtitulo.setForeground(EstiloUI.COLOR_TEXTO_CLARO);
subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
contenido.add(icono);
contenido.add(titulo);
contenido.add(subtitulo);
GridBagConstraints gbc = new GridBagConstraints();
panel.add(contenido, gbc);
panel.setBorder(new EmptyBorder(0, 46, 0, 46));
return panel;
}
// ---------- Panel derecho: formulario ----------
private JComponent construirPanelFormulario() {
JPanel envoltorio = new JPanel(new GridBagLayout());
// Fondo verde (mismo tono del lado izquierdo)
envoltorio.setBackground(new Color(0x1B, 0x5E, 0x4A));
EstiloUI.TarjetaRedondeada tarjeta = new EstiloUI.TarjetaRedondeada();
tarjeta.setLayout(new BorderLayout());
tarjeta.setBorder(new EmptyBorder(38, 38, 34, 38));
JPanel form = new JPanel();
form.setOpaque(false);
form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
form.setMaximumSize(new Dimension(340, Integer.MAX_VALUE));
JLabel titulo = new JLabel("Iniciar sesión");
titulo.setFont(EstiloUI.F_TITULO);
titulo.setForeground(EstiloUI.COLOR_TEXTO);
titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
JLabel subtitulo = new JLabel("Ingresa tus credenciales de acceso");
subtitulo.setFont(EstiloUI.F_SUBTITULO);
subtitulo.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);
subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
subtitulo.setBorder(new EmptyBorder(4, 0, 26, 0));
JLabel lblUsuario = EstiloUI.crearEtiqueta("Usuario");
lblUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
EstiloUI.estilizarCampo(txtUsuario);
txtUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
txtUsuario.setPreferredSize(new Dimension(txtUsuario.getPreferredSize().width, 44));
txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
JLabel lblPassword = EstiloUI.crearEtiqueta("Contraseña");
lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
lblPassword.setBorder(new EmptyBorder(14, 0, 0, 0));
EstiloUI.estilizarCampo(txtPassword);
txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
txtPassword.setPreferredSize(new Dimension(txtPassword.getPreferredSize().width, 44));
txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
txtPassword.setEchoChar('•');
chkMostrar.setOpaque(false);
chkMostrar.setFont(EstiloUI.F_SUBTITULO);
chkMostrar.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);
chkMostrar.setAlignmentX(Component.LEFT_ALIGNMENT);
chkMostrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
chkMostrar.setBorder(new EmptyBorder(6, 0, 0, 0));
chkMostrar.addActionListener(e -> txtPassword.setEchoChar(chkMostrar.isSelected() ? (char) 0 : '•'));
lblMensaje.setFont(EstiloUI.F_LABEL);
lblMensaje.setForeground(EstiloUI.COLOR_PELIGRO);
lblMensaje.setAlignmentX(Component.LEFT_ALIGNMENT);
lblMensaje.setBorder(new EmptyBorder(14, 0, 0, 0));
JButton btnLogin = EstiloUI.botonSolido("Ingresar", EstiloUI.COLOR_PRIMARIO, EstiloUI.COLOR_PRIMARIO_OSC, Color.WHITE);
btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
btnLogin.setPreferredSize(new Dimension(340, 46));
btnLogin.setMinimumSize(new Dimension(100, 46));
btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
btnLogin.setFont(EstiloUI.F_BOTON.deriveFont(14f));
btnLogin.setBorder(new EmptyBorder(0, 0, 0, 0));
btnLogin.addActionListener(e -> intentarLogin());
JLabel ayuda = new JLabel("Usuario de prueba: admin / admin123", SwingConstants.CENTER);
ayuda.setFont(EstiloUI.F_SUBTITULO);
ayuda.setForeground(EstiloUI.COLOR_PRIMARIO);
ayuda.setAlignmentX(Component.LEFT_ALIGNMENT);
ayuda.setOpaque(true);
ayuda.setBackground(EstiloUI.COLOR_PRIMARIO_SUAVE);
ayuda.setBorder(new CompoundBorder(
new EstiloUI.BordeRedondeado(EstiloUI.COLOR_PRIMARIO_SUAVE, 8),
new EmptyBorder(8, 10, 8, 10)));
ayuda.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
ayuda.setAlignmentX(Component.LEFT_ALIGNMENT);
form.add(titulo);
form.add(subtitulo);
form.add(lblUsuario);
form.add(Box.createVerticalStrut(6));
form.add(txtUsuario);
form.add(lblPassword);
form.add(Box.createVerticalStrut(6));
form.add(txtPassword);
form.add(chkMostrar);
form.add(lblMensaje);
form.add(Box.createVerticalStrut(24));
form.add(btnLogin);
form.add(Box.createVerticalStrut(20));
form.add(ayuda);
KeyAdapter enterListener = new KeyAdapter() {
@Override
public void keyPressed(KeyEvent e) {
if (e.getKeyCode() == KeyEvent.VK_ENTER) intentarLogin();
}
};
txtUsuario.addKeyListener(enterListener);
txtPassword.addKeyListener(enterListener);
tarjeta.add(form, BorderLayout.CENTER);
envoltorio.add(tarjeta, new GridBagConstraints());
return envoltorio;
}
private void intentarLogin() {
String usuario = txtUsuario.getText().trim();
String password = new String(txtPassword.getPassword());
lblMensaje.setText(" ");
if (usuario.isEmpty() || password.isEmpty()) {
lblMensaje.setText("Ingresa usuario y contraseña.");
return;
}
try {
Usuario u = usuarioDAO.autenticar(usuario, password);
if (u == null) {
lblMensaje.setText("Usuario o contraseña incorrectos.");
return;
}
usuarioDAO.registrarUltimoLogin(u.getId());
dispose();
SwingUtilities.invokeLater(() -> new MainFrame(u).setVisible(true));
} catch (SQLException ex) {
JOptionPane.showMessageDialog(this,
"No se pudo conectar a la base de datos.\n"
+ "Verifica que el servidor MySQL esté encendido y que la base 'clinica_dental' exista.\n\n"
+ "Detalle: " + ex.getMessage(),
"Error de conexión", JOptionPane.ERROR_MESSAGE);
}
}
}
