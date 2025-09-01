package com.library.repository;

import com.library.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Integer> {
    
    /**
     * Tìm tỉnh theo code
     */
    Optional<Province> findByCode(String code);
    
    /**
     * Tìm tỉnh theo tên (không phân biệt hoa thường)
     */
    @Query("SELECT p FROM Province p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Province> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Kiểm tra xem code đã tồn tại chưa
     */
    boolean existsByCode(String code);
}
