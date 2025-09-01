// File: src/main/java/com/library/service/LibraryService.java
package com.library.service;

import com.library.entity.Library;
import com.library.repository.LibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LibraryService {

    @Autowired
    private LibraryRepository libraryRepository;

    /**
     * Lấy tất cả thư viện
     */
    public List<Library> getAllLibraries() {
        return libraryRepository.findAll();
    }

    /**
     * Lấy thư viện theo ID
     */
    public Optional<Library> getLibraryById(Integer id) {
        return libraryRepository.findById(id);
    }

    /**
     * Tạo thư viện mới
     */
    public Library createLibrary(Library library) {
        return libraryRepository.save(library);
    }

    /**
     * Cập nhật thông tin thư viện
     */
    public Library updateLibrary(Integer id, Library libraryDetails) {
        Optional<Library> optionalLibrary = libraryRepository.findById(id);
        if (optionalLibrary.isPresent()) {
            Library library = optionalLibrary.get();
            library.setName(libraryDetails.getName());
            library.setAddress(libraryDetails.getAddress());
            library.setPhone(libraryDetails.getPhone());
            library.setEmail(libraryDetails.getEmail());
            library.setIsActive(libraryDetails.getIsActive());
            return libraryRepository.save(library);
        }
        return null;
    }

    /**
     * Xóa thư viện
     */
    public boolean deleteLibrary(Integer id) {
        if (libraryRepository.existsById(id)) {
            libraryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
