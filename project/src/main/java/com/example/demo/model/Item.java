package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itemid")
    private Long id;

    @Column(name = "id_name")
    private String idname;

    @Column(name = "license_type")
    private String licensetype;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "created_by")
    private String createdby;

    @Column(name = "created_on")
    private LocalDateTime createdon;

    @Column(name = "item_name", nullable = false)
    private String itemname;

    @Column(name = "updated_by")
    private String updatedby;

    @Column(name = "updated_on")
    private LocalDateTime updatedon;

    @Column(name = "is_active", nullable = false)
    private Boolean isactive = true;

    // One item can be in many quotation items
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Qitem> quotationItems;

    public Item() {
    }

    public Item(String idname, String licensetype, BigDecimal price, String createdby, 
               String itemname, Boolean isactive) {
        this.idname = idname;
        this.licensetype = licensetype;
        this.price = price;
        this.createdby = createdby;
        this.createdon = LocalDateTime.now();
        this.itemname = itemname;
        this.isactive = isactive != null ? isactive : true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdname() {
        return idname;
    }

    public void setIdname(String idname) {
        this.idname = idname;
    }

    public String getLicensetype() {
        return licensetype;
    }

    public void setLicensetype(String licensetype) {
        this.licensetype = licensetype;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public LocalDateTime getCreatedon() {
        return createdon;
    }

    public void setCreatedon(LocalDateTime createdon) {
        this.createdon = createdon;
    }

    public String getItemname() {
        return itemname;
    }

    public void setItemname(String itemname) {
        this.itemname = itemname;
    }

    public String getUpdatedby() {
        return updatedby;
    }

    public void setUpdatedby(String updatedby) {
        this.updatedby = updatedby;
    }

    public LocalDateTime getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(LocalDateTime updatedon) {
        this.updatedon = updatedon;
    }

    public Boolean getIsactive() {
        return isactive;
    }

    public void setIsactive(Boolean isactive) {
        this.isactive = isactive;
    }

    public List<Qitem> getQuotationItems() {
        return quotationItems;
    }

    public void setQuotationItems(List<Qitem> quotationItems) {
        this.quotationItems = quotationItems;
    }

    @PrePersist
    protected void onCreate() {
        createdon = LocalDateTime.now();
        if (isactive == null) {
            isactive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedon = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return id != null && Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : 0;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", idname='" + idname + '\'' +
                ", licensetype='" + licensetype + '\'' +
                ", price=" + price +
                ", itemname='" + itemname + '\'' +
                ", isactive=" + isactive +
                '}';
    }
}