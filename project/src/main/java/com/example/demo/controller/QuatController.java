package com.example.demo.controller;

import com.example.demo.model.Quat;
import com.example.demo.service.InvoicePdfService;
import com.example.demo.service.QuatService;
import com.example.demo.service.QuotationPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/quat")
public class QuatController {

    @Autowired
    private QuatService quatService;

    @Autowired
    private InvoicePdfService invoicePdfService;

    @Autowired
    private QuotationPdfService quotationPdfService;

    @PostMapping
    public ResponseEntity<Quat> createQuat(@RequestBody Quat quat) {
        try {
            Quat createdQuat = quatService.createQuat(quat);
            return new ResponseEntity<>(createdQuat, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quat> updateQuat(@PathVariable Long id, @RequestBody Quat quat) {
        try {
            Quat updated = quatService.updateQuat(id, quat);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public List<Quat> getAllQuats() {
        return quatService.getAllQuats();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quat> getQuatById(@PathVariable Long id) {
        Optional<Quat> quat = quatService.getQuatById(id);
        return quat.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public List<Quat> getQuatsByCustomerId(@PathVariable Long customerId) {
        return quatService.getQuatsByCustomerId(customerId);
    }

    @GetMapping("/user/{userId}")
    public List<Quat> getQuatsByUserId(@PathVariable Long userId) {
        return quatService.getQuatsByUserId(userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuat(@PathVariable Long id) {
        boolean deleted = quatService.deleteQuat(id);
        return deleted ? ResponseEntity.ok("Quotation deleted successfully.") : ResponseEntity.notFound().build();
    }

    @GetMapping("/{quatId}/quotation")
    public ResponseEntity<byte[]> generateQuotation(@PathVariable Long quatId) {
        try {
            byte[] pdfBytes = quotationPdfService.generateQuotationPdf(quatId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "quotation_" + quatId + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(("Error generating PDF: " + e.getMessage()).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage().getBytes(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{quatId}/invoice")
    public ResponseEntity<byte[]> generateInvoice(@PathVariable Long quatId) {
        try {
            byte[] pdfBytes = invoicePdfService.generateInvoicePdf(quatId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "invoice_" + quatId + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(("Error generating PDF: " + e.getMessage()).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage().getBytes(), HttpStatus.NOT_FOUND);
        }
    }
}