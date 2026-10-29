package com.stock.marketscreener.repository;

import com.stock.marketscreener.entity.SignalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignalRepository extends JpaRepository<SignalEntity, Long> {
}