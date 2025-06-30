package com.example.demo.controller;

import com.example.demo.service.QuotationPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotations")
public class QuotationController {

    private final QuotationPdfService quotationPdfService;

    @Autowired
    public QuotationController(QuotationPdfService quotationPdfService) {
        this.quotationPdfService = quotationPdfService;
    }

    @GetMapping("/generate/{quatId}")
    public ResponseEntity<byte[]> generateQuotation(@PathVariable Long quatId) {
        try {
            byte[] pdfBytes = quotationPdfService.generateQuotationPdf(quatId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "quotation_" + quatId + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(("Error generating quotation: " + e.getMessage()).getBytes());
        }
    }
}