package com.example.demo.dto;

import java.math.BigDecimal;

public class QitemCreateRequest {
    private Integer quantity;
    private BigDecimal unitPrice;
    private String licenseType;
    private Long quatId;
    private Long itemId;

    public QitemCreateRequest() {
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public Long getQuatId() {
        return quatId;
    }

    public void setQuatId(Long quatId) {
        this.quatId = quatId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
}