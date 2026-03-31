package com.frauddetection.controller;

import com.frauddetection.dto.SimulationRequest;
import com.frauddetection.dto.TransactionRequest;
import com.frauddetection.model.Transaction;
import com.frauddetection.service.TransactionIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Random;

@RestController
@RequestMapping("/api/simulate")
@CrossOrigin("*")
public class SimulationController {

    private static final Logger log = LoggerFactory.getLogger(SimulationController.class);
    private final TransactionIngestionService ingestionService;
    private final Random random = new Random();

    private final String[] MERCHANTS = {"AMAZON", "WALMART", "NETFLIX", "STARBUCKS", "UBER", "APPLE", "STEAM"};

    public SimulationController(TransactionIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<String> runSimulation(@RequestBody SimulationRequest config) {
        long targetAcc = config.getTargetAccountId() != null ?
                Long.parseLong(config.getTargetAccountId().replace("ACC", "")) : random.nextInt(100) + 1;

        int fraudCount = (int) (config.getCount() * config.getFraudRatio());

        for (int i = 0; i < config.getCount(); i++) {
            boolean isFraud = i < fraudCount;
            String accId = isFraud ? "ACC" + targetAcc : "ACC" + (random.nextInt(1000) + 1);

            TransactionRequest req = new TransactionRequest();
            req.setAccountId(accId);
            
            double amount = isFraud ? (2000 + random.nextDouble() * 5000) : (5 + random.nextDouble() * 200);
            req.setAmount(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP));
            req.setCurrency("USD");
            req.setMerchantId(MERCHANTS[random.nextInt(MERCHANTS.length)]);
            req.setLatitude(isFraud ? random.nextDouble() * 90 : 40.7128);
            req.setLongitude(isFraud ? random.nextDouble() * 180 : -74.0060);
            req.setChannel(isFraud ? Transaction.Channel.ONLINE :
                    Transaction.Channel.values()[random.nextInt(4)]);

            ingestionService.ingest(req);

            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }
        return ResponseEntity.ok("{\"injected\": " + config.getCount() + ", \"fraud\": " + fraudCount + "}");
    }

    @PostMapping("/scenario/{name}")
    public ResponseEntity<String> runScenario(@PathVariable String name) {
        LocalDateTime now = LocalDateTime.now();
        String targetAcc = "ACC_SCENARIO_" + random.nextInt(999);
        int injectedCount = 0;

        switch (name.toLowerCase()) {
            case "card-testing":
                for (int i = 0; i < 20; i++) {
                    TransactionRequest req = new TransactionRequest();
                    req.setAccountId(targetAcc);
                    req.setAmount(BigDecimal.valueOf(1.00 + random.nextDouble() * 2.0));
                    req.setChannel(Transaction.Channel.ONLINE);
                    req.setMerchantId("STEAM");
                    req.setTimestamp(now.plusSeconds(i * 30));
                    ingestionService.ingest(req);
                    injectedCount++;
                }
                break;
            case "account-takeover":
                for (int i = 0; i < 5; i++) {
                    TransactionRequest req = new TransactionRequest();
                    req.setAccountId(targetAcc);
                    req.setAmount(BigDecimal.valueOf(15.0));
                    req.setChannel(Transaction.Channel.POS);
                    req.setTimestamp(now.minusDays(i + 1));
                    ingestionService.ingest(req);
                    injectedCount++;
                }
                TransactionRequest req = new TransactionRequest();
                req.setAccountId(targetAcc);
                req.setAmount(BigDecimal.valueOf(7500.0));
                req.setChannel(Transaction.Channel.ONLINE);
                req.setTimestamp(now);
                ingestionService.ingest(req);
                injectedCount++;
                break;
            case "geo-velocity":
                TransactionRequest req1 = new TransactionRequest();
                req1.setAccountId(targetAcc);
                req1.setAmount(BigDecimal.valueOf(50.0));
                req1.setLatitude(40.7128); // NY
                req1.setLongitude(-74.0060);
                req1.setChannel(Transaction.Channel.POS);
                req1.setTimestamp(now.minusMinutes(5));
                
                TransactionRequest req2 = new TransactionRequest();
                req2.setAccountId(targetAcc);
                req2.setAmount(BigDecimal.valueOf(300.0));
                req2.setLatitude(35.6762); // Tokyo
                req2.setLongitude(139.6503);
                req2.setChannel(Transaction.Channel.POS);
                req2.setTimestamp(now);

                ingestionService.ingest(req1);
                injectedCount++;
                ingestionService.ingest(req2);
                injectedCount++;
                break;
            default:
                return ResponseEntity.badRequest().body("{\"error\": \"Unknown scenario\"}");
        }
        return ResponseEntity.ok("{\"transactions\": " + injectedCount + ", \"account\": \"" + targetAcc + "\"}");
    }
}
