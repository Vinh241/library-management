package com.library.controller;

import com.library.entity.Province;
import com.library.service.ProvinceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/provinces")
@CrossOrigin(origins = "*")
public class ProvinceController {
    
    @Autowired
    private ProvinceService provinceService;
    
    /**
     * GET /api/provinces - Lấy tất cả tỉnh/thành phố
     */
    @GetMapping
    public ResponseEntity<List<Province>> getAllProvinces() {
        try {
            List<Province> provinces = provinceService.getAllProvinces();
            if (provinces.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(provinces, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * GET /api/provinces/{id} - Lấy tỉnh theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Province> getProvinceById(@PathVariable Integer id) {
        try {
            Optional<Province> province = provinceService.getProvinceById(id);
            if (province.isPresent()) {
                return new ResponseEntity<>(province.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * GET /api/provinces/code/{code} - Lấy tỉnh theo code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Province> getProvinceByCode(@PathVariable String code) {
        try {
            Optional<Province> province = provinceService.getProvinceByCode(code);
            if (province.isPresent()) {
                return new ResponseEntity<>(province.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * GET /api/provinces/search?name={name} - Tìm kiếm tỉnh theo tên
     */
    @GetMapping("/search")
    public ResponseEntity<List<Province>> searchProvincesByName(@RequestParam String name) {
        try {
            List<Province> provinces = provinceService.searchProvincesByName(name);
            if (provinces.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(provinces, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * POST /api/provinces - Tạo tỉnh mới
     */
    @PostMapping
    public ResponseEntity<Province> createProvince(@RequestBody Province province) {
        try {
            // Kiểm tra code đã tồn tại chưa
            if (provinceService.isCodeExists(province.getCode())) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            
            Province createdProvince = provinceService.createProvince(province);
            return new ResponseEntity<>(createdProvince, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * PUT /api/provinces/{id} - Cập nhật thông tin tỉnh
     */
    @PutMapping("/{id}")
    public ResponseEntity<Province> updateProvince(@PathVariable Integer id, @RequestBody Province provinceDetails) {
        try {
            Province updatedProvince = provinceService.updateProvince(id, provinceDetails);
            if (updatedProvince != null) {
                return new ResponseEntity<>(updatedProvince, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * DELETE /api/provinces/{id} - Xóa tỉnh
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvince(@PathVariable Integer id) {
        try {
            boolean deleted = provinceService.deleteProvince(id);
            if (deleted) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    

}
