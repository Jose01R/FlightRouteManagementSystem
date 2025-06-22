package data;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import domain.common.Airport;
import domain.linkedlist.DoublyLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.SinglyLinkedList;
import domain.service.AirportService;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.stream.Stream;


public class TopListPdf {
//    public static void main(String[] args) throws DocumentException, FileNotFoundException {
//        Document doc = new Document();
//        String fileName = "C:\\Repositorios\\Proyecto-Algoritmos y Estruc de Datos\\FlightRouteManagementSystem\\src\\main\\java\\data\\Prueba Top Aeropuertos.pdf";
//        PdfWriter.getInstance(doc, new FileOutputStream(fileName));
//        doc.open();
//
//        Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
//        Paragraph paragraph = new Paragraph("Duration of ChronoUnits", bold);
//        doc.add(paragraph);
//
//        PdfPTable table = new PdfPTable(2);
//        Stream.of("Chrono Unit", "Duration").forEach(table::addCell);
//
//        Arrays.stream(ChronoUnit.values()).forEach(unit -> {
//            table.addCell(unit.toString());
//            table.addCell(unit.getDuration().toString());
//        });
//
//        doc.add(table);
//        doc.close();
//    }

    public static void main(String[] args) throws DocumentException, IOException, IOException, ListException {
        // Crear documento y archivo PDF en carpeta data
        String fileName = "C:\\Repositorios\\Proyecto-Algoritmos y Estruc de Datos\\FlightRouteManagementSystem\\src\\main\\java\\data\\Prueba Top Aeropuertos.pdf";

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        doc.open();

        // Título
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Lista de Aeropuertos", titleFont);
        doc.add(title);

        // Crear tabla con 4 columnas
        PdfPTable table = new PdfPTable(4);
        table.addCell("Code");
        table.addCell("Name");
        table.addCell("Country");
        table.addCell("Status");

        // Iterar sobre la lista de aeropuertos
        DoublyLinkedList list = new DoublyLinkedList();
        AirportService airportService = new AirportService();
        list = airportService.getAllAirports();

        for (int i = 1; i <= list.size(); i++) {
            Airport a = (Airport) list.getNode(i).data;
            table.addCell(String.valueOf(a.getCode()));
            table.addCell(a.getName());
            table.addCell(a.getCountry());
            table.addCell(a.getStatus());
        }

        doc.add(table);
        doc.close();

        System.out.println("PDF generado: " + new java.io.File(fileName).getAbsolutePath());
    }
}
