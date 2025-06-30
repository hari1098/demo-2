package com.example.demo.controller;

import com.example.demo.service.InvoicePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoicePdfService invoicePdfService;

    @Autowired
    public InvoiceController(InvoicePdfService invoicePdfService) {
        this.invoicePdfService = invoicePdfService;
    }

    @GetMapping("/generate/{quatId}")
    public ResponseEntity<byte[]> generateInvoice(@PathVariable Long quatId) {
        try {
            byte[] pdfBytes = invoicePdfService.generateInvoicePdf(quatId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice_" + quatId + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(("Error generating invoice: " + e.getMessage()).getBytes());
        }
    }
}