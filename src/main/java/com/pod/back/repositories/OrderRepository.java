package com.pod.back.repositories;

import com.pod.back.entities.Order;
import com.pod.back.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}
