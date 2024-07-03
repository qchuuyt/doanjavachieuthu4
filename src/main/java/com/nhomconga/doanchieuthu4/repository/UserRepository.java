package com.nhomconga.doanchieuthu4.repository;


import com.nhomconga.doanchieuthu4.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}

