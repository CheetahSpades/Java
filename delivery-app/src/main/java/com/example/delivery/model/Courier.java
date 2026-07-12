package com.example.delivery.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "couriers")
public class Courier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String phone;

    @Enumerated(EnumType.STRING)
    private CourierStatus status = CourierStatus.AVAILABLE;

    protected Courier() {
        // required by JPA
    }

    public Courier(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public boolean isAvailable() {
        return status == CourierStatus.AVAILABLE;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public CourierStatus getStatus() {
        return status;
    }

    public void setStatus(CourierStatus status) {
        this.status = status;
    }
}
