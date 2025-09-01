package com.library.service;

import com.library.entity.Province;
import com.library.repository.ProvinceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProvinceService {
    
    @Autowired
    private ProvinceRepository provinceRepository;
    
    /**
     * Lấy tất cả tỉnh/thành phố
     */
    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }
    
    /**
     * Lấy tỉnh theo ID
     */
    public Optional<Province> getProvinceById(Integer id) {
        return provinceRepository.findById(id);
    }
    
    /**
     * Lấy tỉnh theo code
     */
    public Optional<Province> getProvinceByCode(String code) {
        return provinceRepository.findByCode(code);
    }
    
    /**
     * Tìm kiếm tỉnh theo tên
     */
    public List<Province> searchProvincesByName(String name) {
        return provinceRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Tạo tỉnh mới
     */
    public Province createProvince(Province province) {
        return provinceRepository.save(province);
    }
    
    /**
     * Cập nhật thông tin tỉnh
     */
    public Province updateProvince(Integer id, Province provinceDetails) {
        Optional<Province> optionalProvince = provinceRepository.findById(id);
        if (optionalProvince.isPresent()) {
            Province province = optionalProvince.get();
            province.setName(provinceDetails.getName());
            province.setCode(provinceDetails.getCode());
            return provinceRepository.save(province);
        }
        return null;
    }
    
    /**
     * Xóa tỉnh
     */
    public boolean deleteProvince(Integer id) {
        if (provinceRepository.existsById(id)) {
            provinceRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * Kiểm tra code đã tồn tại
     */
    public boolean isCodeExists(String code) {
        return provinceRepository.existsByCode(code);
    }
    

}
