package Vista;
import Modelo.Accesorio;
import Modelo.AccesorioDAO;
import Modelo.Cita;
import Modelo.CitaDAO;
import Modelo.DetalleFactura;
import Modelo.Factura;
import Modelo.FacturaDAO;
import Modelo.Paciente;
import Modelo.PacienteDAO;
import Modelo.Pago;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class FacturacionPanel extends JPanel {
// ---------- Paleta de colores ----------
private static final Color COLOR_FONDO = new Color(0xF4, 0xF6, 0xF9);
private static final Color COLOR_TARJETA = Color.WHITE;
private static final Color COLOR_BORDE = new Color(0xE1, 0xE5, 0xEA);
private static final Color COLOR_PRIMARIO = new Color(0x1E, 0x5F, 0x9E);
private static final Color COLOR_PRIMARIO_HOVER= new Color(0x18, 0x4E, 0x82);
private static final Color COLOR_SECUNDARIO = new Color(0xEE, 0xF2, 0xF7);
private static final Color COLOR_SECUNDARIO_HOVER = new Color(0xE0, 0xE7, 0xEF);
private static final Color COLOR_TEXTO = new Color(0x2C, 0x33, 0x3A);
private static final Color COLOR_TEXTO_SUAVE = new Color(0x6B, 0x74, 0x80);
private static final Color COLOR_TABLA_ALT = new Color(0xF8, 0xFA, 0xFC);
private static final Color COLOR_TABLA_SEL = new Color(0xDD, 0xEB, 0xF7);
private static final Color COLOR_ALERTA_TEXTO = new Color(0xB5, 0x4A, 0x0F);
private static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 18);
private static final Font FUENTE_SECCION = new Font("Segoe UI", Font.BOLD, 13);
private static final Font FUENTE_SUBSECCION = new Font("Segoe UI", Font.BOLD, 12);
private static final Font FUENTE_LABEL = new Font("Segoe UI", Font.PLAIN, 12);
private static final Font FUENTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 13);
private static final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 13);
private static final Font FUENTE_TABLA = new Font("Segoe UI", Font.PLAIN, 13);
private static final Font FUENTE_TABLA_HDR= new Font("Segoe UI", Font.BOLD, 13);
private static final Font FUENTE_TOTAL = new Font("Segoe UI", Font.BOLD, 13);
private static final DecimalFormat FMT_MONEDA = new DecimalFormat(
"RD$ #,##0.00", new DecimalFormatSymbols(new Locale("es", "DO")));
private final FacturaDAO facturaDAO = new FacturaDAO();
private final PacienteDAO pacienteDAO = new PacienteDAO();
private final CitaDAO citaDAO = new CitaDAO();
private final AccesorioDAO accesorioDAO = new AccesorioDAO();
// ---------- Tabla principal de facturas ----------
private final DefaultTableModel modelo = new DefaultTableModel(
new Object[]{"ID", "Paciente", "Fecha", "Subtotal", "Desc.", "Imp.", "Total", "Saldo", "Estado"}, 0) {
@Override public boolean isCellEditable(int row, int col) { return false; }
};
private final JTable tabla = new JTable(modelo);
// ---------- Formulario: nueva factura ----------
private final JComboBox<Paciente> cbPaciente = new JComboBox<>();
private final JComboBox<Cita> cbCita = new JComboBox<>();
private final JTextField txtDescripcionServicio = new JTextField();
private final JTextField txtCostoServicio = new JTextField();
private final JComboBox<Accesorio> cbAccesorio = new JComboBox<>();
private final JTextField txtCantidadProducto = new JTextField();
private final DefaultTableModel modeloItems = new DefaultTableModel(
new Object[]{"Tipo", "Descripción", "Cant.", "Precio", "Subtotal"}, 0) {
@Override public boolean isCellEditable(int row, int col) { return false; }
};
private final JTable tablaItems = new JTable(modeloItems);
private final List<DetalleFactura> itemsFactura = new ArrayList<>();
private final JTextField txtDescuentoPct = new JTextField("0");
private final JTextField txtImpuestoPct = new JTextField("0");
private final JComboBox<String> cbMetodoPagoFactura = new JComboBox<>(
new String[]{"Efectivo", "Tarjeta", "Transferencia", "Cheque"});
private final JLabel lblSubtotalPreview = crearLabelTotal();
private final JLabel lblDescuentoPreview = crearLabelTotal();
private final JLabel lblImpuestoPreview = crearLabelTotal();
private final JLabel lblTotalPreview = crearLabelTotal();
// ---------- Formulario: registrar pago ----------
private final JComboBox<String> cbMetodoPago = new JComboBox<>(
new String[]{"Efectivo", "Tarjeta", "Transferencia", "Cheque"});
private final JTextField txtMontoPago = new JTextField();
public FacturacionPanel() {
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
splitPane.setResizeWeight(1.0); // la tabla absorbe el espacio extra al redimensionar
splitPane.setContinuousLayout(true);
splitPane.setOneTouchExpandable(true);
add(splitPane, BorderLayout.CENTER);
cargarCombos();
cargarTabla();
actualizarTotales();
}
// ---------- Encabezado ----------
private JComponent construirEncabezado() {
JPanel panel = new JPanel(new BorderLayout());
panel.setOpaque(false);
panel.setBorder(new EmptyBorder(0, 0, 15, 0));
JLabel titulo = new JLabel("Facturación");
titulo.setFont(FUENTE_TITULO);
titulo.setForeground(COLOR_TEXTO);
JLabel subtitulo = new JLabel("Genera facturas, aplica impuestos/descuentos y registra pagos");
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
// ---------- Barra de acciones (comprobante y reportes) ----------
private JComponent construirBarraSuperior() {
JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
panel.setOpaque(false);
panel.setBorder(new EmptyBorder(0, 0, 15, 0));
JButton btnComprobante = crearBotonSecundario("🧾 Generar comprobante (PDF)");
btnComprobante.addActionListener(e -> generarComprobantePdf());
JButton btnExportarPdf = crearBotonSecundario("Exportar listado a PDF");
btnExportarPdf.addActionListener(e -> exportarListadoPdf());
JButton btnExportarExcel = crearBotonSecundario("Exportar listado a Excel");
btnExportarExcel.addActionListener(e -> exportarListadoExcel());
panel.add(btnComprobante);
panel.add(btnExportarPdf);
panel.add(btnExportarExcel);
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
boolean esEstado = col == 8;
boolean pendienteOParcial = esEstado && v != null
&& ("Pendiente".equals(v) || "Parcial".equals(v));
if (!sel) {
c.setBackground(row % 2 == 0 ? COLOR_TARJETA : COLOR_TABLA_ALT);
}
setForeground(pendienteOParcial && !sel ? COLOR_ALERTA_TEXTO : COLOR_TEXTO);
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
// ---------- Panel derecho (formularios), con scroll porque ahora es más alto ----------
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
JPanel contenedor = new JPanel() {
@Override
public Dimension getPreferredSize() {
Dimension d = super.getPreferredSize();
return new Dimension(360, d.height);
}
};
contenedor.setOpaque(false);
contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
contenedor.setBorder(new EmptyBorder(0, 10, 0, 0));
JComponent nuevaFactura = construirTarjeta("Nueva factura", construirFormNuevaFactura());
JComponent registrarPago = construirTarjeta("Registrar pago (factura seleccionada)", construirFormRegistrarPago());
nuevaFactura.setAlignmentX(Component.LEFT_ALIGNMENT);
registrarPago.setAlignmentX(Component.LEFT_ALIGNMENT);
contenedor.add(nuevaFactura);
contenedor.add(Box.createVerticalStrut(15));
contenedor.add(registrarPago);
contenedor.add(Box.createVerticalGlue());
return contenedor;
}
/** Envuelve un formulario en una "tarjeta" con título de sección, fondo blanco y borde suave. */
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
// ---------- Formulario "Nueva factura" ----------
private JComponent construirFormNuevaFactura() {
JPanel contenedor = new JPanel();
contenedor.setOpaque(false);
contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
contenedor.add(construirSeccionDatosGenerales());
contenedor.add(Box.createVerticalStrut(12));
contenedor.add(construirSeccionAgregarServicio());
contenedor.add(Box.createVerticalStrut(8));
contenedor.add(construirSeccionAgregarProducto());
contenedor.add(Box.createVerticalStrut(10));
contenedor.add(construirSeccionItems());
contenedor.add(Box.createVerticalStrut(12));
contenedor.add(construirSeccionTotales());
contenedor.add(Box.createVerticalStrut(12));
JButton btnCrear = crearBotonPrimario("Generar factura");
btnCrear.setAlignmentX(Component.LEFT_ALIGNMENT);
btnCrear.addActionListener(e -> crearFactura());
contenedor.add(btnCrear);
return contenedor;
}
private JComponent construirSeccionDatosGenerales() {
JPanel panel = new JPanel(new GridBagLayout());
panel.setOpaque(false);
panel.setAlignmentX(Component.LEFT_ALIGNMENT);
GridBagConstraints gbc = new GridBagConstraints();
gbc.fill = GridBagConstraints.HORIZONTAL;
gbc.weightx = 1;
gbc.gridx = 0;
estilizarCampo(cbPaciente);
estilizarCampo(cbCita);
cbCita.setRenderer(new javax.swing.DefaultListCellRenderer() {
@Override
public Component getListCellRendererComponent(JList<?> list, Object value, int index,
boolean isSelected, boolean cellHasFocus) {
super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
if (value == null) {
setText("Sin cita relacionada");
} else {
Cita c = (Cita) value;
setText("#" + c.getId() + " · " + c.getPacienteNombre() + " · " + c.getFecha() + " " + c.getHora());
}
return this;
}
});
cbCita.addActionListener(e -> sincronizarPacienteConCita());
gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0); panel.add(crearEtiqueta("Paciente"), gbc);
gbc.gridy = 1; gbc.insets = new Insets(0, 0, 10, 0); panel.add(cbPaciente, gbc);
gbc.gridy = 2; gbc.insets = new Insets(0, 0, 4, 0); panel.add(crearEtiqueta("Cita relacionada (opcional)"), gbc);
gbc.gridy = 3; gbc.insets = new Insets(0, 0, 0, 0); panel.add(cbCita, gbc);
return panel;
}
private JComponent construirSeccionAgregarServicio() {
JPanel panel = new JPanel(new GridBagLayout());
panel.setOpaque(false);
panel.setAlignmentX(Component.LEFT_ALIGNMENT);
panel.setBorder(new CompoundBorder(
BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE),
new EmptyBorder(10, 0, 0, 0)));
GridBagConstraints gbc = new GridBagConstraints();
gbc.fill = GridBagConstraints.HORIZONTAL;
gbc.weightx = 1;
gbc.gridx = 0;
estilizarCampo(txtDescripcionServicio);
estilizarCampo(txtCostoServicio);
gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 6, 0);
JLabel lblSeccion = new JLabel("Agregar servicio / tratamiento");
lblSeccion.setFont(FUENTE_SUBSECCION);
lblSeccion.setForeground(COLOR_TEXTO);
panel.add(lblSeccion, gbc);
gbc.gridwidth = 1;
gbc.gridy = 1; gbc.insets = new Insets(0, 0, 4, 0); panel.add(crearEtiqueta("Descripción"), gbc);
gbc.gridy = 2; gbc.insets = new Insets(0, 0, 6, 0); panel.add(txtDescripcionServicio, gbc);
gbc.gridy = 3; gbc.insets = new Insets(0, 0, 4, 0); panel.add(crearEtiqueta("Costo (RD$)"), gbc);
gbc.gridy = 4; gbc.insets = new Insets(0, 0, 6, 0); panel.add(txtCostoServicio, gbc);
JButton btnAgregar = crearBotonSecundario("+ Agregar servicio a la factura");
btnAgregar.addActionListener(e -> agregarServicio());
gbc.gridy = 5; gbc.insets = new Insets(0, 0, 0, 0); panel.add(btnAgregar, gbc);
return panel;
}
private JComponent construirSeccionAgregarProducto() {
JPanel panel = new JPanel(new GridBagLayout());
panel.setOpaque(false);
panel.setAlignmentX(Component.LEFT_ALIGNMENT);
panel.setBorder(new CompoundBorder(
BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE),
new EmptyBorder(10, 0, 0, 0)));
GridBagConstraints gbc = new GridBagConstraints();
gbc.fill = GridBagConstraints.HORIZONTAL;
gbc.weightx = 1;
gbc.gridx = 0;
estilizarCampo(cbAccesorio);
estilizarCampo(txtCantidadProducto);
gbc.gridy = 0; gbc.insets = new Insets(0, 0, 6, 0);
JLabel lblSeccion = new JLabel("Agregar producto de inventario");
lblSeccion.setFont(FUENTE_SUBSECCION);
lblSeccion.setForeground(COLOR_TEXTO);
panel.add(lblSeccion, gbc);
gbc.gridy = 1; gbc.insets = new Insets(0, 0, 4, 0); panel.add(crearEtiqueta("Producto (descuenta stock)"), gbc);
gbc.gridy = 2; gbc.insets = new Insets(0, 0, 6, 0); panel.add(cbAccesorio, gbc);
gbc.gridy = 3; gbc.insets = new Insets(0, 0, 4, 0); panel.add(crearEtiqueta("Cantidad"), gbc);
gbc.gridy = 4; gbc.insets = new Insets(0, 0, 6, 0); panel.add(txtCantidadProducto, gbc);
JButton btnAgregar = crearBotonSecundario("+ Agregar producto a la factura");
btnAgregar.addActionListener(e -> agregarProducto());
gbc.gridy = 5; gbc.insets = new Insets(0, 0, 0, 0); panel.add(btnAgregar, gbc);
return panel;
}
private JComponent construirSeccionItems() {
JPanel panel = new JPanel(new BorderLayout());
panel.setOpaque(false);
panel.setAlignmentX(Component.LEFT_ALIGNMENT);
tablaItems.setFont(FUENTE_TABLA);
tablaItems.setRowHeight(24);
tablaItems.setShowGrid(false);
tablaItems.setSelectionBackground(COLOR_TABLA_SEL);
tablaItems.getTableHeader().setFont(FUENTE_TABLA_HDR);
tablaItems.getTableHeader().setReorderingAllowed(false);
JScrollPane scroll = new JScrollPane(tablaItems);
scroll.setBorder(tarjetaBorde());
scroll.setPreferredSize(new Dimension(100, 110));
scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
JButton btnQuitar = crearBotonSecundario("Quitar línea seleccionada");
btnQuitar.addActionListener(e -> quitarItemSeleccionado());
JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
pie.setOpaque(false);
pie.add(btnQuitar);
panel.add(scroll, BorderLayout.CENTER);
panel.add(pie, BorderLayout.SOUTH);
return panel;
}
private JComponent construirSeccionTotales() {
JPanel panel = new JPanel(new GridBagLayout());
panel.setOpaque(false);
panel.setAlignmentX(Component.LEFT_ALIGNMENT);
panel.setBorder(new CompoundBorder(
BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE),
new EmptyBorder(10, 0, 0, 0)));
GridBagConstraints gbc = new GridBagConstraints();
gbc.fill = GridBagConstraints.HORIZONTAL;
gbc.weightx = 1;
gbc.gridx = 0;
estilizarCampo(txtDescuentoPct);
estilizarCampo(txtImpuestoPct);
estilizarCampo(cbMetodoPagoFactura);
DocumentListener recalcular = new DocumentListener() {
@Override public void insertUpdate(DocumentEvent e) { actualizarTotales(); }
@Override public void removeUpdate(DocumentEvent e) { actualizarTotales(); }
@Override public void changedUpdate(DocumentEvent e) { actualizarTotales(); }
};
txtDescuentoPct.getDocument().addDocumentListener(recalcular);
txtImpuestoPct.getDocument().addDocumentListener(recalcular);
gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 4, 0);
panel.add(crearEtiqueta("Descuento (%)"), gbc);
gbc.gridy = 1; gbc.insets = new Insets(0, 0, 8, 0);
panel.add(txtDescuentoPct, gbc);
gbc.gridy = 2; gbc.insets = new Insets(0, 0, 4, 0);
panel.add(crearEtiqueta("Impuesto / ITBIS (%)"), gbc);
gbc.gridy = 3; gbc.insets = new Insets(0, 0, 8, 0);
panel.add(txtImpuestoPct, gbc);
gbc.gridy = 4; gbc.insets = new Insets(0, 0, 4, 0);
panel.add(crearEtiqueta("Método de pago (al generar)"), gbc);
gbc.gridy = 5; gbc.insets = new Insets(0, 0, 10, 0);
panel.add(cbMetodoPagoFactura, gbc);
gbc.gridy = 6; gbc.insets = new Insets(0, 0, 2, 0);
panel.add(filaTotal("Subtotal:", lblSubtotalPreview), gbc);
gbc.gridy = 7; gbc.insets = new Insets(0, 0, 2, 0);
panel.add(filaTotal("Descuento:", lblDescuentoPreview), gbc);
gbc.gridy = 8; gbc.insets = new Insets(0, 0, 2, 0);
panel.add(filaTotal("Impuesto:", lblImpuestoPreview), gbc);
gbc.gridy = 9; gbc.insets = new Insets(6, 0, 0, 0);
panel.add(filaTotal("Total:", lblTotalPreview), gbc);
gbc.gridwidth = 1;
return panel;
}
private JComponent filaTotal(String etiqueta, JLabel valor) {
JPanel fila = new JPanel(new BorderLayout());
fila.setOpaque(false);
JLabel lbl = new JLabel(etiqueta);
lbl.setFont(FUENTE_LABEL);
lbl.setForeground(COLOR_TEXTO_SUAVE);
fila.add(lbl, BorderLayout.WEST);
fila.add(valor, BorderLayout.EAST);
return fila;
}
private static JLabel crearLabelTotal() {
JLabel lbl = new JLabel("RD$ 0.00");
lbl.setFont(FUENTE_TOTAL);
lbl.setForeground(new Color(0x2C, 0x33, 0x3A));
return lbl;
}
private JComponent construirFormRegistrarPago() {
JPanel panel = new JPanel(new GridBagLayout());
panel.setOpaque(false);
GridBagConstraints gbc = new GridBagConstraints();
gbc.fill = GridBagConstraints.HORIZONTAL;
gbc.weightx = 1;
gbc.gridx = 0;
estilizarCampo(txtMontoPago);
estilizarCampo(cbMetodoPago);
gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0); panel.add(crearEtiqueta("Monto (RD$)"), gbc);
gbc.gridy = 1; gbc.insets = new Insets(0, 0, 14, 0); panel.add(txtMontoPago, gbc);
gbc.gridy = 2; gbc.insets = new Insets(0, 0, 4, 0); panel.add(crearEtiqueta("Método de pago"), gbc);
gbc.gridy = 3; gbc.insets = new Insets(0, 0, 16, 0); panel.add(cbMetodoPago, gbc);
JButton btnPagar = crearBotonPrimario("Registrar pago");
btnPagar.addActionListener(e -> registrarPago());
gbc.gridy = 4; gbc.insets = new Insets(0, 0, 0, 0); panel.add(btnPagar, gbc);
return panel;
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
Border borde = new CompoundBorder(
BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
new EmptyBorder(6, 8, 6, 8)
);
if (campo instanceof JTextField) {
((JTextField) campo).setBorder(borde);
} else if (campo instanceof JComboBox) {
campo.setBorder(new CompoundBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1, true), new EmptyBorder(2, 4, 2, 4)));
}
campo.setPreferredSize(new Dimension(campo.getPreferredSize().width, 34));
campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
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
ButtonModel modeloBtn = boton.getModel();
boton.setBackground(modeloBtn.isRollover() ? COLOR_PRIMARIO_HOVER : COLOR_PRIMARIO);
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
new EmptyBorder(8, 12, 8, 12)
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
// ================= Carga de datos =================
private void cargarCombos() {
cbPaciente.removeAllItems();
for (Paciente p : pacienteDAO.listarTodos()) cbPaciente.addItem(p);
cbCita.removeAllItems();
cbCita.addItem(null); // "Sin cita relacionada"
try {
for (Cita c : citaDAO.listarTodas()) cbCita.addItem(c);
} catch (SQLException ex) {
mostrarError(ex);
}
cbAccesorio.removeAllItems();
try {
for (Accesorio a : accesorioDAO.listarTodos()) cbAccesorio.addItem(a);
} catch (SQLException ex) {
mostrarError(ex);
}
}
private void sincronizarPacienteConCita() {
Cita c = (Cita) cbCita.getSelectedItem();
if (c == null) return;
for (int i = 0; i < cbPaciente.getItemCount(); i++) {
Paciente p = cbPaciente.getItemAt(i);
if (p.getId() == c.getPacienteId()) {
cbPaciente.setSelectedIndex(i);
break;
}
}
}
private void cargarTabla() {
try {
List<Factura> lista = facturaDAO.listarTodas();
modelo.setRowCount(0);
for (Factura f : lista) {
modelo.addRow(new Object[]{
f.getId(), f.getPacienteNombre(), f.getFecha(),
FMT_MONEDA.format(f.getSubtotal()),
FMT_MONEDA.format(f.getDescuentoMonto()),
FMT_MONEDA.format(f.getImpuestoMonto()),
FMT_MONEDA.format(f.getTotal()),
FMT_MONEDA.format(f.getSaldoPendiente()),
f.getEstadoPago()
});
}
} catch (SQLException ex) {
mostrarError(ex);
}
}
// ================= Línea de servicio / producto =================
private void agregarServicio() {
String descripcion = txtDescripcionServicio.getText().trim();
if (descripcion.isEmpty()) {
JOptionPane.showMessageDialog(this, "Ingresa la descripción del servicio.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
BigDecimal costo;
try {
costo = new BigDecimal(txtCostoServicio.getText().trim());
} catch (NumberFormatException ex) {
JOptionPane.showMessageDialog(this, "El costo debe ser un número válido.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
if (costo.compareTo(BigDecimal.ZERO) <= 0) {
JOptionPane.showMessageDialog(this, "El costo debe ser mayor a cero.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
itemsFactura.add(new DetalleFactura(descripcion, 1, costo, null));
redibujarItems();
txtDescripcionServicio.setText("");
txtCostoServicio.setText("");
}
private void agregarProducto() {
Accesorio a = (Accesorio) cbAccesorio.getSelectedItem();
if (a == null) {
JOptionPane.showMessageDialog(this, "Selecciona un producto de inventario.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
int cantidad;
try {
cantidad = Integer.parseInt(txtCantidadProducto.getText().trim());
} catch (NumberFormatException ex) {
JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
if (cantidad <= 0) {
JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a cero.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
int yaAgregado = itemsFactura.stream()
.filter(it -> a.getId() == (it.getAccesorioId() == null ? -1 : it.getAccesorioId()))
.mapToInt(DetalleFactura::getCantidad).sum();
if (a.getStock() < cantidad + yaAgregado) {
JOptionPane.showMessageDialog(this,
"Stock insuficiente para \"" + a.getNombre() + "\". Disponible: " + a.getStock() + ".",
"Stock insuficiente", JOptionPane.WARNING_MESSAGE);
return;
}
itemsFactura.add(new DetalleFactura(a.getNombre(), cantidad, a.getPrecioVenta(), a.getId()));
redibujarItems();
txtCantidadProducto.setText("");
}
private void quitarItemSeleccionado() {
int fila = tablaItems.getSelectedRow();
if (fila < 0) {
JOptionPane.showMessageDialog(this, "Selecciona una línea de la lista de conceptos.", "Atención", JOptionPane.WARNING_MESSAGE);
return;
}
itemsFactura.remove(fila);
redibujarItems();
}
private void redibujarItems() {
modeloItems.setRowCount(0);
for (DetalleFactura it : itemsFactura) {
modeloItems.addRow(new Object[]{
it.esProductoInventario() ? "Producto" : "Servicio",
it.getDescripcion(),
it.getCantidad(),
FMT_MONEDA.format(it.getPrecioUnitario()),
FMT_MONEDA.format(it.getSubtotalLinea())
});
}
actualizarTotales();
}
private void actualizarTotales() {
BigDecimal subtotal = BigDecimal.ZERO;
for (DetalleFactura it : itemsFactura) subtotal = subtotal.add(it.getSubtotalLinea());
BigDecimal descPct = parsePorcentaje(txtDescuentoPct.getText());
BigDecimal impPct = parsePorcentaje(txtImpuestoPct.getText());
BigDecimal descuentoMonto = subtotal.multiply(descPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
BigDecimal base = subtotal.subtract(descuentoMonto);
BigDecimal impuestoMonto = base.multiply(impPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
BigDecimal total = base.add(impuestoMonto);
lblSubtotalPreview.setText(FMT_MONEDA.format(subtotal));
lblDescuentoPreview.setText("- " + FMT_MONEDA.format(descuentoMonto));
lblImpuestoPreview.setText(FMT_MONEDA.format(impuestoMonto));
lblTotalPreview.setText(FMT_MONEDA.format(total));
}
private BigDecimal parsePorcentaje(String texto) {
if (texto == null || texto.trim().isEmpty()) return BigDecimal.ZERO;
try {
BigDecimal v = new BigDecimal(texto.trim());
if (v.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
if (v.compareTo(BigDecimal.valueOf(100)) > 0) return BigDecimal.valueOf(100);
return v;
} catch (NumberFormatException ex) {
return BigDecimal.ZERO;
}
}
// ================= Crear factura / registrar pago =================
private void crearFactura() {
Paciente p = (Paciente) cbPaciente.getSelectedItem();
if (p == null) {
JOptionPane.showMessageDialog(this, "Selecciona un paciente.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
if (itemsFactura.isEmpty()) {
JOptionPane.showMessageDialog(this,
"Agrega al menos un servicio o producto antes de generar la factura.",
"Validación", JOptionPane.WARNING_MESSAGE);
return;
}
BigDecimal descPct = parsePorcentaje(txtDescuentoPct.getText());
BigDecimal impPct = parsePorcentaje(txtImpuestoPct.getText());
Cita citaSeleccionada = (Cita) cbCita.getSelectedItem();
Integer citaId = citaSeleccionada == null ? null : citaSeleccionada.getId();
String metodoPago = (String) cbMetodoPagoFactura.getSelectedItem();
try {
facturaDAO.crear(p.getId(), citaId, itemsFactura, descPct, impPct, metodoPago);
itemsFactura.clear();
redibujarItems();
txtDescuentoPct.setText("0");
txtImpuestoPct.setText("0");
cbCita.setSelectedIndex(0);
cargarCombos(); // refresca stock disponible en el combo de productos
cargarTabla();
JOptionPane.showMessageDialog(this, "Factura generada correctamente.", "Listo", JOptionPane.INFORMATION_MESSAGE);
} catch (SQLException ex) {
mostrarError(ex);
}
}
private void registrarPago() {
int fila = tabla.getSelectedRow();
if (fila < 0) {
JOptionPane.showMessageDialog(this, "Selecciona una factura de la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
return;
}
int facturaId = (int) modelo.getValueAt(fila, 0);
if (txtMontoPago.getText().trim().isEmpty()) {
JOptionPane.showMessageDialog(this, "Ingresa el monto a pagar.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
BigDecimal monto;
try {
monto = new BigDecimal(txtMontoPago.getText().trim());
} catch (NumberFormatException ex) {
JOptionPane.showMessageDialog(this, "El monto debe ser un número válido.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
if (monto.compareTo(BigDecimal.ZERO) <= 0) {
JOptionPane.showMessageDialog(this, "El monto debe ser mayor a cero.", "Validación", JOptionPane.WARNING_MESSAGE);
return;
}
try {
Factura factura = facturaDAO.obtenerPorId(facturaId);
if (factura != null && monto.compareTo(factura.getSaldoPendiente()) > 0) {
int opcion = JOptionPane.showConfirmDialog(this,
"El monto ingresado (" + FMT_MONEDA.format(monto) + ") supera el saldo pendiente ("
+ FMT_MONEDA.format(factura.getSaldoPendiente()) + ").\n¿Deseas registrarlo de todas formas?",
"Monto mayor al saldo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
if (opcion != JOptionPane.YES_OPTION) return;
}
facturaDAO.registrarPago(facturaId, monto, (String) cbMetodoPago.getSelectedItem());
txtMontoPago.setText("");
cargarTabla();
} catch (SQLException ex) {
mostrarError(ex);
}
}
// ================= Comprobante de factura (OpenPDF) =================
private void generarComprobantePdf() {
int fila = tabla.getSelectedRow();
if (fila < 0) {
JOptionPane.showMessageDialog(this, "Selecciona una factura de la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
return;
}
int facturaId = (int) modelo.getValueAt(fila, 0);
try {
Factura f = facturaDAO.obtenerPorId(facturaId);
if (f == null) {
JOptionPane.showMessageDialog(this, "No se encontró la factura seleccionada.", "Error", JOptionPane.ERROR_MESSAGE);
return;
}
List<DetalleFactura> detalle = facturaDAO.obtenerDetalle(facturaId);
List<Pago> pagos = facturaDAO.obtenerPagos(facturaId);
File destino = elegirDestino("factura_" + facturaId + ".pdf", "Archivos PDF (*.pdf)", "pdf");
if (destino == null) return;
Document documento = new Document(PageSize.A4, 40, 40, 50, 50);
PdfWriter.getInstance(documento, new FileOutputStream(destino));
documento.open();
com.lowagie.text.Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(0x1E, 0x5F, 0x9E));
com.lowagie.text.Font fSubt = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
com.lowagie.text.Font fEtiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
com.lowagie.text.Font fValor = FontFactory.getFont(FontFactory.HELVETICA, 11);
com.lowagie.text.Font fEncabezadoTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
com.lowagie.text.Font fCelda = FontFactory.getFont(FontFactory.HELVETICA, 10);
com.lowagie.text.Font fTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
com.lowagie.text.Font fPie = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);
Paragraph titulo = new Paragraph("Clínica Dental — Comprobante de factura #" + f.getId(), fTitulo);
titulo.setSpacingAfter(2);
documento.add(titulo);
PdfPTable lineaAcento = new PdfPTable(1);
lineaAcento.setWidthPercentage(100);
PdfPCell celdaLinea = new PdfPCell();
celdaLinea.setFixedHeight(2f);
celdaLinea.setBackgroundColor(new Color(0x1E, 0x5F, 0x9E));
celdaLinea.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
lineaAcento.addCell(celdaLinea);
lineaAcento.setSpacingAfter(10);
documento.add(lineaAcento);
DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
Paragraph datos = new Paragraph();
datos.add(new Phrase("Paciente: ", fEtiqueta));
datos.add(new Phrase(f.getPacienteNombre() + "\n", fValor));
datos.add(new Phrase("Fecha: ", fEtiqueta));
datos.add(new Phrase(f.getFecha().format(fmtFecha) + "\n", fValor));
if (f.getCitaId() != null) {
datos.add(new Phrase("Cita relacionada: ", fEtiqueta));
datos.add(new Phrase("#" + f.getCitaId() + "\n", fValor));
}
datos.add(new Phrase("Estado de pago: ", fEtiqueta));
datos.add(new Phrase(f.getEstadoPago() + "\n", fValor));
if (f.getMetodoPagoPreferido() != null) {
datos.add(new Phrase("Método de pago: ", fEtiqueta));
datos.add(new Phrase(f.getMetodoPagoPreferido() + "\n", fValor));
}
datos.setSpacingAfter(14);
documento.add(datos);
PdfPTable tablaDet = new PdfPTable(4);
tablaDet.setWidthPercentage(100);
tablaDet.setWidths(new float[]{3.5f, 1f, 1.3f, 1.3f});
String[] encabezados = {"Concepto", "Cant.", "Precio", "Subtotal"};
for (int i = 0; i < encabezados.length; i++) {
PdfPCell celdaHeader = new PdfPCell(new Phrase(encabezados[i], fEncabezadoTabla));
celdaHeader.setBackgroundColor(new Color(0x1E, 0x5F, 0x9E));
celdaHeader.setPadding(6);
celdaHeader.setHorizontalAlignment(i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
tablaDet.addCell(celdaHeader);
}
int i = 0;
for (DetalleFactura d : detalle) {
Color fondo = i % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFB);
tablaDet.addCell(celda(d.getDescripcion(), fCelda, Element.ALIGN_LEFT, fondo));
tablaDet.addCell(celda(String.valueOf(d.getCantidad()), fCelda, Element.ALIGN_RIGHT, fondo));
tablaDet.addCell(celda(FMT_MONEDA.format(d.getPrecioUnitario()), fCelda, Element.ALIGN_RIGHT, fondo));
tablaDet.addCell(celda(FMT_MONEDA.format(d.getSubtotalLinea()), fCelda, Element.ALIGN_RIGHT, fondo));
i++;
}
documento.add(tablaDet);
Paragraph espacio = new Paragraph(" ");
espacio.setSpacingAfter(4);
documento.add(espacio);
PdfPTable tablaTotales = new PdfPTable(2);
tablaTotales.setWidthPercentage(50);
tablaTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
agregarFilaTotal(tablaTotales, "Subtotal", FMT_MONEDA.format(f.getSubtotal()), fEtiqueta, fValor, false);
agregarFilaTotal(tablaTotales, "Descuento (" + f.getDescuentoPorcentaje() + "%)",
"- " + FMT_MONEDA.format(f.getDescuentoMonto()), fEtiqueta, fValor, false);
agregarFilaTotal(tablaTotales, "Impuesto (" + f.getImpuestoPorcentaje() + "%)",
FMT_MONEDA.format(f.getImpuestoMonto()), fEtiqueta, fValor, false);
agregarFilaTotal(tablaTotales, "TOTAL", FMT_MONEDA.format(f.getTotal()), fTotal, fTotal, true);
agregarFilaTotal(tablaTotales, "Pagado", FMT_MONEDA.format(f.getPagado()), fEtiqueta, fValor, false);
agregarFilaTotal(tablaTotales, "Saldo pendiente", FMT_MONEDA.format(f.getSaldoPendiente()), fEtiqueta, fValor, false);
documento.add(tablaTotales);
if (!pagos.isEmpty()) {
Paragraph tituloPagos = new Paragraph("Pagos registrados", fEtiqueta);
tituloPagos.setSpacingBefore(16);
tituloPagos.setSpacingAfter(6);
documento.add(tituloPagos);
PdfPTable tablaPagos = new PdfPTable(3);
tablaPagos.setWidthPercentage(100);
String[] encPagos = {"Fecha", "Método", "Monto"};
for (int c = 0; c < encPagos.length; c++) {
PdfPCell ch = new PdfPCell(new Phrase(encPagos[c], fEncabezadoTabla));
ch.setBackgroundColor(new Color(0x1E, 0x5F, 0x9E));
ch.setPadding(6);
ch.setHorizontalAlignment(c == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
tablaPagos.addCell(ch);
}
int j = 0;
for (Pago pago : pagos) {
Color fondo = j % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFB);
tablaPagos.addCell(celda(pago.getFecha().format(fmtFecha), fCelda, Element.ALIGN_LEFT, fondo));
tablaPagos.addCell(celda(pago.getMetodo(), fCelda, Element.ALIGN_RIGHT, fondo));
tablaPagos.addCell(celda(FMT_MONEDA.format(pago.getMonto()), fCelda, Element.ALIGN_RIGHT, fondo));
j++;
}
documento.add(tablaPagos);
}
Paragraph pie = new Paragraph("Clínica Dental — Comprobante generado automáticamente por el sistema.", fPie);
pie.setSpacingBefore(16);
pie.setAlignment(Element.ALIGN_CENTER);
documento.add(pie);
documento.close();
mostrarExitoYAbrir(destino, "Comprobante");
} catch (SQLException ex) {
mostrarError(ex);
} catch (Exception ex) {
JOptionPane.showMessageDialog(this, "No se pudo generar el comprobante:\n" + ex.getMessage(),
"Error de exportación", JOptionPane.ERROR_MESSAGE);
}
}
private PdfPCell celda(String texto, com.lowagie.text.Font fuente, int alineacion, Color fondo) {
PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente));
c.setPadding(6);
c.setHorizontalAlignment(alineacion);
c.setBackgroundColor(fondo);
return c;
}
private void agregarFilaTotal(PdfPTable tabla, String etiqueta, String valor,
com.lowagie.text.Font fEtiqueta, com.lowagie.text.Font fValor, boolean resaltado) {
Color fondo = resaltado ? new Color(0xE6, 0xEF, 0xF6) : Color.WHITE;
PdfPCell c1 = new PdfPCell(new Phrase(etiqueta, fEtiqueta));
c1.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
c1.setPadding(4);
c1.setBackgroundColor(fondo);
PdfPCell c2 = new PdfPCell(new Phrase(valor, fValor));
c2.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
c2.setPadding(4);
c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
c2.setBackgroundColor(fondo);
tabla.addCell(c1);
tabla.addCell(c2);
}
// ================= Exportación del listado (reportes) =================
private void exportarListadoPdf() {
if (modelo.getRowCount() == 0) {
JOptionPane.showMessageDialog(this, "No hay facturas para exportar.", "Atención", JOptionPane.WARNING_MESSAGE);
return;
}
File destino = elegirDestino("listado_facturas.pdf", "Archivos PDF (*.pdf)", "pdf");
if (destino == null) return;
try {
Document documento = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
PdfWriter.getInstance(documento, new FileOutputStream(destino));
documento.open();
com.lowagie.text.Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(0x1E, 0x5F, 0x9E));
com.lowagie.text.Font fEncabezadoTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
com.lowagie.text.Font fCelda = FontFactory.getFont(FontFactory.HELVETICA, 9);
Paragraph titulo = new Paragraph("Clínica Dental — Listado de facturas", fTitulo);
titulo.setSpacingAfter(10);
documento.add(titulo);
PdfPTable t = new PdfPTable(modelo.getColumnCount());
t.setWidthPercentage(100);
for (int c = 0; c < modelo.getColumnCount(); c++) {
PdfPCell ch = new PdfPCell(new Phrase(modelo.getColumnName(c), fEncabezadoTabla));
ch.setBackgroundColor(new Color(0x1E, 0x5F, 0x9E));
ch.setPadding(5);
t.addCell(ch);
}
for (int r = 0; r < modelo.getRowCount(); r++) {
Color fondo = r % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFB);
for (int c = 0; c < modelo.getColumnCount(); c++) {
Object v = modelo.getValueAt(r, c);
t.addCell(celda(v == null ? "" : v.toString(), fCelda,
c == 0 || c >= 3 ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT, fondo));
}
}
documento.add(t);
documento.close();
mostrarExitoYAbrir(destino, "Listado");
} catch (Exception ex) {
JOptionPane.showMessageDialog(this, "No se pudo generar el PDF:\n" + ex.getMessage(),
"Error de exportación", JOptionPane.ERROR_MESSAGE);
}
}
private void exportarListadoExcel() {
if (modelo.getRowCount() == 0) {
JOptionPane.showMessageDialog(this, "No hay facturas para exportar.", "Atención", JOptionPane.WARNING_MESSAGE);
return;
}
File destino = elegirDestino("listado_facturas.xlsx", "Archivos Excel (*.xlsx)", "xlsx");
if (destino == null) return;
try (XSSFWorkbook libro = new XSSFWorkbook()) {
Sheet hoja = libro.createSheet("Facturas");
XSSFCellStyle estiloHeader = libro.createCellStyle();
estiloHeader.setFillForegroundColor(new XSSFColor(new Color(0x1E, 0x5F, 0x9E), null));
estiloHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
XSSFFont fuenteHeader = libro.createFont();
fuenteHeader.setColor(new XSSFColor(Color.WHITE, null));
fuenteHeader.setBold(true);
estiloHeader.setFont(fuenteHeader);
estiloHeader.setAlignment(HorizontalAlignment.CENTER);
estiloHeader.setBorderBottom(BorderStyle.THIN);
Row filaHeader = hoja.createRow(0);
for (int c = 0; c < modelo.getColumnCount(); c++) {
Cell celda = filaHeader.createCell(c);
celda.setCellValue(modelo.getColumnName(c));
celda.setCellStyle(estiloHeader);
}
CellStyle estiloCeldaAlt = libro.createCellStyle();
estiloCeldaAlt.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
estiloCeldaAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);
for (int r = 0; r < modelo.getRowCount(); r++) {
Row fila = hoja.createRow(r + 1);
for (int c = 0; c < modelo.getColumnCount(); c++) {
Object v = modelo.getValueAt(r, c);
Cell celda = fila.createCell(c);
celda.setCellValue(v == null ? "" : v.toString());
if (r % 2 != 0) celda.setCellStyle(estiloCeldaAlt);
}
}
for (int c = 0; c < modelo.getColumnCount(); c++) hoja.autoSizeColumn(c);
try (FileOutputStream out = new FileOutputStream(destino)) {
libro.write(out);
}
mostrarExitoYAbrir(destino, "Listado");
} catch (IOException ex) {
JOptionPane.showMessageDialog(this, "No se pudo generar el Excel:\n" + ex.getMessage(),
"Error de exportación", JOptionPane.ERROR_MESSAGE);
}
}
private File elegirDestino(String nombreSugerido, String descripcionFiltro, String extension) {
JFileChooser selector = new JFileChooser();
selector.setDialogTitle("Guardar archivo");
selector.setSelectedFile(new File(nombreSugerido));
selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(descripcionFiltro, extension));
int opcion = selector.showSaveDialog(this);
if (opcion != JFileChooser.APPROVE_OPTION) return null;
File archivo = selector.getSelectedFile();
if (!archivo.getName().toLowerCase().endsWith("." + extension)) {
archivo = new File(archivo.getParentFile(), archivo.getName() + "." + extension);
}
return archivo;
}
private void mostrarExitoYAbrir(File archivo, String tipo) {
int opcion = JOptionPane.showConfirmDialog(this,
tipo + " exportado correctamente en:\n" + archivo.getAbsolutePath() + "\n\n¿Deseas abrirlo ahora?",
"Exportación completada", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
if (opcion == JOptionPane.YES_OPTION) {
try {
Desktop.getDesktop().open(archivo);
} catch (IOException | UnsupportedOperationException ex) {
JOptionPane.showMessageDialog(this, "No se pudo abrir el archivo automáticamente.",
"Atención", JOptionPane.WARNING_MESSAGE);
}
}
}
private void mostrarError(SQLException ex) {
JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
}
}
