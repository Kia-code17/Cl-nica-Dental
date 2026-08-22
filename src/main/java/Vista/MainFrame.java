package Vista;
import Modelo.Usuario;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
/**
* Ventana principal: menú lateral de navegación + panel central con CardLayout.
* Cada módulo del sistema es un JPanel independiente añadido como "carta".
*/
public class MainFrame extends JFrame {
private final CardLayout cardLayout = new CardLayout();
private final JPanel panelContenido = new JPanel(cardLayout);
private final Usuario usuarioActual;
private final boolean esAdmin;
private final Map<String, JButton> botonesMenu = new LinkedHashMap<>();
private String cardActiva = "dashboard";
public MainFrame(Usuario usuario) {
this.usuarioActual = usuario;
this.esAdmin = calcularEsAdmin(usuario);
setTitle("Clínica Dental - Panel Principal");
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setSize(1280, 800);
setMinimumSize(new Dimension(1024, 640));
setLocationRelativeTo(null);
setLayout(new BorderLayout());
getContentPane().setBackground(EstiloUI.COLOR_FONDO);
add(construirBarraSuperior(), BorderLayout.NORTH);
add(construirMenuLateral(), BorderLayout.WEST);
panelContenido.setBackground(EstiloUI.COLOR_FONDO);
add(panelContenido, BorderLayout.CENTER);
registrarModulos();
mostrarCarta("dashboard");
}
private static boolean calcularEsAdmin(Usuario usuario) {
if (usuario == null) return false;
String rol = usuario.getRolNombre();
if (rol == null) return false;
String normalizado = rol.trim().toLowerCase();
return normalizado.equals("admin") || normalizado.equals("administrador")
|| normalizado.contains("admin");
}
// ---------- Barra superior ----------
private JPanel construirBarraSuperior() {
JPanel top = new JPanel(new BorderLayout());
top.setBackground(EstiloUI.COLOR_PRIMARIO_OSC);
top.setBorder(new EmptyBorder(14, 24, 14, 24));
JLabel titulo = new JLabel("Clínica Dental");
titulo.setForeground(Color.WHITE);
titulo.setFont(EstiloUI.F_MARCA);
JLabel subtitulo = new JLabel(" · Sistema de gestión");
subtitulo.setForeground(EstiloUI.COLOR_TEXTO_CLARO);
subtitulo.setFont(EstiloUI.F_SUBTITULO);
JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
marca.setOpaque(false);
marca.add(titulo);
marca.add(subtitulo);
JPanel usuarioBox = new JPanel();
usuarioBox.setOpaque(false);
usuarioBox.setLayout(new BoxLayout(usuarioBox, BoxLayout.Y_AXIS));
JLabel nombreLbl = new JLabel(usuarioActual.getNombreCompleto());
nombreLbl.setForeground(Color.WHITE);
nombreLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
nombreLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
JLabel rolLbl = new JLabel(usuarioActual.getRolNombre());
rolLbl.setForeground(EstiloUI.COLOR_TEXTO_CLARO);
rolLbl.setFont(EstiloUI.F_SUBTITULO);
rolLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
usuarioBox.add(nombreLbl);
usuarioBox.add(rolLbl);
JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
derecha.setOpaque(false);
derecha.add(usuarioBox);
derecha.add(new EstiloUI.IconoMonograma(inicialesDe(usuarioActual.getNombreCompleto()), EstiloUI.COLOR_ACENTO, 38));
top.add(marca, BorderLayout.WEST);
top.add(derecha, BorderLayout.EAST);
return top;
}
private static String inicialesDe(String nombreCompleto) {
if (nombreCompleto == null || nombreCompleto.isBlank()) return "US";
String[] partes = nombreCompleto.trim().split("\\s+");
StringBuilder sb = new StringBuilder();
for (int i = 0; i < Math.min(2, partes.length); i++) {
if (!partes[i].isEmpty()) sb.append(Character.toUpperCase(partes[i].charAt(0)));
}
return sb.length() > 0 ? sb.toString() : "US";
}
// ---------- Menú lateral ----------
private JScrollPane construirMenuLateral() {
JPanel menu = new JPanel();
menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
menu.setBorder(new CompoundBorder(
new MatteBorder(0, 0, 0, 1, EstiloUI.COLOR_PRIMARIO_OSC),
new EmptyBorder(16, 12, 16, 12)));
menu.setBackground(EstiloUI.COLOR_PRIMARIO);
agregarSeccion(menu, "GENERAL");
agregarBoton(menu, "GE", "Dashboard", "dashboard", new Color(0x1B, 0x5E, 0x4A)); // Verde oscuro
agregarSeccion(menu, "PACIENTES");
agregarBoton(menu, "PA", "Pacientes", "pacientes", new Color(0x1E, 0x88, 0xE5)); // Azul
agregarBoton(menu, "OD", "Odontograma", "odontograma", new Color(0x8E, 0x24, 0xAA)); // Morado
agregarSeccion(menu, "OPERACIÓN");
agregarBoton(menu, "DR", "Doctores", "doctores", new Color(0xFB, 0x8C, 0x00)); // Naranja
agregarBoton(menu, "CI", "Citas", "citas", new Color(0xE5, 0x39, 0x35)); // Rojo
agregarBoton(menu, "FA", "Facturación", "facturacion", new Color(0x00, 0x89, 0x7B)); // Verde azulado
agregarBoton(menu, "IN", "Inventario", "inventario", new Color(0x39, 0x49, 0xAB)); // Índigo
if (esAdmin) {
agregarSeccion(menu, "ADMINISTRACIÓN");
agregarBoton(menu, "US", "Usuarios y roles", "usuarios", new Color(0xD8, 0x1B, 0x60)); // Rosa
}
agregarSeccion(menu, "ANÁLISIS");
agregarBoton(menu, "RE", "Reportes", "reportes", new Color(0x43, 0xA0, 0x47)); // Verde
menu.add(Box.createVerticalGlue());
IlustracionMenuLateral ilustracion = new IlustracionMenuLateral();
ilustracion.setAlignmentX(Component.LEFT_ALIGNMENT);
menu.add(ilustracion);
menu.add(Box.createVerticalStrut(10));
JButton salir = EstiloUI.botonMenu("Cerrar sesión", EstiloUI.COLOR_PELIGRO, EstiloUI.COLOR_PELIGRO_OSC, EstiloUI.COLOR_PELIGRO);
salir.setForeground(Color.WHITE);
salir.setAlignmentX(Component.LEFT_ALIGNMENT);
salir.addActionListener(e -> {
dispose();
SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
});
menu.add(salir);
JPanel contenedor = new JPanel(new BorderLayout());
contenedor.setBackground(EstiloUI.COLOR_PRIMARIO);
contenedor.add(menu, BorderLayout.NORTH);
contenedor.add(Box.createVerticalGlue(), BorderLayout.CENTER);
JScrollPane scroll = new JScrollPane(contenedor);
scroll.setBorder(null);
scroll.setPreferredSize(new Dimension(232, 0));
scroll.getVerticalScrollBar().setUnitIncrement(16);
scroll.getViewport().setBackground(EstiloUI.COLOR_PRIMARIO);
return scroll;
}
private void agregarSeccion(JPanel menu, String texto) {
JLabel lbl = new JLabel(texto);
lbl.setFont(EstiloUI.F_MENU_SEC);
lbl.setForeground(EstiloUI.COLOR_TEXTO_CLARO);
lbl.setBorder(new EmptyBorder(16, 10, 6, 0));
lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
menu.add(lbl);
}
private void agregarBoton(JPanel menu, String siglas, String texto, String cardName, Color colorIcono) {
JButton btn = EstiloUI.botonMenu(texto, null,
EstiloUI.COLOR_ACENTO, EstiloUI.COLOR_ACENTO_OSC);
btn.setIcon(new IconoBotonMenu(siglas, colorIcono));
btn.setIconTextGap(10);
btn.addActionListener(e -> mostrarCarta(cardName));
botonesMenu.put(cardName, btn);
menu.add(btn);
menu.add(Box.createVerticalStrut(3));
}
/** Ícono pequeño (monograma) con color personalizado. */
private static class IconoBotonMenu implements Icon {
private final String letras;
private final Color colorFondo;
IconoBotonMenu(String letras, Color colorFondo) {
this.letras = letras;
this.colorFondo = colorFondo;
}
@Override
public int getIconWidth() {
return 26;
}
@Override
public int getIconHeight() {
return 26;
}
@Override
public void paintIcon(Component c, Graphics g, int x, int y) {
Graphics2D g2 = (Graphics2D) g.create();
g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
// Fondo con el color del módulo
g2.setColor(colorFondo);
g2.fillRoundRect(x, y, 26, 26, 8, 8);
// Texto blanco
g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
FontMetrics fm = g2.getFontMetrics();
int tx = x + (26 - fm.stringWidth(letras)) / 2;
int ty = y + (26 - fm.getHeight()) / 2 + fm.getAscent();
g2.setColor(Color.WHITE);
g2.drawString(letras, tx, ty);
g2.dispose();
}
}
private static class IlustracionMenuLateral extends JComponent {
IlustracionMenuLateral() {
setOpaque(false);
setPreferredSize(new Dimension(200, 132));
setMaximumSize(new Dimension(220, 132));
}
@Override
protected void paintComponent(Graphics g) {
Graphics2D g2 = (Graphics2D) g.create();
g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
int w = getWidth();
g2.setColor(new Color(255, 255, 255, 18));
g2.fillOval(w - 46, 4, 40, 40);
g2.setColor(new Color(255, 255, 255, 14));
g2.fillOval(6, 60, 24, 24);
float bx = (w - 66) / 2f, by = 6, s = 0.62f;
java.awt.geom.Path2D.Double diente = new java.awt.geom.Path2D.Double();
diente.moveTo(bx + 20 * s, by + 8 * s);
diente.curveTo(bx + 4 * s, by + 8 * s, bx, by + 30 * s, bx, by + 52 * s);
diente.curveTo(bx, by + 74 * s, bx + 10 * s, by + 90 * s, bx + 15 * s, by + 116 * s);
diente.curveTo(bx + 18 * s, by + 130 * s, bx + 27 * s, by + 130 * s, bx + 30 * s, by + 114 * s);
diente.curveTo(bx + 32 * s, by + 98 * s, bx + 34 * s, by + 92 * s, bx + 43 * s, by + 92 * s);
diente.curveTo(bx + 52 * s, by + 92 * s, bx + 54 * s, by + 98 * s, bx + 56 * s, by + 114 * s);
diente.curveTo(bx + 59 * s, by + 130 * s, bx + 67 * s, by + 130 * s, bx + 70 * s, by + 116 * s);
diente.curveTo(bx + 76 * s, by + 90 * s, bx + 86 * s, by + 74 * s, bx + 86 * s, by + 52 * s);
diente.curveTo(bx + 86 * s, by + 30 * s, bx + 80 * s, by + 8 * s, bx + 64 * s, by + 8 * s);
diente.curveTo(bx + 54 * s, by + 8 * s, bx + 49 * s, by + 17 * s, bx + 43 * s, by + 17 * s);
diente.curveTo(bx + 37 * s, by + 17 * s, bx + 32 * s, by + 8 * s, bx + 20 * s, by + 8 * s);
diente.closePath();
g2.setColor(new Color(255, 255, 255, 230));
g2.fill(diente);
g2.setColor(EstiloUI.COLOR_ACENTO);
g2.setStroke(new BasicStroke(1.8f));
g2.draw(diente);
String texto = "Cuidando tu sonrisa";
g2.setFont(EstiloUI.F_SUBTITULO.deriveFont(11f));
g2.setColor(EstiloUI.COLOR_TEXTO_CLARO);
FontMetrics fm = g2.getFontMetrics();
int tx = (w - fm.stringWidth(texto)) / 2;
g2.drawString(texto, Math.max(0, tx), 118);
g2.dispose();
}
}
private void mostrarCarta(String cardName) {
cardActiva = cardName;
cardLayout.show(panelContenido, cardName);
for (Map.Entry<String, JButton> entry : botonesMenu.entrySet()) {
entry.getValue().putClientProperty("activo", entry.getKey().equals(cardActiva));
entry.getValue().repaint();
}
}
// ---------- Registro de módulos ----------
private void registrarModulos() {
panelContenido.add(construirDashboard(), "dashboard");
panelContenido.add(new PacientesPanel(), "pacientes");
panelContenido.add(new OdontogramaPanel(), "odontograma");
panelContenido.add(new DoctoresPanel(), "doctores");
panelContenido.add(new CitasPanel(), "citas");
panelContenido.add(new FacturacionPanel(), "facturacion");
panelContenido.add(new InventarioPanel(), "inventario");
if (esAdmin) {
panelContenido.add(new UsuariosPanel(usuarioActual), "usuarios");
}
panelContenido.add(new ReportesPanel(), "reportes");
}
// ---------- Dashboard ----------
private JComponent construirDashboard() {
JPanel panel = new JPanel(new BorderLayout());
panel.setBackground(EstiloUI.COLOR_FONDO);
panel.setBorder(new EmptyBorder(30, 30, 30, 30));
JLabel bienvenida = new JLabel("Bienvenido(a), " + usuarioActual.getNombreCompleto());
bienvenida.setFont(new Font("Segoe UI", Font.BOLD, 25));
bienvenida.setForeground(EstiloUI.COLOR_TEXTO);
JLabel subtitulo = new JLabel("Selecciona un módulo del menú lateral o accede directamente desde aquí");
subtitulo.setFont(EstiloUI.F_SUBTITULO);
subtitulo.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);
subtitulo.setBorder(new EmptyBorder(6, 0, 26, 0));
JPanel encabezado = new JPanel();
encabezado.setOpaque(false);
encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
bienvenida.setAlignmentX(Component.LEFT_ALIGNMENT);
subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
encabezado.add(bienvenida);
encabezado.add(subtitulo);
JPanel rejilla = new JPanel(new GridLayout(0, 3, 16, 16));
rejilla.setOpaque(false);
// Colores distintos para cada módulo (iguales que el menú)
rejilla.add(tarjetaModulo("PA", new Color(0x1E, 0x88, 0xE5), "Pacientes", "Registro, historial y búsqueda", "pacientes")); // Azul
rejilla.add(tarjetaModulo("OD", new Color(0x8E, 0x24, 0xAA), "Odontograma", "Estado dental por paciente", "odontograma")); // Morado
rejilla.add(tarjetaModulo("DR", new Color(0xFB, 0x8C, 0x00), "Doctores", "Gestión de especialistas", "doctores")); // Naranja
rejilla.add(tarjetaModulo("CI", new Color(0xE5, 0x39, 0x35), "Citas", "Agenda y disponibilidad", "citas")); // Rojo
rejilla.add(tarjetaModulo("FA", new Color(0x00, 0x89, 0x7B), "Facturación", "Cobros y pagos", "facturacion")); // Verde azulado
rejilla.add(tarjetaModulo("IN", new Color(0x39, 0x49, 0xAB), "Inventario", "Control de accesorios y materiales", "inventario")); // Índigo
if (esAdmin) {
rejilla.add(tarjetaModulo("US", new Color(0xD8, 0x1B, 0x60), "Usuarios y roles", "Cuentas de acceso al sistema", "usuarios")); // Rosa
}
rejilla.add(tarjetaModulo("RE", new Color(0x43, 0xA0, 0x47), "Reportes", "Estadísticas generales", "reportes")); // Verde
JPanel centro = new JPanel(new BorderLayout());
centro.setOpaque(false);
centro.add(rejilla, BorderLayout.NORTH);
panel.add(encabezado, BorderLayout.NORTH);
panel.add(centro, BorderLayout.CENTER);
JScrollPane scroll = new JScrollPane(panel);
scroll.setBorder(null);
scroll.getViewport().setBackground(EstiloUI.COLOR_FONDO);
scroll.getVerticalScrollBar().setUnitIncrement(16);
return scroll;
}
private JComponent tarjetaModulo(String siglas, Color colorIcono, String titulo, String descripcion, String cardName) {
EstiloUI.TarjetaRedondeada tarjeta = new EstiloUI.TarjetaRedondeada();
tarjeta.setLayout(new BorderLayout());
tarjeta.setBorder(new EmptyBorder(20, 20, 20, 20));
tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
EstiloUI.IconoMonograma icono = new EstiloUI.IconoMonograma(siglas, colorIcono, 40);
icono.setAlignmentX(Component.LEFT_ALIGNMENT);
JLabel tituloLbl = new JLabel(titulo);
tituloLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
tituloLbl.setForeground(EstiloUI.COLOR_TEXTO);
tituloLbl.setBorder(new EmptyBorder(12, 0, 0, 0));
JLabel descLbl = new JLabel("<html><body style='width: 170px'>" + descripcion + "</body></html>");
descLbl.setFont(EstiloUI.F_SUBTITULO);
descLbl.setForeground(EstiloUI.COLOR_TEXTO_SUAVE);
descLbl.setBorder(new EmptyBorder(4, 0, 0, 0));
JPanel textos = new JPanel();
textos.setOpaque(false);
textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
icono.setAlignmentX(Component.LEFT_ALIGNMENT);
tituloLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
textos.add(icono);
textos.add(tituloLbl);
textos.add(descLbl);
tarjeta.add(textos, BorderLayout.CENTER);
tarjeta.addMouseListener(new java.awt.event.MouseAdapter() {
@Override
public void mouseClicked(java.awt.event.MouseEvent e) {
mostrarCarta(cardName);
}
@Override
public void mouseEntered(java.awt.event.MouseEvent e) {
tarjeta.setBorder(new CompoundBorder(
BorderFactory.createLineBorder(colorIcono, 1, true),
new EmptyBorder(19, 19, 19, 19)));
tarjeta.repaint();
}
@Override
public void mouseExited(java.awt.event.MouseEvent e) {
tarjeta.setBorder(new EmptyBorder(20, 20, 20, 20));
tarjeta.repaint();
}
});
return tarjeta;
}
}