package com.example.demo.dto;

import java.time.LocalDateTime;

public class QuatCreateRequest {
    private String quatno;
    private LocalDateTime quatDate;
    private Integer validity;
    private Long customerId;
    private Long userId;

    public QuatCreateRequest() {
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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}