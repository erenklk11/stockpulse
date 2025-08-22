package com.erenkalkan.stockpulse.repository;

import com.erenkalkan.stockpulse.model.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
}
