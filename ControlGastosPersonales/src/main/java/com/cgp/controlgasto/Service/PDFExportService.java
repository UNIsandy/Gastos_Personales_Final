package com.cgp.controlgasto.Service;

import com.cgp.controlgasto.Model.TipoTransaccion;
import com.cgp.controlgasto.Model.Transaccion;
import com.cgp.controlgasto.Repository.TransaccionRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class PDFExportService {

    private final TransaccionRepository repository;

    public PDFExportService(TransaccionRepository repository) {
        this.repository = repository;
    }

    public byte[] generarReporteMensual(Long usuarioId, int mes, int anio) {
        List<Transaccion> transacciones = repository.findByUsuarioId(usuarioId).stream()
            .filter(t -> t.getFecha().getMonthValue() == mes && t.getFecha().getYear() == anio)
            .toList();

        double totalIngresos = transacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
            .mapToDouble(Transaccion::getMonto).sum();

        double totalGastos = transacciones.stream()
            .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
            .mapToDouble(Transaccion::getMonto).sum();

        String mesNombre = LocalDate.of(anio, mes, 1).format(DateTimeFormatter.ofPattern("MMMM", new Locale("es", "ES")));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);

        document.open();

        document.add(new Paragraph("Reporte Mensual - " + mesNombre + " " + anio, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("Total Ingresos: S/ " + String.format("%.2f", totalIngresos), FontFactory.getFont(FontFactory.HELVETICA, 12)));
        document.add(new Paragraph("Total Gastos: S/ " + String.format("%.2f", totalGastos), FontFactory.getFont(FontFactory.HELVETICA, 12)));
        document.add(new Paragraph("Balance: S/ " + String.format("%.2f", totalIngresos - totalGastos), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.addCell("Descripción");
        table.addCell("Monto");
        table.addCell("Tipo");
        table.addCell("Fecha");

        for (Transaccion t : transacciones) {
            table.addCell(t.getDescripcion());
            table.addCell("S/ " + String.format("%.2f", t.getMonto()));
            table.addCell(t.getTipo().name());
            table.addCell(t.getFecha().toString());
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }
}
