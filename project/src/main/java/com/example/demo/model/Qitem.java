package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "qitem")
public class Qitem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "license_type")
    private String licenseType;

    // Foreign key to Quat (Quotation)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quat_id", nullable = false, foreignKey = @ForeignKey(name = "FK_qitem_quat"))
    private Quat quotation;

    // Foreign key to Item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "FK_qitem_item"))
    private Item item;

    public Qitem() {
    }

    public Qitem(Integer quantity, BigDecimal unitPrice, String licenseType, Quat quotation, Item item) {
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.licenseType = licenseType;
        this.quotation = quotation;
        this.item = item;
        calculateTotalPrice();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public Quat getQuotation() {
        return quotation;
    }

    public void setQuotation(Quat quotation) {
        this.quotation = quotation;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
        if (item != null && unitPrice == null) {
            this.unitPrice = item.getPrice();
            calculateTotalPrice();
        }
    }

    // Helper methods for backward compatibility
    public Long getQuatId() {
        return quotation != null ? quotation.getId() : null;
    }

    public Long getItemId() {
        return item != null ? item.getId() : null;
    }

    public String getQitem() {
        return item != null ? item.getItemname() : null;
    }

    public Integer getQty() {
        return quantity;
    }

    public void setQty(Integer qty) {
        setQuantity(qty);
    }

    // Calculate total price automatically
    private void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @PrePersist
    @PreUpdate
    private void prePersist() {
        calculateTotalPrice();
    }

    @Override
    public String toString() {
        return "Qitem{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", licenseType='" + licenseType + '\'' +
                ", quatId=" + getQuatId() +
                ", itemId=" + getItemId() +
                '}';
    }
}