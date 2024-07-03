package com.nhomconga.doanchieuthu4.repository;


import com.nhomconga.doanchieuthu4.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
