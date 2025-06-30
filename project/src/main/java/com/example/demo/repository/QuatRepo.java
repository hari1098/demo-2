package com.example.demo.repository;

import com.example.demo.model.Quat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuatRepo extends JpaRepository<Quat, Long> {
    
    @Query("SELECT q FROM Quat q WHERE q.customer.id = :customerId")
    List<Quat> findByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT q FROM Quat q WHERE q.user.id = :userId")
    List<Quat> findByUserId(@Param("userId") Long userId);
    
    boolean existsByQuatno(String quatno);
}