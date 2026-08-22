package Vista;
import Modelo.Accesorio;
import Modelo.AccesorioDAO;
import Modelo.Categoria;
import Modelo.CategoriaDAO;
import Modelo.MovimientoInventario;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
public class InventarioPanel extends JPanel {
// ---------- Paleta de colores (misma línea visual que FacturacionPanel) ----------
private static final Color COLOR_FONDO = new Color(0xF4, 0xF6, 0xF9);
private static final Color COLOR_TARJETA = Color.WHITE;
private static final Color COLOR_BORDE = new Color(0xE1, 0xE5, 0xEA);
private static final Color COLOR_PRIMARIO = new Color(0x1E, 0x5F, 0x9E);
private static final Color COLOR_PRIMARIO_HOVER = new Color(0x18, 0x4E, 0x82);
private static final Color COLOR_SECUNDARIO = new Color(0xEE, 0xF2, 0xF7);
private static final Color COLOR_SECUNDARIO_HOVER= new Color(0xE0, 0xE7, 0xEF);
private static final Color COLOR_TEXTO = new Color(0x2C, 0x33, 0x3A);
private static final Color COLOR_TEXTO_SUAVE = new Color(0x6B, 0x74, 0x80);
private static final Color COLOR_TABLA_ALT = new Color(0xF8, 0xFA, 0xFC);
private static final Color COLOR_TABLA_SEL = new Color(0xDD, 0xEB, 0xF7);
private static final Color COLOR_ALERTA_TEXTO = new Color(0xB5, 0x4A, 0x0F);
private static final Color COLOR_ALERTA_FONDO = new Color(0xFD, 0xEE, 0xDC);
private static final Color COLOR_ENTRADA_TEXTO = new Color(0x1E, 0x7A, 0x3D);
private static final Color COLOR_SALIDA_TEXTO = new Color(0xB0, 0x2E, 0x2E);
private static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 18);
private static final Font FUENTE_SECCION = new Font("Segoe UI", Font.BOLD, 13);
private static final Font FUENTE_LABEL = new Font("Segoe UI", Font.PLAIN, 12);
private static final Font FUENTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 13);
private static final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 13);
private static final Font FUENTE_TABLA = new Font("Segoe UI", Font.PLAIN, 13);
private static final Font FUENTE_TABLA_HDR = new Font("Segoe UI", Font.BOLD, 13);
private static final Font FUENTE_ALERTA = new Font("Segoe UI", Font.BOLD, 12);
private final AccesorioDAO dao = new AccesorioDAO();
private final CategoriaDAO categoriaDAO = new CategoriaDAO();
private final DefaultTableModel modelo = new DefaultTableModel(
new Object[]{"ID", "Nombre", "Categoría", "Descripción", "Costo", "Venta", "Stock", "Mínimo", "Alerta"}, 0) {
@Override public boolean isCellEditable(int row, int col) { return false; }
};
private final JTable tabla = new JTable(modelo);
private final JTextField txtNombre = new JTextField();
private final JTextField txtDescripcion = new JTextField();
private final JTextField txtCosto = new JTextField();
private final JTextField txtVenta = new JTextField();
private final JTextField txtStock = new JTextField();
private final JTextField txtMinimo = new JTextField();
private final JComboBox<Categoria> cbCategoria = new JComboBox<>();
private int idSeleccionado = -1;
private final JComboBox<Categoria> cbFiltroCategoria = new JComboBox<>();
private final JTextField txtCantidadMov = new JTextField();
private final JComboBox<String> cbTipoMov = new JComboBox<>(new String[]{"Entrada", "Salida"});
private final JTextField txtNotaMov = new JTextField();
private JLabel lblFormTitulo;
public InventarioPanel() {
setLayout(new BorderLayout());
setBackground(COLOR_FONDO);
setBorder(new EmptyBorder(20, 20, 20, 20));
JPanel norte = new JPanel(new BorderLayout());
norte.setOpaque(false);
norte.add(construirEncabezado(), BorderLayout.NORTH);
norte.add(construirBarraSuperior(), BorderLayout.SOUTH);
add(norte, BorderLayout.NORTH);
JSplitPane splitPane = new JSplitPane(
JSplitPane.HORIZONTAL_SPLIT,
construirPanelTabla(),
construirPanelDerechoConScroll()
);
splitPane.setBorder(null);
splitPane.setOpaque(false);
splitPane.setDividerSize(10);
splitPane.setResizeWeight(1.0);
splitPane.setContinuousLayout(true);
splitPane.setOneTouchExpandable(true);
add(splitPane, BorderLayout.CENTER);
tabla.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());
cargarCategorias();
cargarTabla(false);
}
// ---------- Encabezado ----------
private JComponent construirEncabezado() {
JPanel panel = new JPanel(new BorderLayout());
panel.setOpaque(false);
panel.setBorder(new EmptyBorder(0, 0, 15, 0));
JLabel titulo = new JLabel("Inventario");
titulo.setFont(FUENTE_TITULO);
titulo.setForeground(COLOR_TEXTO);
JLabel subtitulo = new JLabel("Administra accesorios, categorías y movimientos de stock");
subtitulo.setFont(FUENTE_LABEL);
subtitulo.setForeground(COLOR_TEXTO_SUAVE);
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
// ---------- Barra de filtros y acciones ----------
private JComponent construirBarraSuperior() {
JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
panel.setOpaque(false);
panel.setBorder(new EmptyBorder(0, 0, 15, 0));
JButton btnTodos = crearBotonPrimario("Ver todos");
btnTodos.addActionListener(e -> { cbFiltroCategoria.setSelectedIndex(0); cargarTabla(false); });
JButton btnStockBajo = crearBotonSecundario("⚠ Alertas de stock bajo");
btnStockBajo.addActionListener(e -> cargarTabla(true));
estilizarCampo(cbFiltroCategoria);
cbFiltroCategoria.setPreferredSize(new Dimension(170, 34));
JButton btnFiltrar = crearBotonSecundario("Filtrar por categoría");
btnFiltrar.addActionListener(e -> filtrarPorCategoriaSeleccionada());
JButton btnHistorialSel = crearBotonSecundario("🕘 Historial (seleccionado)");
btnHistorialSel.addActionListener(e -> mostrarHistorialSeleccionado());
JButton btnMovRecientes = crearBotonSecundario("Movimientos recientes");
btnMovRecientes.addActionListener(e -> mostrarMovimientosRecientes());
panel.add(btnTodos);
panel.add(btnStockBajo);
panel.add(cbFiltroCategoria);
panel.add(btnFiltrar);
panel.add(btnHistorialSel);
panel.add(btnMovRecientes);
return panel;
}
// ---------- Panel de la tabla ----------
private JComponent construirPanelTabla() {
tabla.setFont(FUENTE_TABLA);
tabla.setRowHeight(30);
tabla.setShowGrid(false);
tabla.setIntercellSpacing(new Dimension(0, 0));
tabla.setSelectionBackground(COLOR_TABLA_SEL);
tabla.setSelectionForeground(COLOR_TEXTO);
tabla.setFillsViewportHeight(true);
tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
JTableHeader header = tabla.getTableHeader();
header.setFont(FUENTE_TABLA_HDR);
header.setBackground(COLOR_TARJETA);
header.setForeground(COLOR_TEXTO);
header.setPreferredSize(new Dimension(header.getWidth(), 38));
header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_BORDE));
header.setReorderingAllowed(false);
tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
@Override
public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
setBorder(new EmptyBorder(0, 12, 0, 12));
boolean esColumnaAlerta = col == 8;
boolean tieneAlerta = esColumnaAlerta && v != null && !v.toString().isEmpty();
if (sel) {
setFont(esColumnaAlerta && tieneAlerta ? FUENTE_ALERTA : FUENTE_TABLA);
setForeground(tieneAlerta ? COLOR_ALERTA_TEXTO : COLOR_TEXTO);
} else {
c.setBackground(row % 2 == 0 ? COLOR_TARJETA : COLOR_TABLA_ALT);
if (tieneAlerta) {
setFont(FUENTE_ALERTA);
setForeground(COLOR_ALERTA_TEXTO);
} else {
setFont(FUENTE_TABLA);
setForeground(COLOR_TEXTO);
}
}
return c;
}
});
JScrollPane scroll = new JScrollPane(tabla);
scroll.setBorder(tarjetaBorde());
scroll.getViewport().setBackground(COLOR_TARJETA);
JPanel contenedor = new JPanel(new BorderLayout());
contenedor.setOpaque(false);
contenedor.setBorder(new EmptyBorder(0, 0, 0, 10));
contenedor.add(scroll, BorderLayout.CENTER);
return contenedor;
}
// ---------- Panel derecho (formularios) ----------
/**
* Envuelve el panel derecho (formulario + movimiento) en un JScrollPane.
* Antes se agregaba directamente al JSplitPane sin scroll, así que si la
* ventana no era lo bastante alta, la tarjeta "Movimiento de inventario"
* quedaba cortada y no se podían ver ni usar sus campos (Tipo de
* movimiento, Cantidad, Nota, botón Registrar movimiento).
*/
private JComponent construirPanelDerechoConScroll() {
JComponent contenido = construirPanelDerecho();
JScrollPane scroll = new JScrollPane(contenido,
JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
scroll.setBorder(null);
scroll.setOpaque(false);
scroll.getViewport().setOpaque(false);
scroll.getVerticalScrollBar().setUnitIncrement(16);
return scroll;
}
private JComponent construirPanelDerecho() {
// OJO: no usar setPreferredSize(new Dimension(340, 0)) aquí. Un
// JScrollPane calcula cuánto puede scrollear a partir de
// getPreferredSize() del contenido; si el alto preferido queda fijo
// en 0, el scroll se corta casi de inmediato aunque el contenido
// real (formulario + tarjeta de movimiento) sea mucho más alto. Por
// eso solo se fija el ANCHO sobrescribiendo getPreferredSize(), y el
// alto se deja que lo calcule el BoxLayout normalmente.
JPanel contenedor = new JPanel() {
@Override
public Dimension getPreferredSize() {
Dimension d = super.getPreferredSize();
return new Dimension(340, d.height);
}
};
contenedor.setOpaque(false);
contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
contenedor.setBorder(new EmptyBorder(0, 10, 0, 0));
JComponent formulario = construirTarjetaFormulario();
JComponent movimiento = construirTarjeta("Movimiento de inventario (accesorio seleccionado)", construirMovimiento());
formulario.setAlignmentX(Component.LEFT_ALIGNMENT);
movimiento.setAlignmentX(Component.LEFT_ALIGNMENT);
contenedor.add(formulario);
contenedor.add(Box.createVerticalStrut(15));
contenedor.add(movimiento);
contenedor.add(Box.createVerticalGlue());
return contenedor;
}
/** Tarjeta del formulario principal, con título dinámico (Nuevo accesorio / Editando: ...). */
private JComponent construirTarjetaFormulario() {
JPanel tarjeta = new JPanel(new BorderLayout());
tarjeta.setBackground(COLOR_TARJETA);
tarjeta.setBorder(new CompoundBorder(tarjetaBorde(), new EmptyBorder(16, 16, 16, 16)));
tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, tarjeta.getMaximumSize().height));
lblFormTitulo = new JLabel("Nuevo accesorio / material");
lblFormTitulo.setFont(FUENTE_SECCION);
lblFormTitulo.setForeground(COLOR_TEXTO);
lblFormTitulo.setBorder(new EmptyBorder(0, 0, 12, 0));
tarjeta.add(lblFormTitulo, BorderLayout.NORTH);
tarjeta.add(construirFormulario(), BorderLayout.CENTER);
return tarjeta;
}
private JComponent construirTarjeta(String titulo, JComponent contenido) {
JPanel tarjeta = new JPanel(new BorderLayout());
tarjeta.setBackground(COLOR_TARJETA);
tarjeta.setBorder(new CompoundBorder(tarjetaBorde(), new EmptyBorder(16, 16, 16, 16)));
tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, tarjeta.getMaximumSize().height));
JLabel lblTitulo = new JLabel(titulo);
lblTitulo.setFont(FUENTE_SECCION);
lblTitulo.setForeground(COLOR_TEXTO);
lblTitulo.setBorder(new EmptyBorder(0, 0, 12, 0));
tarjeta.add(lblTitulo, BorderLayout.NORTH);
tarjeta.add(contenido, BorderLayout.CENTER);
return tarjeta;
}
private Border tarjetaBorde() {
return BorderFactory.createLineBorder(COLOR_BORDE, 1, true);
}
private JComponent construirFormulario() {
JPanel panel = new JPanel(new GridBagLayout());
panel.setOpaque(false);
GridBagConstraints gbc = new GridBagConstraints();
gbc.fill = GridBagConstraints.HORIZONTAL;
gbc.weightx = 1;
gbc.gridx = 0;
estilizarCampo(txtNombre);
estilizarCampo(txtDescripcion);
estilizarCampo(txtCosto);
estilizarCampo(txtVenta);
estilizarCampo(txtStock);
estilizarCampo(txtMinimo);
estilizarCampo(cbCategoria);
int row = 0;
row = campo(panel, gbc, row, "Nombre", txtNombre);
row = campo(panel, gbc, row, "Descripción", txtDescripcion);
gbc.gridy = row; gbc.insets = new Insets(0, 0, 4, 0);
panel.add(crearEtiqueta("Categoría"), gbc);
row++;
gbc.gridy = row; gbc.insets = new Insets(0, 0, 6, 0);
panel.add(cbCategoria, gbc);
row++;
JButton btnNuevaCategoria = crearBotonSecundario("+ Nueva categoría");
btnNuevaCategoria.addActionListener(e -> crearCategoriaRapida());
gbc.gridy = row; gbc.insets = new Insets(0, 0, 14, 0);
panel.add(btnNuevaCategoria, gbc);
row++;
row = campo(panel, gbc, row, "Precio costo (RD$)", txtCosto);
row = campo(panel, gbc, row, "Precio venta (RD$)", txtVenta);
row = campo(panel, gbc, row, "Stock inicial", txtStock);
row = campo(panel, gbc, row, "Stock mínimo", txtMinimo);
JPanel botones = new JPanel(new GridLayout(1, 2, 8, 0));
botones.setOpaque(false);
JButton btnNuevo = crearBotonSecundario("Nuevo");
JButton btnGuardar = crearBotonPrimario("Guardar");
btnNuevo.addActionListener(e -> limpiar());
btnGuardar.addActionListener(e -> guardar());
botones.add(btnNuevo);
botones.add(btnGuardar);
gbc.gridy = row; gbc.insets = new Insets(2, 0, 0, 0);
panel.add(botones, gbc);
return panel;
}
private JComponent construirMovimiento() {
JPanel panel = new JPanel(new GridBagLayout());
panel.setOpaque(false);
GridBagConstraints gbc = new GridBagConstraints();
gbc.fill = GridBagConstraints.HORIZONTAL;
gbc.weightx = 1;
gbc.gridx = 0;
estilizarCampo(cbTipoMov);
estilizarCampo(txtCantidadMov);
estilizarCampo(txtNotaMov);
gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0); panel.add(crearEtiqueta("Tipo de movimiento"), gbc);
gbc.gridy = 1; gbc.insets = new Insets(0, 0, 14, 0); panel.add(cbTipoMov, gbc);
gbc.gridy = 2; gbc.insets = new Insets(0, 0, 4, 0); panel.add(crearEtiqueta("Cantidad"), gbc);
gbc.gridy = 3; gbc.insets = new Insets(0, 0, 14, 0); panel.add(txtCantidadMov, gbc);
gbc.gridy = 4; gbc.insets = new Insets(0, 0, 4, 0); panel.add(crearEtiqueta("Nota"), gbc);
gbc.gridy = 5; gbc.insets = new Insets(0, 0, 16, 0); panel.add(txtNotaMov, gbc);
JButton btnRegistrar = crearBotonPrimario("Registrar movimiento");
btnRegistrar.addActionListener(e -> registrarMovimiento());
gbc.gridy = 6; gbc.insets = new Insets(0, 0, 0, 0); panel.add(btnRegistrar, gbc);
return panel;
}
private int campo(JPanel panel, GridBagConstraints gbc, int row, String etiqueta, JTextField campoTexto) {
gbc.gridy = row; gbc.insets = new Insets(0, 0, 4, 0);
panel.add(crearEtiqueta(etiqueta), gbc);
row++;
gbc.gridy = row; gbc.insets = new Insets(0, 0, 14, 0);
panel.add(campoTexto, gbc);
return row + 1;
}
// ---------- Helpers de estilo ----------
private JLabel crearEtiqueta(String texto) {
JLabel lbl = new JLabel(texto);
lbl.setFont(FUENTE_LABEL);
lbl.setForeground(COLOR_TEXTO_SUAVE);
return lbl;
}
private void estilizarCampo(JComponent campo) {
campo.setFont(FUENTE_CAMPO);
if (campo instanceof JTextField) {
Border borde = new CompoundBorder(
BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
new EmptyBorder(6, 8, 6, 8)
);
((JTextField) campo).setBorder(borde);
} else if (campo instanceof JComboBox) {
campo.setBorder(new CompoundBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1, true), new EmptyBorder(2, 4, 2, 4)));
}
campo.setPreferredSize(new Dimension(campo.getPreferredSize().width, 34));
}
private JButton crearBotonPrimario(String texto) {
JButton boton = new JButton(texto);
boton.setFont(FUENTE_BOTON);
boton.setForeground(Color.WHITE);
boton.setBackground(COLOR_PRIMARIO);
boton.setFocusPainted(false);
boton.setBorder(new EmptyBorder(10, 14, 10, 14));
boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
boton.setOpaque(true);
boton.setBorderPainted(false);
boton.addChangeListener(e -> {
ButtonModel m = boton.getModel();
boton.setBackground(m.isRollover() ? COLOR_PRIMARIO_HOVER : COLOR_PRIMARIO);
});
return boton;
}
private JButton crearBotonSecundario(String texto) {
JButton boton = new JButton(texto);
boton.setFont(FUENTE_BOTON);
boton.setForeground(COLOR_PRIMARIO);
boton.setBackground(COLOR_SECUNDARIO);
boton.setFocusPainted(false);
boton.setBorder(new CompoundBorder(
BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
new EmptyBorder(9, 13, 9, 13)
));
boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
boton.setOpaque(true);
boton.setBorderPainted(true);
boton.addChangeListener(e -> {
ButtonModel m = boton.getModel();
boton.setBackground(m.isRollover() ? COLOR_SECUNDARIO_HOVER : COLOR_SECUNDARIO);
});
return boton;
}
// ---------- Categorías ----------
private void cargarCategorias() {
try {
List<Categoria> categorias = categoriaDAO.listarTodas();
cbCategoria.removeAllItems();
cbCategoria.addItem(null); // "Sin categoría"
for (Categoria c : categorias) cbCategoria.addItem(c);
cbCategoria.setRenderer(new javax.swing.DefaultListCellRenderer() {
@Override
public Component getListCellRendererComponent(JList<?> list, Object value, int index,
boolean isSelected, boolean cellHasFocus) {
super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
setText(value == null ? "Sin categoría" : ((Categoria) value).getNombre());
return this;
}
});
cbFiltroCategoria.removeAllItems();
cbFiltroCategoria.addItem(null); // "Todas"
for (Categoria c : categorias) cbFiltroCategoria.addItem(c);
cbFiltroCategoria.setRenderer(new javax.swing.DefaultListCellRenderer() {
@Override
public Component getListCellRendererComponent(JList<?> list, Object value, int index,
boolean isSelected, boolean cellHasFocus) {
super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
setText(value == null ? "Todas las categorías" : ((Categoria) value).getNombre());
return this;
}
});
} catch (SQLException ex) {
mostrarError(ex);
}
}
private void crearCategoriaRapida() {
String nombre = JOptionPane.showInputDialog(this, "Nombre de la nueva categoría:", "Nueva categoría", JOptionPane.PLAIN_MESSAGE);
if (nombre == null || nombre.trim().isEmpty()) return;
try {
int id = categoriaDAO.crear(nombre.trim(), null);
cargarCategorias();
for (int i = 0; i < cbCategoria.getItemCount(); i++) {
Categoria c = cbCategoria.getItemAt(i);
if (c != null && c.getId() == id) {
cbCategoria.setSelectedIndex(i);
break;
}
}
} catch (SQLException ex) {
mostrarError(ex);
}
}
private void filtrarPorCategoriaSeleccionada() {
Categoria seleccionada = (Categoria) cbFiltroCategoria.getSelectedItem();
if (seleccionada == null) {
cargarTabla(false);
return;
}
try {
List<Accesorio> lista = dao.listarPorCategoria(seleccionada.getId());
volcarEnTabla(lista);
} catch (SQLException ex) {
mostrarError(ex);
}
}
// ---------- Lógica principal ----------
private void cargarTabla(boolean soloStockBajo) {
try {
List<Accesorio> lista = soloStockBajo ? dao.listarStockBajo() : dao.listarTodos();
volcarEnTabla(lista);
} catch (SQLException ex) {
mostrarError(ex);
}
}
private void volcarEnTabla(List<Accesorio> lista) {
modelo.setRowCount(0);
for (Accesorio a : lista) {
modelo.addRow(new Object[]{a.getId(), a.getNombre(),
a.getCategoriaNombre() == null ? "—" : a.getCategoriaNombre(), a.getDescripcion(),
a.getPrecioCosto(), a.getPrecioVenta(), a.getStock(), a.getStockMinimo(),
a.isStockBajo() ? "⚠ Stock bajo" : ""});
}
}
private void cargarSeleccion() {
int fila = tabla.getSelectedRow();
if (fila < 0) return;
idSeleccionado = (int) modelo.getValueAt(fila, 0);
txtNombre.setText((String) modelo.getValueAt(fila, 1));
String categoriaNombre = (String) modelo.getValueAt(fila, 2);
seleccionarCategoriaPorNombre("—".equals(categoriaNombre) ? null : categoriaNombre);
txtDescripcion.setText((String) modelo.getValueAt(fila, 3));
txtCosto.setText(String.valueOf(modelo.getValueAt(fila, 4)));
txtVenta.setText(String.valueOf(modelo.getValueAt(fila, 5)));
txtStock.setText(String.valueOf(modelo.getValueAt(fila, 6)));
txtMinimo.setText(String.valueOf(modelo.getValueAt(fila, 7)));
lblFormTitulo.setText("Editando: " + txtNombre.getText());
}
private void seleccionarCategoriaPorNombre(String nombre) {
if (nombre == null) {
cbCategoria.setSelectedIndex(0);
return;
}
for (int i = 0; i < cbCategoria.getItemCount(); i++) {
Categoria c = cbCategoria.getItemAt(i);
if (c != null && c.getNombre().equals(nombre)) {
cbCategoria.setSelectedIndex(i);
return;
}
}
cbCategoria.setSelectedIndex(0);
}
private void guardar() {
if (txtNombre.getText().trim().isEmpty()) {
JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
BigDecimal costo, venta;
int stock, minimo;
try {
costo = new BigDecimal(txtCosto.getText().trim().isEmpty() ? "0" : txtCosto.getText().trim());
venta = new BigDecimal(txtVenta.getText().trim().isEmpty() ? "0" : txtVenta.getText().trim());
stock = Integer.parseInt(txtStock.getText().trim().isEmpty() ? "0" : txtStock.getText().trim());
minimo = Integer.parseInt(txtMinimo.getText().trim().isEmpty() ? "5" : txtMinimo.getText().trim());
} catch (NumberFormatException ex) {
JOptionPane.showMessageDialog(this, "Precio/stock deben ser numéricos.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
if (costo.compareTo(BigDecimal.ZERO) < 0 || venta.compareTo(BigDecimal.ZERO) < 0) {
JOptionPane.showMessageDialog(this, "Los precios no pueden ser negativos.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
if (stock < 0 || minimo < 0) {
JOptionPane.showMessageDialog(this, "El stock y el mínimo no pueden ser negativos.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
try {
Accesorio a = new Accesorio();
a.setId(idSeleccionado);
a.setNombre(txtNombre.getText().trim());
a.setDescripcion(txtDescripcion.getText().trim());
a.setPrecioCosto(costo);
a.setPrecioVenta(venta);
a.setStock(stock);
a.setStockMinimo(minimo);
Categoria categoriaSel = (Categoria) cbCategoria.getSelectedItem();
a.setCategoriaId(categoriaSel == null ? null : categoriaSel.getId());
if (idSeleccionado == -1) dao.crear(a); else dao.actualizar(a);
limpiar();
cargarTabla(false);
} catch (SQLException ex) {
mostrarError(ex);
}
}
private void registrarMovimiento() {
if (idSeleccionado == -1) {
JOptionPane.showMessageDialog(this, "Selecciona un accesorio de la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
return;
}
if (txtCantidadMov.getText().trim().isEmpty()) {
JOptionPane.showMessageDialog(this, "Ingresa la cantidad.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
int cantidad;
try {
cantidad = Integer.parseInt(txtCantidadMov.getText().trim());
} catch (NumberFormatException ex) {
JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
if (cantidad <= 0) {
JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a cero.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
String tipo = (String) cbTipoMov.getSelectedItem();
if ("Salida".equals(tipo) && !txtStock.getText().trim().isEmpty()) {
try {
int stockActual = Integer.parseInt(txtStock.getText().trim());
if (cantidad > stockActual) {
JOptionPane.showMessageDialog(this,
"Stock insuficiente. Disponible: " + stockActual + ".",
"Validación", JOptionPane.WARNING_MESSAGE);
return;
}
} catch (NumberFormatException ignored) { /* si no se puede leer, se valida igual en el DAO */ }
}
try {
dao.registrarMovimiento(idSeleccionado, tipo, cantidad, txtNotaMov.getText().trim());
txtCantidadMov.setText("");
txtNotaMov.setText("");
cargarTabla(false);
} catch (SQLException ex) {
mostrarError(ex);
}
}
private void limpiar() {
idSeleccionado = -1;
txtNombre.setText(""); txtDescripcion.setText(""); txtCosto.setText("");
txtVenta.setText(""); txtStock.setText(""); txtMinimo.setText("");
cbCategoria.setSelectedIndex(0);
tabla.clearSelection();
lblFormTitulo.setText("Nuevo accesorio / material");
}
// ---------- Historial de movimientos ----------
private void mostrarHistorialSeleccionado() {
if (idSeleccionado == -1) {
JOptionPane.showMessageDialog(this, "Selecciona un accesorio de la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
return;
}
try {
List<MovimientoInventario> lista = dao.listarMovimientos(idSeleccionado);
mostrarDialogoHistorial("Historial de movimientos — " + txtNombre.getText(), lista);
} catch (SQLException ex) {
mostrarError(ex);
}
}
private void mostrarMovimientosRecientes() {
try {
List<MovimientoInventario> lista = dao.listarMovimientosRecientes(100);
mostrarDialogoHistorial("Movimientos recientes de inventario (últimos 100)", lista);
} catch (SQLException ex) {
mostrarError(ex);
}
}
private void mostrarDialogoHistorial(String titulo, List<MovimientoInventario> lista) {
DefaultTableModel modeloHist = new DefaultTableModel(
new Object[]{"Fecha", "Producto", "Tipo", "Cantidad", "Nota", "Factura"}, 0) {
@Override public boolean isCellEditable(int row, int col) { return false; }
};
DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
for (MovimientoInventario m : lista) {
modeloHist.addRow(new Object[]{
m.getFecha().format(fmt), m.getAccesorioNombre(), m.getTipo(), m.getCantidad(),
m.getNota() == null ? "" : m.getNota(),
m.getFacturaId() == null ? "" : "#" + m.getFacturaId()
});
}
JTable tablaHist = new JTable(modeloHist);
tablaHist.setFont(FUENTE_TABLA);
tablaHist.setRowHeight(26);
tablaHist.getTableHeader().setFont(FUENTE_TABLA_HDR);
tablaHist.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
@Override
public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
setBorder(new EmptyBorder(0, 10, 0, 10));
if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : COLOR_TABLA_ALT);
if (col == 2) {
setForeground("Entrada".equals(v) ? COLOR_ENTRADA_TEXTO : COLOR_SALIDA_TEXTO);
} else {
setForeground(COLOR_TEXTO);
}
return c;
}
});
JScrollPane scroll = new JScrollPane(tablaHist);
scroll.setPreferredSize(new Dimension(680, 380));
JOptionPane.showMessageDialog(this, scroll, titulo, JOptionPane.PLAIN_MESSAGE);
}
private void mostrarError(SQLException ex) {
JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
}
}
