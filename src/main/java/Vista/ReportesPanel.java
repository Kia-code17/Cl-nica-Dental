package Vista;

import Modelo.Conexion;

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
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Módulo de reportes: estadísticas generales calculadas con consultas de
 * agregación sobre la BD, con exportación a PDF (OpenPDF) y Excel (Apache POI).
 */
public class ReportesPanel extends JPanel {

    private final JLabel lblPacientes = new JLabel("-");
    private final JLabel lblDoctores = new JLabel("-");
    private final JLabel lblCitasHoy = new JLabel("-");
    private final JLabel lblFacturado = new JLabel("-");
    private final JLabel lblPendiente = new JLabel("-");

    private final DefaultTableModel modeloTopTratamientos = new DefaultTableModel(
        new Object[]{"Tratamiento", "Cantidad", "Total (RD$)"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tablaTratamientos = new JTable(modeloTopTratamientos);

    // Formateador de moneda propio (evita depender de un Locale "es_DO" que
    // no siempre existe en el JDK): separador de miles y 2 decimales fijos.
    private static final DecimalFormat FMT_MONEDA = new DecimalFormat(
        "#,##0.00", new DecimalFormatSymbols(Locale.US));

    // ---------- Paleta y tipografía (consistente con el resto de la app) ----------
    private static final Color COLOR_FONDO        = new Color(0xF5, 0xF7, 0xFA);
    private static final Color COLOR_PANEL         = Color.WHITE;
    private static final Color COLOR_BORDE         = new Color(0xE0, 0xE4, 0xEA);
    private static final Color COLOR_PRIMARIO      = new Color(0x1F, 0x6F, 0x8B);
    private static final Color COLOR_PRIMARIO_OSC  = new Color(0x14, 0x4F, 0x63);
    private static final Color COLOR_TEXTO         = new Color(0x2E, 0x33, 0x38);
    private static final Color COLOR_TEXTO_SUAVE   = new Color(0x6B, 0x74, 0x7C);
    private static final Color COLOR_FILA_ALT      = new Color(0xF7, 0xF9, 0xFB);
    private static final Color COLOR_PDF           = new Color(0xC0, 0x39, 0x2B);
    private static final Color COLOR_XLS           = new Color(0x21, 0x7A, 0x46);

    private static final java.awt.Font F_TITULO   = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18);
    private static final java.awt.Font F_SUBT     = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12);
    private static final java.awt.Font F_LABEL    = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12);
    private static final java.awt.Font F_VALOR    = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22);
    private static final java.awt.Font F_TABLA    = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12);
    private static final java.awt.Font F_TABLA_HD = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12);
    private static final java.awt.Font F_BOTON    = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13);

    // Totales en memoria para poder imprimir una fila de "Total" en los
    // reportes exportados sin tener que volver a consultar la BD.
    private BigDecimal totalTratamientos = BigDecimal.ZERO;
    private int cantidadTotalTratamientos = 0;

    public ReportesPanel() {
        setLayout(new BorderLayout());
        setBackground(COLOR_FONDO);
        setBorder(new EmptyBorder(18, 20, 18, 20));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirTarjetaTabla(), BorderLayout.CENTER);
        add(construirBarraAcciones(), BorderLayout.SOUTH);

        cargarDatos();
    }

    // ================= ENCABEZADO =================

    private JPanel construirEncabezado() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titulo = new JLabel("Reportes");
        titulo.setFont(F_TITULO);
        titulo.setForeground(COLOR_TEXTO);

        JLabel subtitulo = new JLabel("Resumen general de la clínica y tratamientos más realizados");
        subtitulo.setFont(F_SUBT);
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
        contenedor.add(construirTarjetasIndicadores(), BorderLayout.SOUTH);
        return contenedor;
    }

    private JPanel construirTarjetasIndicadores() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 12, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 0, 0, 0));
        panel.add(tarjetaIndicador("Pacientes activos", lblPacientes));
        panel.add(tarjetaIndicador("Doctores activos", lblDoctores));
        panel.add(tarjetaIndicador("Citas de hoy", lblCitasHoy));
        panel.add(tarjetaIndicador("Total facturado", lblFacturado));
        panel.add(tarjetaIndicador("Saldo pendiente", lblPendiente));
        return panel;
    }

    private JPanel tarjetaIndicador(String titulo, JLabel valor) {
        JPanel tarjeta = new TarjetaRedondeada();
        tarjeta.setLayout(new BorderLayout(0, 6));
        tarjeta.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(F_LABEL);
        lblTitulo.setForeground(COLOR_TEXTO_SUAVE);

        valor.setFont(F_VALOR);
        valor.setForeground(COLOR_PRIMARIO_OSC);
        valor.setHorizontalAlignment(SwingConstants.LEFT);

        tarjeta.add(lblTitulo, BorderLayout.NORTH);
        tarjeta.add(valor, BorderLayout.CENTER);
        return tarjeta;
    }

    // ================= TABLA =================

    private JPanel construirTarjetaTabla() {
        JPanel tarjeta = new TarjetaRedondeada();
        tarjeta.setLayout(new BorderLayout(0, 10));
        tarjeta.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel titulo = new JLabel("Tratamientos más realizados");
        titulo.setFont(F_LABEL);
        titulo.setForeground(COLOR_TEXTO);

        estilizarTabla();
        JScrollPane scroll = new JScrollPane(tablaTratamientos);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);

        tarjeta.add(titulo, BorderLayout.NORTH);
        tarjeta.add(scroll, BorderLayout.CENTER);
        return tarjeta;
    }

    private void estilizarTabla() {
        tablaTratamientos.setFont(F_TABLA);
        tablaTratamientos.setRowHeight(30);
        tablaTratamientos.setShowGrid(false);
        tablaTratamientos.setIntercellSpacing(new Dimension(0, 0));
        tablaTratamientos.setSelectionBackground(new Color(0xDD, 0xEE, 0xF3));
        tablaTratamientos.setFillsViewportHeight(true);

        JTableHeader header = tablaTratamientos.getTableHeader();
        header.setFont(F_TABLA_HD);
        header.setPreferredSize(new Dimension(0, 34));
        header.setReorderingAllowed(false);
        // NOTA: header.setBackground()/setForeground() NO funcionan de forma
        // fiable con el Look & Feel de Windows (igual que pasaba con los
        // JButton): el header se sigue pintando con su fondo por defecto
        // (blanco) y el texto blanco queda invisible sobre blanco. Por eso
        // se pinta a mano con un renderer propio, igual que se hizo con los
        // botones de exportar.
        header.setOpaque(true);
        header.setBackground(COLOR_PRIMARIO);
        header.setDefaultRenderer(new HeaderRendererPintado());

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : COLOR_FILA_ALT);
                c.setForeground(COLOR_TEXTO);
                setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.RIGHT);
                return c;
            }
        };
        for (int i = 0; i < tablaTratamientos.getColumnCount(); i++) {
            tablaTratamientos.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    /**
     * Renderer del encabezado de tabla que se pinta a mano con Graphics2D
     * (fondo azul + texto blanco), en vez de depender de
     * setBackground/setForeground del JTableHeader, que el Look & Feel de
     * Windows ignora. Es el mismo enfoque que se usa en botonAccion().
     */
    private class HeaderRendererPintado extends JLabel implements TableCellRenderer {
        HeaderRendererPintado() {
            setOpaque(false);
            setFont(F_TABLA_HD);
            setForeground(Color.WHITE);
            setBorder(new EmptyBorder(0, 10, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.RIGHT);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = crearG2(g);
            g2.setColor(COLOR_PRIMARIO);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================= BARRA DE ACCIONES / EXPORTACIÓN =================

    private JPanel construirBarraAcciones() {
        JPanel tarjeta = new TarjetaRedondeada();
        tarjeta.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        tarjeta.setBorder(new EmptyBorder(10, 16, 10, 16));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(14, 0, 0, 0), tarjeta.getBorder()));

        JButton btnActualizar = botonSecundario("Actualizar reportes");
        btnActualizar.addActionListener(e -> cargarDatos());

        JButton btnExportarPdf = botonAccion("Exportar a PDF", COLOR_PDF, COLOR_PDF.darker());
        btnExportarPdf.addActionListener(e -> exportarPdf());

        JButton btnExportarExcel = botonAccion("Exportar a Excel", COLOR_XLS, COLOR_XLS.darker());
        btnExportarExcel.addActionListener(e -> exportarExcel());

        tarjeta.add(btnActualizar);
        tarjeta.add(Box.createHorizontalStrut(4));
        tarjeta.add(btnExportarPdf);
        tarjeta.add(btnExportarExcel);
        return tarjeta;
    }

    /**
     * Botón de contorno (fondo blanco, texto y borde de color). Se pinta a
     * mano con Graphics2D en vez de depender de setBackground/setOpaque,
     * porque el Look & Feel de Windows ignora esos valores en JButton.
     */
    private JButton botonSecundario(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = crearG2(g);
                g2.setColor(getModel().isRollover() ? new Color(0xEE, 0xF4, 0xF7) : Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(COLOR_PRIMARIO);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth() - 1.6f, getHeight() - 1.6f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        prepararBotonBase(btn, COLOR_PRIMARIO);
        return btn;
    }

    /**
     * Botón sólido de color (fondo relleno + texto blanco). Igual que
     * botonSecundario, se pinta a mano: con setBackground/setOpaque el
     * fondo se queda blanco en Windows y el texto blanco queda invisible
     * sobre fondo blanco (el bug que se reportó en Exportar PDF/Excel).
     */
    private JButton botonAccion(String texto, Color color, Color colorHover) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = crearG2(g);
                g2.setColor(getModel().isRollover() || getModel().isPressed() ? colorHover : color);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        prepararBotonBase(btn, Color.WHITE);
        return btn;
    }

    private void prepararBotonBase(JButton btn, Color colorTexto) {
        btn.setFont(F_BOTON);
        btn.setForeground(colorTexto);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static Graphics2D crearG2(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g2;
    }

    // ================= CARGA DE DATOS =================

    private void cargarDatos() {
        Conexion cn = new Conexion();
        try (Connection con = cn.getConnetion()) {
            lblPacientes.setText(contar(con, "SELECT COUNT(*) FROM pacientes WHERE activo = TRUE"));
            lblDoctores.setText(contar(con, "SELECT COUNT(*) FROM doctores WHERE activo = TRUE"));
            lblCitasHoy.setText(contar(con, "SELECT COUNT(*) FROM citas WHERE fecha = CURDATE()"));
            lblFacturado.setText("RD$ " + FMT_MONEDA.format(
                contarDecimal(con, "SELECT COALESCE(SUM(total),0) FROM facturas")));
            lblPendiente.setText("RD$ " + FMT_MONEDA.format(
                contarDecimal(con,
                    "SELECT COALESCE(SUM(f.total),0) - COALESCE((SELECT SUM(p.monto) FROM pagos p "
                    + "JOIN facturas f2 ON f2.id = p.factura_id),0) FROM facturas f")));

            modeloTopTratamientos.setRowCount(0);
            totalTratamientos = BigDecimal.ZERO;
            cantidadTotalTratamientos = 0;
            String sql = "SELECT descripcion, COUNT(*) AS veces, SUM(costo) AS total "
                    + "FROM tratamientos GROUP BY descripcion ORDER BY veces DESC LIMIT 10";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int veces = rs.getInt("veces");
                    BigDecimal total = rs.getBigDecimal("total").setScale(2, RoundingMode.HALF_UP);
                    modeloTopTratamientos.addRow(new Object[]{
                        rs.getString("descripcion"), veces, total
                    });
                    cantidadTotalTratamientos += veces;
                    totalTratamientos = totalTratamientos.add(total);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String contar(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString(1);
        }
        return "0";
    }

    private BigDecimal contarDecimal(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal v = rs.getBigDecimal(1);
                return v != null ? v : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    // ================= EXPORTACIÓN A PDF (OpenPDF) =================

    private void exportarPdf() {
        File destino = elegirDestino("reporte_clinica.pdf", "Archivos PDF (*.pdf)", "pdf");
        if (destino == null) return;

        try {
            Document documento = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(documento, new FileOutputStream(destino));
            documento.open();

            com.lowagie.text.Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new java.awt.Color(0x1F, 0x6F, 0x8B));
            com.lowagie.text.Font fSubt = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.GRAY);
            com.lowagie.text.Font fEtiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            com.lowagie.text.Font fValor = FontFactory.getFont(FontFactory.HELVETICA, 11);
            com.lowagie.text.Font fEncabezadoTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, java.awt.Color.WHITE);
            com.lowagie.text.Font fCelda = FontFactory.getFont(FontFactory.HELVETICA, 10);
            com.lowagie.text.Font fTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            com.lowagie.text.Font fPie = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, java.awt.Color.GRAY);

            // --- Encabezado con línea de acento debajo del título ---
            Paragraph titulo = new Paragraph("Reporte General - Clínica Dental", fTitulo);
            titulo.setSpacingAfter(2);
            documento.add(titulo);

            PdfPTable lineaAcento = new PdfPTable(1);
            lineaAcento.setWidthPercentage(100);
            PdfPCell celdaLinea = new PdfPCell();
            celdaLinea.setFixedHeight(2f);
            celdaLinea.setBackgroundColor(new java.awt.Color(0x1F, 0x6F, 0x8B));
            celdaLinea.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            lineaAcento.addCell(celdaLinea);
            lineaAcento.setSpacingAfter(6);
            documento.add(lineaAcento);

            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph subt = new Paragraph("Generado el " + fecha, fSubt);
            subt.setSpacingAfter(18);
            documento.add(subt);

            // --- Indicadores clave ---
            PdfPTable tablaIndicadores = new PdfPTable(5);
            tablaIndicadores.setWidthPercentage(100);
            String[][] indicadores = {
                {"Pacientes activos", lblPacientes.getText()},
                {"Doctores activos", lblDoctores.getText()},
                {"Citas de hoy", lblCitasHoy.getText()},
                {"Total facturado", lblFacturado.getText()},
                {"Saldo pendiente", lblPendiente.getText()}
            };
            for (String[] ind : indicadores) {
                PdfPCell celda = new PdfPCell();
                celda.setPadding(8);
                celda.setBorderColor(new java.awt.Color(0xE0, 0xE4, 0xEA));
                celda.setBackgroundColor(new java.awt.Color(0xF7, 0xF9, 0xFB));
                Paragraph contenido = new Paragraph();
                contenido.add(new Phrase(ind[0] + "\n", fEtiqueta));
                contenido.add(new Phrase(ind[1], fValor));
                celda.addElement(contenido);
                tablaIndicadores.addCell(celda);
            }
            documento.add(tablaIndicadores);

            Paragraph espacio = new Paragraph(" ");
            espacio.setSpacingAfter(10);
            documento.add(espacio);

            Paragraph tituloTabla = new Paragraph("Tratamientos más realizados", fEtiqueta);
            tituloTabla.setSpacingAfter(8);
            documento.add(tituloTabla);

            PdfPTable tabla = new PdfPTable(3);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{3f, 1.2f, 1.5f});

            String[] encabezados = {"Tratamiento", "Cantidad", "Total (RD$)"};
            for (int i = 0; i < encabezados.length; i++) {
                PdfPCell celdaHeader = new PdfPCell(new Phrase(encabezados[i], fEncabezadoTabla));
                celdaHeader.setBackgroundColor(new java.awt.Color(0x1F, 0x6F, 0x8B));
                celdaHeader.setPadding(6);
                celdaHeader.setHorizontalAlignment(i == 0
                    ? com.lowagie.text.Element.ALIGN_LEFT : com.lowagie.text.Element.ALIGN_RIGHT);
                tabla.addCell(celdaHeader);
            }

            for (int i = 0; i < modeloTopTratamientos.getRowCount(); i++) {
                for (int col = 0; col < 3; col++) {
                    Object valor = modeloTopTratamientos.getValueAt(i, col);
                    String texto = formatearValorTabla(valor, col);
                    PdfPCell celda = new PdfPCell(new Phrase(texto, fCelda));
                    celda.setPadding(6);
                    celda.setHorizontalAlignment(col == 0
                        ? com.lowagie.text.Element.ALIGN_LEFT : com.lowagie.text.Element.ALIGN_RIGHT);
                    celda.setBackgroundColor(i % 2 == 0 ? java.awt.Color.WHITE : new java.awt.Color(0xF7, 0xF9, 0xFB));
                    tabla.addCell(celda);
                }
            }

            // --- Fila de totales, para que el reporte quede completo/organizado ---
            PdfPCell totalEtiqueta = new PdfPCell(new Phrase("Total", fTotal));
            totalEtiqueta.setPadding(6);
            totalEtiqueta.setBackgroundColor(new java.awt.Color(0xE6, 0xEF, 0xF6));
            tabla.addCell(totalEtiqueta);

            PdfPCell totalCantidad = new PdfPCell(new Phrase(String.valueOf(cantidadTotalTratamientos), fTotal));
            totalCantidad.setPadding(6);
            totalCantidad.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
            totalCantidad.setBackgroundColor(new java.awt.Color(0xE6, 0xEF, 0xF6));
            tabla.addCell(totalCantidad);

            PdfPCell totalMonto = new PdfPCell(new Phrase(FMT_MONEDA.format(totalTratamientos), fTotal));
            totalMonto.setPadding(6);
            totalMonto.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
            totalMonto.setBackgroundColor(new java.awt.Color(0xE6, 0xEF, 0xF6));
            tabla.addCell(totalMonto);

            documento.add(tabla);

            Paragraph pie = new Paragraph("Clínica Dental — Reporte generado automáticamente por el sistema.", fPie);
            pie.setSpacingBefore(16);
            pie.setAlignment(Element.ALIGN_CENTER);
            documento.add(pie);

            documento.close();

            mostrarExitoYAbrir(destino);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo generar el PDF:\n" + ex.getMessage(),
                "Error de exportación", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Da formato consistente (moneda con separador de miles) a las celdas numéricas de la tabla. */
    private String formatearValorTabla(Object valor, int columna) {
        if (valor == null) return "";
        if (columna == 2 && valor instanceof Number) {
            return FMT_MONEDA.format(((Number) valor).doubleValue());
        }
        return valor.toString();
    }

    // ================= EXPORTACIÓN A EXCEL (Apache POI) =================

    private void exportarExcel() {
        File destino = elegirDestino("reporte_clinica.xlsx", "Archivos Excel (*.xlsx)", "xlsx");
        if (destino == null) return;

        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            DataFormat formatoDatos = libro.createDataFormat();

            // ---- Estilo de título (igual línea visual que el PDF) ----
            XSSFCellStyle estiloTitulo = libro.createCellStyle();
            XSSFFont fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 16);
            fuenteTitulo.setColor(colorPersonalizado(COLOR_PRIMARIO));
            estiloTitulo.setFont(fuenteTitulo);

            // Barra de acento (línea de color sólida) debajo del título, como en el PDF
            XSSFCellStyle estiloAcento = libro.createCellStyle();
            estiloAcento.setFillForegroundColor(colorPersonalizado(COLOR_PRIMARIO));
            estiloAcento.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle estiloSubtitulo = libro.createCellStyle();
            org.apache.poi.ss.usermodel.Font fuenteSubtitulo = libro.createFont();
            fuenteSubtitulo.setItalic(true);
            fuenteSubtitulo.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            estiloSubtitulo.setFont(fuenteSubtitulo);

            // ---- Estilos de "tarjeta" para los indicadores (igual que las
            // cajas del PDF: etiqueta arriba con fondo suave, valor grande
            // abajo, todo dentro de un recuadro con borde) ----
            XSSFCellStyle estiloCajaEtiqueta = libro.createCellStyle();
            XSSFFont fuenteCajaEtiqueta = libro.createFont();
            fuenteCajaEtiqueta.setBold(true);
            fuenteCajaEtiqueta.setFontHeightInPoints((short) 10);
            fuenteCajaEtiqueta.setColor(colorPersonalizado(COLOR_TEXTO_SUAVE));
            estiloCajaEtiqueta.setFont(fuenteCajaEtiqueta);
            estiloCajaEtiqueta.setFillForegroundColor(colorPersonalizado(COLOR_FILA_ALT));
            estiloCajaEtiqueta.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloCajaEtiqueta.setBorderTop(BorderStyle.MEDIUM);
            estiloCajaEtiqueta.setBorderLeft(BorderStyle.MEDIUM);
            estiloCajaEtiqueta.setBorderRight(BorderStyle.MEDIUM);
            estiloCajaEtiqueta.setTopBorderColor(colorPersonalizado(COLOR_BORDE));
            estiloCajaEtiqueta.setLeftBorderColor(colorPersonalizado(COLOR_BORDE));
            estiloCajaEtiqueta.setRightBorderColor(colorPersonalizado(COLOR_BORDE));
            estiloCajaEtiqueta.setVerticalAlignment(VerticalAlignment.CENTER);
            estiloCajaEtiqueta.setWrapText(true);

            XSSFCellStyle estiloCajaValorTexto = libro.createCellStyle();
            XSSFFont fuenteCajaValor = libro.createFont();
            fuenteCajaValor.setBold(true);
            fuenteCajaValor.setFontHeightInPoints((short) 15);
            fuenteCajaValor.setColor(colorPersonalizado(COLOR_PRIMARIO_OSC));
            estiloCajaValorTexto.setFont(fuenteCajaValor);
            estiloCajaValorTexto.setFillForegroundColor(colorPersonalizado(COLOR_FILA_ALT));
            estiloCajaValorTexto.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloCajaValorTexto.setBorderBottom(BorderStyle.MEDIUM);
            estiloCajaValorTexto.setBorderLeft(BorderStyle.MEDIUM);
            estiloCajaValorTexto.setBorderRight(BorderStyle.MEDIUM);
            estiloCajaValorTexto.setBottomBorderColor(colorPersonalizado(COLOR_BORDE));
            estiloCajaValorTexto.setLeftBorderColor(colorPersonalizado(COLOR_BORDE));
            estiloCajaValorTexto.setRightBorderColor(colorPersonalizado(COLOR_BORDE));
            estiloCajaValorTexto.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFCellStyle estiloCajaValorMoneda = libro.createCellStyle();
            estiloCajaValorMoneda.cloneStyleFrom(estiloCajaValorTexto);
            estiloCajaValorMoneda.setDataFormat(formatoDatos.getFormat("\"RD$\" #,##0.00"));

            XSSFCellStyle estiloEncabezado = libro.createCellStyle();
            org.apache.poi.ss.usermodel.Font fuenteEncabezado = libro.createFont();
            fuenteEncabezado.setBold(true);
            fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());
            estiloEncabezado.setFont(fuenteEncabezado);
            estiloEncabezado.setFillForegroundColor(colorPersonalizado(COLOR_PRIMARIO));
            estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloEncabezado.setAlignment(HorizontalAlignment.CENTER);
            estiloEncabezado.setVerticalAlignment(VerticalAlignment.CENTER);
            estiloEncabezado.setBorderBottom(BorderStyle.THIN);

            // Estilo de celda de texto normal, con borde fino (para que la
            // tabla se vea "organizada" en vez de solo texto suelto).
            XSSFCellStyle estiloCeldaTexto = libro.createCellStyle();
            estiloCeldaTexto.setBorderBottom(BorderStyle.THIN);
            estiloCeldaTexto.setBorderTop(BorderStyle.THIN);
            estiloCeldaTexto.setBorderLeft(BorderStyle.THIN);
            estiloCeldaTexto.setBorderRight(BorderStyle.THIN);
            estiloCeldaTexto.setVerticalAlignment(VerticalAlignment.CENTER);

            // Estilo numérico con separador de miles y 2 decimales.
            XSSFCellStyle estiloCeldaNumero = libro.createCellStyle();
            estiloCeldaNumero.cloneStyleFrom(estiloCeldaTexto);
            estiloCeldaNumero.setDataFormat(formatoDatos.getFormat("#,##0"));
            estiloCeldaNumero.setAlignment(HorizontalAlignment.RIGHT);

            XSSFCellStyle estiloCeldaMoneda = libro.createCellStyle();
            estiloCeldaMoneda.cloneStyleFrom(estiloCeldaTexto);
            estiloCeldaMoneda.setDataFormat(formatoDatos.getFormat("\"RD$\" #,##0.00"));
            estiloCeldaMoneda.setAlignment(HorizontalAlignment.RIGHT);

            XSSFCellStyle estiloFilaAlterna = libro.createCellStyle();
            estiloFilaAlterna.cloneStyleFrom(estiloCeldaTexto);
            estiloFilaAlterna.setFillForegroundColor(colorPersonalizado(COLOR_FILA_ALT));
            estiloFilaAlterna.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle estiloFilaAlternaNumero = libro.createCellStyle();
            estiloFilaAlternaNumero.cloneStyleFrom(estiloCeldaNumero);
            estiloFilaAlternaNumero.setFillForegroundColor(colorPersonalizado(COLOR_FILA_ALT));
            estiloFilaAlternaNumero.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle estiloTotalEtiqueta = libro.createCellStyle();
            estiloTotalEtiqueta.cloneStyleFrom(estiloCeldaTexto);
            org.apache.poi.ss.usermodel.Font fuenteTotal = libro.createFont();
            fuenteTotal.setBold(true);
            estiloTotalEtiqueta.setFont(fuenteTotal);
            estiloTotalEtiqueta.setFillForegroundColor(colorPersonalizado(new Color(0xE6, 0xEF, 0xF6)));
            estiloTotalEtiqueta.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle estiloTotalNumero = libro.createCellStyle();
            estiloTotalNumero.cloneStyleFrom(estiloTotalEtiqueta);
            estiloTotalNumero.setAlignment(HorizontalAlignment.RIGHT);

            XSSFCellStyle estiloTotalMoneda = libro.createCellStyle();
            estiloTotalMoneda.cloneStyleFrom(estiloTotalNumero);
            estiloTotalMoneda.setDataFormat(formatoDatos.getFormat("\"RD$\" #,##0.00"));

            // ---- Hoja 1: Resumen general ----
            Sheet hojaResumen = libro.createSheet("Resumen general");
            hojaResumen.setDisplayGridlines(false);

            for (int c = 0; c <= 4; c++) hojaResumen.setColumnWidth(c, 20 * 256);

            // Título
            Row filaTitulo = hojaResumen.createRow(0);
            filaTitulo.setHeightInPoints(26);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue("Reporte General - Clínica Dental");
            celdaTitulo.setCellStyle(estiloTitulo);
            hojaResumen.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            // Barra de acento (línea de color sólida) debajo del título
            Row filaAcento = hojaResumen.createRow(1);
            filaAcento.setHeightInPoints(4);
            for (int c = 0; c <= 4; c++) {
                Cell celda = filaAcento.createCell(c);
                celda.setCellStyle(estiloAcento);
            }
            hojaResumen.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

            // Subtítulo con la fecha de generación
            Row filaFecha = hojaResumen.createRow(2);
            Cell celdaFecha = filaFecha.createCell(0);
            celdaFecha.setCellValue("Generado el "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            celdaFecha.setCellStyle(estiloSubtitulo);
            hojaResumen.addMergedRegion(new CellRangeAddress(2, 2, 0, 4));

            // Indicadores clave en formato de "tarjeta": una columna por
            // indicador, etiqueta arriba y valor grande abajo, todo dentro
            // de un recuadro con fondo suave (igual que en el PDF).
            String[] etiquetasIndicadores = {
                "Pacientes activos", "Doctores activos", "Citas de hoy", "Total facturado", "Saldo pendiente"
            };
            Object[] valoresIndicadores = {
                lblPacientes.getText(), lblDoctores.getText(), lblCitasHoy.getText(),
                extraerNumero(lblFacturado.getText()), extraerNumero(lblPendiente.getText())
            };
            boolean[] esMonedaIndicador = {false, false, false, true, true};

            int filaEtiquetasIdx = 4;
            int filaValoresIdx = 5;
            Row filaEtiquetas = hojaResumen.createRow(filaEtiquetasIdx);
            filaEtiquetas.setHeightInPoints(26);
            Row filaValores = hojaResumen.createRow(filaValoresIdx);
            filaValores.setHeightInPoints(30);

            for (int c = 0; c < etiquetasIndicadores.length; c++) {
                Cell celdaEtiqueta = filaEtiquetas.createCell(c);
                celdaEtiqueta.setCellValue(etiquetasIndicadores[c]);
                celdaEtiqueta.setCellStyle(estiloCajaEtiqueta);

                Cell celdaValor = filaValores.createCell(c);
                if (esMonedaIndicador[c]) {
                    celdaValor.setCellValue(((Number) valoresIndicadores[c]).doubleValue());
                    celdaValor.setCellStyle(estiloCajaValorMoneda);
                } else {
                    // Pacientes/Doctores/Citas son conteos; se guardan como
                    // texto porque vienen directo del JLabel (ya validados).
                    celdaValor.setCellValue(valoresIndicadores[c].toString());
                    celdaValor.setCellStyle(estiloCajaValorTexto);
                }
            }

            // ---- Hoja 2: Tratamientos ----
            Sheet hojaTratamientos = libro.createSheet("Tratamientos");
            hojaTratamientos.setDisplayGridlines(false);

            Row filaTituloTrat = hojaTratamientos.createRow(0);
            filaTituloTrat.setHeightInPoints(24);
            Cell celdaTituloTrat = filaTituloTrat.createCell(0);
            celdaTituloTrat.setCellValue("Tratamientos más realizados");
            celdaTituloTrat.setCellStyle(estiloTitulo);
            hojaTratamientos.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

            Row encabezado = hojaTratamientos.createRow(2);
            encabezado.setHeightInPoints(24);
            String[] columnas = {"Tratamiento", "Cantidad", "Total (RD$)"};
            for (int c = 0; c < columnas.length; c++) {
                Cell celda = encabezado.createCell(c);
                celda.setCellValue(columnas[c]);
                celda.setCellStyle(estiloEncabezado);
            }
            for (int i = 0; i < modeloTopTratamientos.getRowCount(); i++) {
                Row r = hojaTratamientos.createRow(i + 3);
                r.setHeightInPoints(20);
                boolean filaAlterna = (i % 2 == 1);
                for (int c = 0; c < 3; c++) {
                    Object valor = modeloTopTratamientos.getValueAt(i, c);
                    Cell celda = r.createCell(c);
                    if (c == 0) {
                        celda.setCellValue(valor != null ? valor.toString() : "");
                        celda.setCellStyle(filaAlterna ? estiloFilaAlterna : estiloCeldaTexto);
                    } else if (c == 1) {
                        celda.setCellValue(valor instanceof Number ? ((Number) valor).doubleValue() : 0);
                        celda.setCellStyle(filaAlterna ? estiloFilaAlternaNumero : estiloCeldaNumero);
                    } else {
                        celda.setCellValue(valor instanceof Number ? ((Number) valor).doubleValue() : 0);
                        XSSFCellStyle base = filaAlterna ? estiloFilaAlternaNumero : estiloCeldaNumero;
                        XSSFCellStyle moneda = libro.createCellStyle();
                        moneda.cloneStyleFrom(base);
                        moneda.setDataFormat(formatoDatos.getFormat("\"RD$\" #,##0.00"));
                        celda.setCellStyle(moneda);
                    }
                }
            }

            // Fila de totales al final de la tabla, para que el reporte
            // quede completo (no solo una lista suelta de tratamientos).
            int filaTotalIdx = modeloTopTratamientos.getRowCount() + 3;
            Row filaTotal = hojaTratamientos.createRow(filaTotalIdx);
            filaTotal.setHeightInPoints(22);
            Cell etiquetaTotal = filaTotal.createCell(0);
            etiquetaTotal.setCellValue("Total");
            etiquetaTotal.setCellStyle(estiloTotalEtiqueta);

            Cell cantidadTotal = filaTotal.createCell(1);
            cantidadTotal.setCellValue(cantidadTotalTratamientos);
            cantidadTotal.setCellStyle(estiloTotalNumero);

            Cell montoTotal = filaTotal.createCell(2);
            montoTotal.setCellValue(totalTratamientos.doubleValue());
            montoTotal.setCellStyle(estiloTotalMoneda);

            hojaTratamientos.createFreezePane(0, 3); // encabezado siempre visible al hacer scroll
            hojaTratamientos.setColumnWidth(0, 32 * 256);
            hojaTratamientos.setColumnWidth(1, 14 * 256);
            hojaTratamientos.setColumnWidth(2, 16 * 256);

            try (FileOutputStream fos = new FileOutputStream(destino)) {
                libro.write(fos);
            }
            mostrarExitoYAbrir(destino);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo generar el Excel:\n" + ex.getMessage(),
                "Error de exportación", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Convierte un java.awt.Color de la app a un XSSFColor usable en estilos de POI. */
    private static XSSFColor colorPersonalizado(Color color) {
        return new XSSFColor(new byte[]{(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()}, null);
    }

    /** Extrae el valor numérico de un texto tipo "RD$ 3,500.00" para guardarlo como número en Excel. */
    private double extraerNumero(String textoConFormato) {
        try {
            String limpio = textoConFormato.replaceAll("[^0-9.]", "");
            return limpio.isEmpty() ? 0 : Double.parseDouble(limpio);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // ================= UTILIDADES DE EXPORTACIÓN =================

    private File elegirDestino(String nombreSugerido, String descripcionFiltro, String extension) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar reporte");
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

    private void mostrarExitoYAbrir(File archivo) {
        int opcion = JOptionPane.showConfirmDialog(this,
            "Reporte exportado correctamente en:\n" + archivo.getAbsolutePath() + "\n\n¿Deseas abrirlo ahora?",
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