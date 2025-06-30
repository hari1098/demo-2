package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customerid")
    private Long id;

    @Column(name = "customer_name", nullable = false)
    private String customername;

    @Column(name = "email_id", unique = true, nullable = false)
    private String emailid;

    @Column(name = "mobile_number", nullable = false)
    private Long mobilenumber;

    @Column(name = "company_name")
    private String companyname;

    @Column(name = "address")
    private String address;

    @Column(name = "reffered_by")
    private String refferedby;

    @Column(name = "user_no", nullable = false)
    private Integer userno;

    // One customer can have many quotations
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Quat> quotations;

    public Customer() {
    }

    public Customer(String customername, String emailid, Long mobilenumber, String companyname, 
                   String address, String refferedby, Integer userno) {
        this.customername = customername;
        this.emailid = emailid;
        this.mobilenumber = mobilenumber;
        this.companyname = companyname;
        this.address = address;
        this.refferedby = refferedby;
        this.userno = userno;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomername() {
        return customername;
    }

    public void setCustomername(String customername) {
        this.customername = customername;
    }

    public String getEmailid() {
        return emailid;
    }

    public void setEmailid(String emailid) {
        this.emailid = emailid;
    }

    public Long getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(Long mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRefferedby() {
        return refferedby;
    }

    public void setRefferedby(String refferedby) {
        this.refferedby = refferedby;
    }

    public Integer getUserno() {
        return userno;
    }

    public void setUserno(Integer userno) {
        this.userno = userno;
    }

    public List<Quat> getQuotations() {
        return quotations;
    }

    public void setQuotations(List<Quat> quotations) {
        this.quotations = quotations;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", customername='" + customername + '\'' +
                ", emailid='" + emailid + '\'' +
                ", mobilenumber=" + mobilenumber +
                ", companyname='" + companyname + '\'' +
                ", address='" + address + '\'' +
                ", refferedby='" + refferedby + '\'' +
                ", userno=" + userno +
                '}';
    }
}