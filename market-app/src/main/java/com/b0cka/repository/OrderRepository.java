package com.b0cka.repository;

import com.b0cka.models.Order;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {
    Flux<Order> findAllByUserId(String username);
}
