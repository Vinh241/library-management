// File: src/main/java/com/library/repository/LibraryRepository.java
package com.library.repository;

import com.library.entity.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibraryRepository extends JpaRepository<Library, Integer> {

    /**
     * Tìm thư viện theo tên
     */
    List<Library> findByNameContainingIgnoreCase(String name);

    /**
     * Tìm thư viện đang hoạt động
     */
    List<Library> findByIsActiveTrue();

    /**
     * Tìm thư viện theo tỉnh
     */
    List<Library> findByProvinceId(Integer provinceId);
}
