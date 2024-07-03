package com.nhomconga.doanchieuthu4.repository;


import com.nhomconga.doanchieuthu4.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
