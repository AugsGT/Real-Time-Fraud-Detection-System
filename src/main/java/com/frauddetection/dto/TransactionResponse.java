package com.frauddetection.dto;

import com.frauddetection.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String merchantId;
    private String merchantCategory;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private Transaction.Channel channel;
    private Transaction.TransactionStatus status;
    private String ipAddress;
    private String deviceId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getMerchantCategory() { return merchantCategory; }
    public void setMerchantCategory(String merchantCategory) { this.merchantCategory = merchantCategory; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Transaction.Channel getChannel() { return channel; }
    public void setChannel(Transaction.Channel channel) { this.channel = channel; }
    public Transaction.TransactionStatus getStatus() { return status; }
    public void setStatus(Transaction.TransactionStatus status) { this.status = status; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}
