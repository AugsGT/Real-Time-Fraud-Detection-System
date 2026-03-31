package com.frauddetection.config;

import com.frauddetection.model.FraudRule;
import com.frauddetection.repository.FraudRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final FraudRuleRepository fraudRuleRepository;

    public DataInitializer(FraudRuleRepository fraudRuleRepository) {
        this.fraudRuleRepository = fraudRuleRepository;
    }

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            if (fraudRuleRepository.count() == 0) {
                log.info("Initializing default fraud rules...");

                FraudRule amountRule = new FraudRule();
                amountRule.setName("High Amount Threshold");
                amountRule.setDescription("Flags transactions over $5,000");
                amountRule.setRuleType(FraudRule.RuleType.AMOUNT_THRESHOLD);
                amountRule.setParamJson("{\"maxAmount\": 5000.0}");
                amountRule.setEnabled(true);
                amountRule.setCreatedAt(LocalDateTime.now());

                FraudRule freqRule = new FraudRule();
                freqRule.setName("High Frequency Account");
                freqRule.setDescription("Flags > 8 transactions in 30 minutes");
                freqRule.setRuleType(FraudRule.RuleType.FREQUENCY);
                freqRule.setParamJson("{\"maxCount\": 8, \"windowMinutes\": 30}");
                freqRule.setEnabled(true);
                freqRule.setCreatedAt(LocalDateTime.now());

                FraudRule geoRule = new FraudRule();
                geoRule.setName("Impossible Travel (Geo-Velocity)");
                geoRule.setDescription("Flags speed > 900km/h between transactions");
                geoRule.setRuleType(FraudRule.RuleType.GEO_VELOCITY);
                geoRule.setParamJson("{\"maxSpeedKmH\": 900.0}");
                geoRule.setEnabled(true);
                geoRule.setCreatedAt(LocalDateTime.now());
                
                FraudRule channelRule = new FraudRule();
                channelRule.setName("High Risk ONLINE Channel");
                channelRule.setDescription("Flags ONLINE transactions over $2,000");
                channelRule.setRuleType(FraudRule.RuleType.CHANNEL_RISK);
                channelRule.setParamJson("{\"onlineThreshold\": 2000.0}");
                channelRule.setEnabled(true);
                channelRule.setCreatedAt(LocalDateTime.now());

                fraudRuleRepository.saveAll(List.of(amountRule, freqRule, geoRule, channelRule));
                log.info("Loaded {} default rules.", fraudRuleRepository.count());
            }
        };
    }
}
