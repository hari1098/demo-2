package com.example.demo.controller;

import com.example.demo.model.Qitem;
import com.example.demo.service.QitemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qitem")
public class QitemController {

    @Autowired
    private QitemService qitemService;

    @GetMapping
    public List<Qitem> getQitems() {
        return qitemService.getQitems();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Qitem> getQitemById(@PathVariable Long id) {
        return qitemService.getQitemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/quotation/{quatId}")
    public List<Qitem> getQitemsByQuotationId(@PathVariable Long quatId) {
        return qitemService.getQitemsByQuotationId(quatId);
    }

    @PostMapping
    public ResponseEntity<Qitem> createQitem(@RequestBody Qitem qitem) {
        try {
            Qitem createdQitem = qitemService.createQitem(qitem);
            return new ResponseEntity<>(createdQitem, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Qitem> updateQitem(@PathVariable Long id, @RequestBody Qitem qitem) {
        Qitem updated = qitemService.updateQitem(id, qitem);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQitem(@PathVariable Long id) {
        boolean deleted = qitemService.deleteQitem(id);
        return deleted ? ResponseEntity.ok("Qitem deleted.") : ResponseEntity.notFound().build();
    }
}