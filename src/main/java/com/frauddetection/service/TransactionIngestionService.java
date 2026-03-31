package com.frauddetection.service;

import com.frauddetection.dto.TransactionRequest;
import com.frauddetection.dto.TransactionResponse;
import com.frauddetection.model.Transaction;
import com.frauddetection.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TransactionIngestionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionIngestionService.class);

    private final TransactionRepository transactionRepository;
    private final FraudDetectionPipeline fraudDetectionPipeline;

    public TransactionIngestionService(TransactionRepository transactionRepository, FraudDetectionPipeline fraudDetectionPipeline) {
        this.transactionRepository = transactionRepository;
        this.fraudDetectionPipeline = fraudDetectionPipeline;
    }

    @Transactional
    public TransactionResponse ingest(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(request.getAccountId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        transaction.setMerchantId(request.getMerchantId());
        transaction.setMerchantCategory(request.getMerchantCategory());
        transaction.setLatitude(request.getLatitude());
        transaction.setLongitude(request.getLongitude());
        transaction.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());
        transaction.setChannel(request.getChannel());
        transaction.setIpAddress(request.getIpAddress());
        transaction.setDeviceId(request.getDeviceId());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        transaction = transactionRepository.save(transaction);
        log.info("Ingested transaction: id={}, account={}, amount={}",
                transaction.getId(), transaction.getAccountId(), transaction.getAmount());

        runFraudDetection(transaction);

        return toResponse(transaction);
    }

    @Async("fraudDetectionExecutor")
    public void runFraudDetection(Transaction transaction) {
        try {
            fraudDetectionPipeline.process(transaction);
        } catch (Exception e) {
            log.error("Fraud detection failed for transaction {}: {}", transaction.getId(), e.getMessage(), e);
        }
    }

    public Page<Transaction> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    public Page<Transaction> findByAccountId(String accountId, Pageable pageable) {
        return transactionRepository.findByAccountId(accountId, pageable);
    }

    public Page<Transaction> findByStatus(Transaction.TransactionStatus status, Pageable pageable) {
        return transactionRepository.findByStatus(status, pageable);
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
    }

    public static TransactionResponse toResponse(Transaction tx) {
        TransactionResponse r = new TransactionResponse();
        r.setId(tx.getId());
        r.setAccountId(tx.getAccountId());
        r.setAmount(tx.getAmount());
        r.setCurrency(tx.getCurrency());
        r.setMerchantId(tx.getMerchantId());
        r.setMerchantCategory(tx.getMerchantCategory());
        r.setLatitude(tx.getLatitude());
        r.setLongitude(tx.getLongitude());
        r.setTimestamp(tx.getTimestamp());
        r.setChannel(tx.getChannel());
        r.setStatus(tx.getStatus());
        r.setIpAddress(tx.getIpAddress());
        r.setDeviceId(tx.getDeviceId());
        return r;
    }
}
