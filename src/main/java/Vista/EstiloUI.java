package Vista;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
/**
* ============================================================================
* SISTEMA DE DISEÑO UNIFICADO — Clínica Dental
* ============================================================================
* Única fuente de verdad para colores, tipografía, botones, tablas, tarjetas
* e íconos de TODO el sistema. Todos los paneles (Vista.*) deben usar esta
* clase en vez de definir su propia paleta — así se garantiza consistencia
* visual real en toda la aplicación.
*
* NOTAS TÉCNICAS IMPORTANTES:
* 1) Los botones y encabezados de tabla se pintan a mano (paintComponent /
* TableCellRenderer) en vez de usar JButton#setBackground o
* JTableHeader#setBackground directamente, porque el Look&Feel de Windows
* ignora esos colores y deja el texto invisible sobre fondo blanco.
* 2) Los íconos del menú y del dashboard NO usan emoji Unicode (🏠, 🦷, etc.):
* en muchos sistemas Windows la fuente "Segoe UI Emoji" no está asociada
* correctamente y esos glifos se pintan como cuadros vacíos "□". En su
* lugar se usa IconoMonograma: un cuadrado de color con 1-2 letras,
* dibujado con Graphics2D, que se ve igual en cualquier equipo.
* 3) FIX (ago-2026): estilizarCampo() forzaba un alto de 36px mientras el
* borde compuesto (BordeRedondeado + EmptyBorder) consumía 26px de
* padding vertical, dejando solo 10px para el texto → el contenido de
* JComboBox/JTextField se veía cortado arriba y abajo. Se subió el alto
* a 42px y se redujo el padding del borde para dejar espacio suficiente.
* ============================================================================
*/
public final class EstiloUI {
// ---------------------------------------------------------------------
// PALETA DE COLORES (única para todo el sistema) — TONOS VERDES
// ---------------------------------------------------------------------
public static final Color COLOR_FONDO = new Color(0xF2, 0xF7, 0xF4); // Fondo ligeramente verdoso
public static final Color COLOR_PANEL = Color.WHITE;
public static final Color COLOR_BORDE = new Color(0xD8, 0xE5, 0xDE);
public static final Color COLOR_BORDE_FUERTE = new Color(0xB8, 0xCD, 0xC2);
public static final Color COLOR_PRIMARIO = new Color(0x1B, 0x6B, 0x4A); // Verde principal
public static final Color COLOR_PRIMARIO_OSC = new Color(0x12, 0x4F, 0x36); // Verde oscuro (hover/pressed)
public static final Color COLOR_PRIMARIO_SUAVE = new Color(0xE4, 0xF4, 0xEB); // Verde muy suave
public static final Color COLOR_ACENTO = new Color(0x14, 0x9B, 0x6A); // Verde acento más vivo
public static final Color COLOR_ACENTO_OSC = new Color(0x0F, 0x7C, 0x54);
public static final Color COLOR_EXITO = new Color(0x1E, 0x7A, 0x46);
public static final Color COLOR_EXITO_SUAVE = new Color(0xE4, 0xF3, 0xE9);
public static final Color COLOR_PELIGRO = new Color(0xC0, 0x39, 0x2B);
public static final Color COLOR_PELIGRO_OSC = new Color(0x9C, 0x2C, 0x21);
public static final Color COLOR_PELIGRO_SUAVE = new Color(0xFB, 0xE7, 0xE5);
public static final Color COLOR_ALERTA = new Color(0xB5, 0x4A, 0x0F);
public static final Color COLOR_ALERTA_SUAVE = new Color(0xFD, 0xEE, 0xDC);
public static final Color COLOR_TEXTO = new Color(0x1E, 0x2B, 0x24);
public static final Color COLOR_TEXTO_SUAVE = new Color(0x5A, 0x6E, 0x63);
public static final Color COLOR_TEXTO_CLARO = new Color(0xD0, 0xE8, 0xDB);
public static final Color COLOR_FILA_ALT = new Color(0xF4, 0xF9, 0xF6);
public static final Color COLOR_SELECCION = new Color(0xD6, 0xED, 0xE0); // Selección verde suave
// ---------------------------------------------------------------------
// TIPOGRAFÍA
// ---------------------------------------------------------------------
private static final String FAM = "Segoe UI";
public static final Font F_MARCA = new Font(FAM, Font.BOLD, 18);
public static final Font F_TITULO = new Font(FAM, Font.BOLD, 21);
public static final Font F_SUBTITULO = new Font(FAM, Font.PLAIN, 13);
public static final Font F_SECCION = new Font(FAM, Font.BOLD, 14);
public static final Font F_LABEL = new Font(FAM, Font.BOLD, 12);
public static final Font F_CAMPO = new Font(FAM, Font.PLAIN, 14);
public static final Font F_TABLA = new Font(FAM, Font.PLAIN, 13);
public static final Font F_TABLA_HD = new Font(FAM, Font.BOLD, 13);
public static final Font F_BOTON = new Font(FAM, Font.BOLD, 13);
public static final Font F_VALOR = new Font(FAM, Font.BOLD, 24);
public static final Font F_MENU = new Font(FAM, Font.PLAIN, 14);
public static final Font F_MENU_SEC = new Font(FAM, Font.BOLD, 11);
private EstiloUI() { }
// ---------------------------------------------------------------------
// BOTONES
// ---------------------------------------------------------------------
/** Botón sólido (acción principal). El color se pinta a mano: funciona igual en cualquier L&F. */
public static JButton botonSolido(String texto, Color fondo, Color fondoHover, Color colorTexto) {
JButton btn = new JButton(texto) {
@Override
protected void paintComponent(Graphics g) {
Graphics2D g2 = crearG2(g);
Color relleno = !isEnabled() ? mezclar(fondo, Color.WHITE, 0.5f)
: (getModel().isPressed() ? fondoHover : (getModel().isRollover() ? fondoHover : fondo));
g2.setColor(relleno);
g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() - 1, 9, 9));
g2.dispose();
super.paintComponent(g);
}
};
prepararBoton(btn, colorTexto);
return btn;
}
/** Botón de contorno (acción secundaria): fondo blanco/leve, borde y texto de color. */
public static JButton botonContorno(String texto, Color color) {
JButton btn = new JButton(texto) {
@Override
protected void paintComponent(Graphics g) {
Graphics2D g2 = crearG2(g);
g2.setColor(getModel().isRollover() ? mezclar(color, Color.WHITE, 0.88f) : Color.WHITE);
g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() - 1, 9, 9));
g2.setColor(color);
g2.setStroke(new BasicStroke(1.3f));
g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth() - 1.6f, getHeight() - 2.2f, 9, 9));
g2.dispose();
super.paintComponent(g);
}
};
prepararBoton(btn, color);
return btn;
}
/** Botón "fantasma": sin fondo ni borde propio, para usarse dentro de menús laterales de color. */
public static JButton botonMenu(String texto, Color fondoNormal, Color fondoHover, Color fondoActivo) {
JButton btn = new JButton(texto) {
@Override
protected void paintComponent(Graphics g) {
Graphics2D g2 = crearG2(g);
Color estado = (Boolean.TRUE.equals(getClientProperty("activo"))) ? fondoActivo
: (getModel().isRollover() ? fondoHover : fondoNormal);
if (estado != null) {
g2.setColor(estado);
g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
}
g2.dispose();
super.paintComponent(g);
}
};
btn.setFont(F_MENU);
btn.setForeground(Color.WHITE);
btn.setContentAreaFilled(false);
btn.setBorderPainted(false);
btn.setFocusPainted(false);
btn.setOpaque(false);
btn.setHorizontalAlignment(SwingConstants.LEFT);
btn.setBorder(new EmptyBorder(9, 12, 9, 12));
btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
btn.setAlignmentX(Component.LEFT_ALIGNMENT);
btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
btn.addChangeListener(e -> btn.repaint());
return btn;
}
private static void prepararBoton(JButton btn, Color colorTexto) {
btn.setFont(F_BOTON);
btn.setForeground(colorTexto);
btn.setContentAreaFilled(false);
btn.setBorderPainted(false);
btn.setFocusPainted(false);
btn.setOpaque(false);
btn.setBorder(new EmptyBorder(10, 18, 10, 18));
btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
btn.setAlignmentX(Component.CENTER_ALIGNMENT);
}
private static Graphics2D crearG2(Graphics g) {
Graphics2D g2 = (Graphics2D) g.create();
g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
return g2;
}
private static Color mezclar(Color a, Color b, float t) {
int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
int bb = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
return new Color(clamp(r), clamp(g), clamp(bb));
}
private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
// ---------------------------------------------------------------------
// CAMPOS DE FORMULARIO
// ---------------------------------------------------------------------
public static JLabel crearEtiqueta(String texto) {
JLabel lbl = new JLabel(texto);
lbl.setFont(F_LABEL);
lbl.setForeground(COLOR_TEXTO_SUAVE);
return lbl;
}
/**
* Aplica tipografía, alto y borde redondeado consistente a un campo de
* texto o combo.
*
* FIX: antes se forzaba un alto de 36px mientras el borde compuesto
* (BordeRedondeado + EmptyBorder) consumía 26px verticales, dejando solo
* 10px para el texto (insuficiente para fuente de 14px) → el contenido
* se veía cortado arriba y abajo, sobre todo en JComboBox. Ahora el alto
* es 42px y el padding se redujo a 6,10,6,10 (EmptyBorder) + 4,8,4,8
* (BordeRedondeado), dejando ~26px libres para el texto.
*/
public static void estilizarCampo(JComponent campo) {
campo.setFont(F_CAMPO);
campo.setBorder(new CompoundBorder(
new BordeRedondeado(COLOR_BORDE, 8),
new EmptyBorder(6, 10, 6, 10)));
campo.setPreferredSize(new Dimension(campo.getPreferredSize().width, 42));
instalarFoco(campo);
}
/** Resalta el borde del campo en verde primario cuando obtiene el foco (feedback visual claro). */
private static void instalarFoco(JComponent campo) {
campo.addFocusListener(new java.awt.event.FocusAdapter() {
@Override public void focusGained(java.awt.event.FocusEvent e) {
campo.setBorder(new CompoundBorder(new BordeRedondeado(COLOR_PRIMARIO, 8, 1.6f), new EmptyBorder(6, 10, 6, 10)));
}
@Override public void focusLost(java.awt.event.FocusEvent e) {
campo.setBorder(new CompoundBorder(new BordeRedondeado(COLOR_BORDE, 8), new EmptyBorder(6, 10, 6, 10)));
}
});
}
// ---------------------------------------------------------------------
// TABLAS — encabezado y filas legibles en cualquier Look & Feel
// ---------------------------------------------------------------------
/** Aplica look consistente a una JTable completa: encabezado, filas alternas, selección, alto de fila. */
public static void estilizarTabla(JTable tabla) {
tabla.setFont(F_TABLA);
tabla.setRowHeight(34);
tabla.setShowGrid(false);
tabla.setIntercellSpacing(new Dimension(0, 0));
tabla.setSelectionBackground(COLOR_SELECCION);
tabla.setSelectionForeground(COLOR_TEXTO);
tabla.setFillsViewportHeight(true);
tabla.setForeground(COLOR_TEXTO);
tabla.getTableHeader().setReorderingAllowed(false);
estilizarEncabezado(tabla.getTableHeader());
tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
@Override
public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
setBorder(new EmptyBorder(0, 14, 0, 14));
if (!sel) {
c.setBackground(row % 2 == 0 ? COLOR_PANEL : COLOR_FILA_ALT);
c.setForeground(COLOR_TEXTO);
}
return c;
}
});
}
/**
* Encabezado de tabla con fondo de color y texto blanco garantizados mediante
* un TableCellRenderer propio (header.setBackground solo, en Windows, se ignora).
*/
public static void estilizarEncabezado(JTableHeader header) {
header.setDefaultRenderer(new DefaultTableCellRenderer() {
{ setOpaque(true); setHorizontalAlignment(SwingConstants.LEFT); setBorder(new EmptyBorder(0, 14, 0, 14)); }
@Override
public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
boolean hasFocus, int row, int column) {
super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
setBackground(COLOR_PRIMARIO);
setForeground(Color.WHITE);
setFont(F_TABLA_HD);
return this;
}
});
header.setPreferredSize(new Dimension(header.getWidth(), 40));
header.setOpaque(true);
header.setBackground(COLOR_PRIMARIO);
}
/** Envuelve una tabla en un JScrollPane con borde de tarjeta consistente. */
public static JScrollPane envolverTabla(JTable tabla) {
JScrollPane scroll = new JScrollPane(tabla);
scroll.setBorder(new BordeRedondeado(COLOR_BORDE, 12));
scroll.getViewport().setBackground(COLOR_PANEL);
scroll.getVerticalScrollBar().setUnitIncrement(16);
return scroll;
}
// ---------------------------------------------------------------------
// TARJETAS (cards) y "chips" de estado
// ---------------------------------------------------------------------
/** Panel con esquinas redondeadas y fondo blanco, estilo "tarjeta" (card). Reutilizable en cualquier panel. */
public static class TarjetaRedondeada extends JPanel {
public TarjetaRedondeada() { setOpaque(false); }
@Override
protected void paintComponent(Graphics g) {
Graphics2D g2 = crearG2(g);
g2.setColor(COLOR_PANEL);
g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
g2.setColor(COLOR_BORDE);
g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
g2.dispose();
super.paintComponent(g);
}
}
/** Envuelve un componente en una tarjeta con título de sección. */
public static JComponent tarjetaConTitulo(String titulo, JComponent contenido) {
TarjetaRedondeada tarjeta = new TarjetaRedondeada();
tarjeta.setLayout(new BorderLayout(0, 12));
tarjeta.setBorder(new EmptyBorder(18, 18, 18, 18));
JLabel lbl = new JLabel(titulo);
lbl.setFont(F_SECCION);
lbl.setForeground(COLOR_TEXTO);
tarjeta.add(lbl, BorderLayout.NORTH);
tarjeta.add(contenido, BorderLayout.CENTER);
return tarjeta;
}
/** Chip de estado (p.ej. "Activo" / "Inactivo", "Stock bajo"): texto con fondo suave redondeado. */
public static JLabel chipEstado(String texto, Color colorTexto, Color colorFondo) {
JLabel chip = new JLabel(texto, SwingConstants.CENTER) {
@Override
protected void paintComponent(Graphics g) {
Graphics2D g2 = crearG2(g);
g2.setColor(colorFondo);
g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
g2.dispose();
super.paintComponent(g);
}
};
chip.setFont(F_LABEL);
chip.setForeground(colorTexto);
chip.setOpaque(false);
chip.setBorder(new EmptyBorder(3, 12, 3, 12));
return chip;
}
/** Borde redondeado reutilizable para tarjetas, tablas y campos de texto. */
public static class BordeRedondeado extends javax.swing.border.AbstractBorder {
private final Color color; private final int radio; private final float grosor;
public BordeRedondeado(Color color, int radio) { this(color, radio, 1.1f); }
public BordeRedondeado(Color color, int radio, float grosor) {
this.color = color; this.radio = radio; this.grosor = grosor;
}
@Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
Graphics2D g2 = crearG2(g);
g2.setColor(color);
g2.setStroke(new BasicStroke(grosor));
g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1.5f, h - 1.5f, radio, radio));
g2.dispose();
}
@Override public Insets getBorderInsets(Component c) { return new Insets(4, 8, 4, 8); }
@Override public Insets getBorderInsets(Component c, Insets insets) {
insets.set(4, 8, 4, 8); return insets;
}
}
// ---------------------------------------------------------------------
// ÍCONOS VECTORIALES (reemplazo robusto de los emoji que no renderizaban)
// ---------------------------------------------------------------------
/**
* Cuadrado de color con 1-2 letras (monograma), dibujado con Graphics2D.
* No depende de fuentes de emoji: se ve idéntico en Windows, macOS y Linux.
* Uso: new IconoMonograma("PA", new Color(0x1B, 0x6B, 0x4A))
*/
public static class IconoMonograma extends JComponent {
private final String letras;
private final Color color;
private final int tam;
public IconoMonograma(String letras, Color color) { this(letras, color, 34); }
public IconoMonograma(String letras, Color color, int tam) {
this.letras = letras;
this.color = color;
this.tam = tam;
setPreferredSize(new Dimension(tam, tam));
setMinimumSize(new Dimension(tam, tam));
setMaximumSize(new Dimension(tam, tam));
setOpaque(false);
}
@Override
protected void paintComponent(Graphics g) {
Graphics2D g2 = crearG2(g);
g2.setColor(color);
g2.fill(new RoundRectangle2D.Float(0, 0, tam, tam - 1, 9, 9));
g2.setFont(new Font(FAM, Font.BOLD, letras.length() > 1 ? tam / 3 : (int) (tam / 2.4)));
FontMetrics fm = g2.getFontMetrics();
int tx = (tam - fm.stringWidth(letras)) / 2;
int ty = (tam - fm.getHeight()) / 2 + fm.getAscent();
g2.setColor(Color.WHITE);
g2.drawString(letras, tx, ty);
g2.dispose();
}
}
/** DefaultTableModel de solo lectura, compartido por todos los paneles (evita repetir la misma clase anónima). */
public static class TablaSinEdicion extends javax.swing.table.DefaultTableModel {
public TablaSinEdicion(Object[] columnas) { super(columnas, 0); }
@Override public boolean isCellEditable(int row, int col) { return false; }
}
/** Punto de color simple (para leyendas y estados), sin depender de emoji. */
public static JComponent puntoColor(Color color, int diametro) {
JComponent punto = new JComponent() {
@Override protected void paintComponent(Graphics g) {
Graphics2D g2 = crearG2(g);
g2.setColor(color);
g2.fillOval(0, 0, diametro, diametro);
g2.dispose();
}
};
punto.setPreferredSize(new Dimension(diametro, diametro));
punto.setOpaque(false);
return punto;
}
}
