package com.safebank.transaction.application;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.safebank.transaction.application.dto.TransactionHistoryResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReceiptService {

    private static final Color SAFEBANK_INDIGO = new DeviceRgb(79, 70, 229);
    private static final Color EMERALD_GREEN = new DeviceRgb(16, 185, 129);
    private static final Color RED_LOSS = new DeviceRgb(220, 38, 38);
    private static final Color GRAY_TEXT = new DeviceRgb(107, 114, 128);

    public byte[] generateReceipt(TransactionHistoryResponse tx) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // --- 1. CABECERA CON LOGO Y TÍTULO ---
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{20, 80})).useAllAvailableWidth();
            headerTable.setBorder(Border.NO_BORDER);

            try {
                byte[] logoBytes = new ClassPathResource("logo.png").getContentAsByteArray();
                Image logo = new Image(ImageDataFactory.create(logoBytes));
                logo.setWidth(UnitValue.createPercentValue(100));
                headerTable.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER));
            } catch (IOException e) {
                headerTable.addCell(new Cell().add(new Paragraph("SafeBank")).setBorder(Border.NO_BORDER));
                System.err.println("No se pudo cargar el logo, se generará PDF sin imagen.");
            }

            Cell bankInfoCell = new Cell().add(
                    new Paragraph("SafeBank - Justificante de Operación")
                            .setBold()
                            .setFontSize(20)
                            .setFontColor(SAFEBANK_INDIGO)
                            .setTextAlignment(TextAlignment.RIGHT)
            ).setBorder(Border.NO_BORDER);
            headerTable.addCell(bankInfoCell);
            
            document.add(headerTable);
            document.add(new Paragraph("\n"));


            // --- 2. CUERPO DE DATOS ---
            document.add(new Paragraph("Detalles de la Transacción")
                    .setBold()
                    .setFontSize(14)
                    .setMarginBottom(10));

            Table dataTable = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();
            
            // Usamos la clase Style original de iText
            Style labelStyle = new Style().setBold().setFontColor(GRAY_TEXT);

            addStyledRow(dataTable, "ID de Operación:", "#" + tx.id(), labelStyle);
            addStyledRow(dataTable, "Fecha y Hora:", tx.transactionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), labelStyle);
            addStyledRow(dataTable, "Concepto:", tx.concept(), labelStyle);
            addStyledRow(dataTable, "Tipo:", tx.isIncoming() ? "Ingreso (Recibido)" : "Envío (Transferencia)", labelStyle);
            addStyledRow(dataTable, tx.isIncoming() ? "De (Origen):" : "Para (Destino):", tx.otherIban(), labelStyle);

            document.add(dataTable);
            document.add(new Paragraph("\n\n"));


            // --- 3. IMPORTE FINAL ---
            Table totalTable = new Table(1).useAllAvailableWidth();
            
            String sign = tx.isIncoming() ? "+" : "-";
            Color amountColor = tx.isIncoming() ? EMERALD_GREEN : RED_LOSS;

            Cell totalCell = new Cell().add(
                    new Paragraph("TOTAL IMPORTE")
                            .setFontSize(12)
                            .setFontColor(GRAY_TEXT)
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER)
            ).add(
                    new Paragraph(sign + tx.amount() + " EUR")
                            .setFontSize(28)
                            .setBold()
                            .setFontColor(amountColor)
                            .setTextAlignment(TextAlignment.CENTER)
            )
            .setBackgroundColor(new DeviceRgb(249, 250, 251))
            .setPadding(20)
            .setBorder(Border.NO_BORDER);

            totalTable.addCell(totalCell);
            document.add(totalTable);

            // --- 4. PIE DE PÁGINA ---
            document.add(new Paragraph("\n\n\nEste documento sirve como justificante bancario de SafeBank.")
                    .setFontSize(8)
                    .setFontColor(GRAY_TEXT)
                    .setTextAlignment(TextAlignment.CENTER));

        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    private void addStyledRow(Table table, String label, String value, Style labelStyle) {
        Cell labelCell = new Cell().add(new Paragraph(label).addStyle(labelStyle))
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(5);
        table.addCell(labelCell);

        Cell valueCell = new Cell().add(new Paragraph(value).setFontColor(com.itextpdf.kernel.colors.DeviceRgb.BLACK))
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(5);
        table.addCell(valueCell);
    }
}