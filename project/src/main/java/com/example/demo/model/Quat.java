package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quat")
public class Quat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quat_id")
    private Long id;

    @Column(name = "quatno", unique = true, nullable = false)
    private String quatno;

    @Column(name = "quat_date", nullable = false)
    private LocalDateTime quatDate;

    @Column(name = "validity", nullable = false)
    private Integer validity;

    // Foreign key to Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "FK_quat_customer"))
    private Customer customer;

    // Foreign key to Login (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_quat_user"))
    private Login user;

    // One quotation can have many quotation items
    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Qitem> quotationItems;

    public Quat() {
    }

    public Quat(String quatno, LocalDateTime quatDate, Integer validity, Customer customer, Login user) {
        this.quatno = quatno;
        this.quatDate = quatDate;
        this.validity = validity;
        this.customer = customer;
        this.user = user;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuatno() {
        return quatno;
    }

    public void setQuatno(String quatno) {
        this.quatno = quatno;
    }

    public LocalDateTime getQuatDate() {
        return quatDate;
    }

    public void setQuatDate(LocalDateTime quatDate) {
        this.quatDate = quatDate;
    }

    public Integer getValidity() {
        return validity;
    }

    public void setValidity(Integer validity) {
        this.validity = validity;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Login getUser() {
        return user;
    }

    public void setUser(Login user) {
        this.user = user;
    }

    public List<Qitem> getQuotationItems() {
        return quotationItems;
    }

    public void setQuotationItems(List<Qitem> quotationItems) {
        this.quotationItems = quotationItems;
    }

    // Helper methods for backward compatibility
    public Long getCustomerId() {
        return customer != null ? customer.getId() : null;
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    @Override
    public String toString() {
        return "Quat{" +
                "id=" + id +
                ", quatno='" + quatno + '\'' +
                ", quatDate=" + quatDate +
                ", validity=" + validity +
                ", customerId=" + getCustomerId() +
                ", userId=" + getUserId() +
                '}';
    }
}