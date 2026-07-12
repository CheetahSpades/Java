package com.example.delivery.repository;

import com.example.delivery.model.Courier;
import com.example.delivery.model.CourierStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierRepository extends JpaRepository<Courier, Long> {

    List<Courier> findByStatus(CourierStatus status);
}
