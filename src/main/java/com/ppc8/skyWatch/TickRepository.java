package com.ppc8.skyWatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface TickRepository extends JpaRepository<Tick, String> {
    @Modifying
    @Query(value = "INSERT INTO tick (id, open, close, volume, transactions, volume_weighted_price, date, time, market, trend) " +
            "VALUES (:id, :open, :close, :volume, :transactions, :volumeWeightedPrice, :date, :time, :market, :trend) " +
            "ON CONFLICT DO NOTHING", nativeQuery = true)

    void insertIfNotExists(
            @Param("id") String id,
            @Param("open") double open,
            @Param("close") double close,
            @Param("volume") long volume,
            @Param("transactions") int transactions,
            @Param("volumeWeightedPrice") double volumeWeightedPrice,
            @Param("date") LocalDate date,
            @Param("time") LocalTime time,
            @Param("market") String market,
            @Param("trend") int trend
    );
}