package com.frauddetection.controller;

import com.frauddetection.dto.TransactionRequest;
import com.frauddetection.dto.TransactionResponse;
import com.frauddetection.model.Transaction;
import com.frauddetection.service.TransactionIngestionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin("*")
public class TransactionController {

    private final TransactionIngestionService ingestionService;

    public TransactionController(TransactionIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> ingestTransaction(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(ingestionService.ingest(request));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> listTransactions(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) Transaction.TransactionStatus status,
            Pageable pageable) {

        Page<Transaction> page;
        if (accountId != null) {
            page = ingestionService.findByAccountId(accountId, pageable);
        } else if (status != null) {
            page = ingestionService.findByStatus(status, pageable);
        } else {
            page = ingestionService.findAll(pageable);
        }

        return ResponseEntity.ok(page.map(TransactionIngestionService::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(TransactionIngestionService.toResponse(ingestionService.findById(id)));
    }
}
