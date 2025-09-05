package com.library.controller;

import com.library.dto.request.BorrowRequestRequest;
import com.library.dto.request.ReviewBorrowRequestRequest;
import com.library.dto.request.BorrowByTitleRequest;
import com.library.dto.response.BorrowByTitleResponse;
import com.library.dto.response.BorrowRequestResponse;
import com.library.dto.response.PagedResponse;
import com.library.entity.Account;
import com.library.service.BorrowRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow-requests")
public class BorrowRequestController {

    @Autowired
    private BorrowRequestService borrowRequestService;

    // === CREATE BORROW REQUEST ===

    /**
     * Tạo yêu cầu mượn sách mới
     * POST /api/borrow-requests
     */
    @PostMapping
    public ResponseEntity<?> createBorrowRequest(@Valid @RequestBody BorrowRequestRequest request) {
        try {
            Account currentUser = getCurrentUser();
            
            // Tất cả các role đều có thể tạo yêu cầu mượn sách
            // Logic auto-approval sẽ được xử lý trong service layer
            BorrowRequestResponse response = borrowRequestService.createBorrowRequest(request, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while creating borrow request"));
        }
    }

    /**
     * Tạo nhiều yêu cầu mượn theo đầu sách và số lượng
     * POST /api/borrow-requests/by-title
     */
    @PostMapping("/by-title")
    public ResponseEntity<?> createBorrowRequestsByTitle(@Valid @RequestBody BorrowByTitleRequest request) {
        try {
            Account currentUser = getCurrentUser();
            BorrowByTitleResponse response = borrowRequestService.createBorrowRequestsByTitle(request, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while creating borrow requests by title"));
        }
    }

    // === REVIEW BORROW REQUEST ===

    /**
     * Phê duyệt hoặc từ chối yêu cầu mượn sách
     * PUT /api/borrow-requests/{id}/review
     */
    @PutMapping("/{id}/review")
    public ResponseEntity<?> reviewBorrowRequest(@PathVariable Integer id, 
                                               @Valid @RequestBody ReviewBorrowRequestRequest review) {
        try {
            Account currentUser = getCurrentUser();
            
            // Chỉ LIBRARIAN, PROVINCE_MANAGER, hoặc ADMIN mới có thể review
            if (currentUser.getRole() != Account.Role.LIBRARIAN && 
                currentUser.getRole() != Account.Role.PROVINCE_MANAGER && 
                currentUser.getRole() != Account.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You don't have permission to review borrow requests"));
            }

            BorrowRequestResponse response = borrowRequestService.reviewBorrowRequest(id, review, currentUser.getId());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while reviewing borrow request"));
        }
    }

    // === CANCEL BORROW REQUEST ===

    /**
     * Hủy yêu cầu mượn sách
     * PUT /api/borrow-requests/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBorrowRequest(@PathVariable Integer id) {
        try {
            Account currentUser = getCurrentUser();
            BorrowRequestResponse response = borrowRequestService.cancelBorrowRequest(id, currentUser.getId());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while cancelling borrow request"));
        }
    }


    /**
     * Lấy danh sách yêu cầu mượn sách của người dùng hiện tại
     * GET /api/borrow-requests/my-requests
     */
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyBorrowRequests(@RequestParam(defaultValue = "") String status,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        try {
            Account currentUser = getCurrentUser();
            PagedResponse<BorrowRequestResponse> response = borrowRequestService
                    .getUserBorrowRequests(currentUser.getId(), status, page, size);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while fetching borrow requests"));
        }
    }

    /**
     * Lấy danh sách yêu cầu mượn sách của một người dùng cụ thể (cho admin/manager)
     * GET /api/borrow-requests/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBorrowRequests(@PathVariable Integer userId,
                                                 @RequestParam(defaultValue = "") String status,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        try {
            Account currentUser = getCurrentUser();
            
            // Chỉ ADMIN hoặc PROVINCE_MANAGER mới có thể xem yêu cầu của người khác
            if (currentUser.getRole() != Account.Role.ADMIN && 
                currentUser.getRole() != Account.Role.PROVINCE_MANAGER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You don't have permission to view other users' requests"));
            }

            PagedResponse<BorrowRequestResponse> response = borrowRequestService
                    .getUserBorrowRequests(userId, status, page, size);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while fetching borrow requests"));
        }
    }

    /**
     * Lấy danh sách yêu cầu mượn sách theo thư viện (cho thủ thư)
     * GET /api/borrow-requests/library/{libraryId}
     */
    @GetMapping("/library/{libraryId}")
    public ResponseEntity<?> getLibraryBorrowRequests(@PathVariable Integer libraryId,
                                                    @RequestParam(defaultValue = "") String status,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        try {
            Account currentUser = getCurrentUser();
            
            // Kiểm tra quyền truy cập
            if (currentUser.getRole() == Account.Role.LIBRARIAN && 
                !currentUser.getLibraryId().equals(libraryId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You can only view requests for your library"));
            }
            
            if (currentUser.getRole() == Account.Role.READER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You don't have permission to view library requests"));
            }

            PagedResponse<BorrowRequestResponse> response = borrowRequestService
                    .getLibraryBorrowRequests(libraryId, status, page, size);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while fetching library borrow requests"));
        }
    }

    /**
     * Lấy danh sách yêu cầu mượn sách cần được review
     * GET /api/borrow-requests/pending-review
     */
    @GetMapping("/pending-review")
    public ResponseEntity<?> getPendingRequestsForReview(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        try {
            Account currentUser = getCurrentUser();
            
            // Chỉ LIBRARIAN, PROVINCE_MANAGER, hoặc ADMIN mới có thể xem
            if (currentUser.getRole() != Account.Role.LIBRARIAN && 
                currentUser.getRole() != Account.Role.PROVINCE_MANAGER && 
                currentUser.getRole() != Account.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You don't have permission to view pending requests"));
            }

            Integer libraryId = null;
            if (currentUser.getRole() == Account.Role.LIBRARIAN) {
                libraryId = currentUser.getLibraryId();
                if (libraryId == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(createErrorResponse("Librarian must be assigned to a library"));
                }
            }

            PagedResponse<BorrowRequestResponse> response = borrowRequestService
                    .getPendingRequestsForReview(libraryId, page, size);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while fetching pending requests"));
        }
    }

    /**
     * Lấy chi tiết một yêu cầu mượn sách
     * GET /api/borrow-requests/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBorrowRequestById(@PathVariable Integer id) {
        try {
            Account currentUser = getCurrentUser();
            BorrowRequestResponse response = borrowRequestService.getBorrowRequestById(id);
            
            // Kiểm tra quyền truy cập
            boolean hasAccess = false;
            
            switch (currentUser.getRole()) {
                case ADMIN:
                case PROVINCE_MANAGER:
                    hasAccess = true;
                    break;
                case LIBRARIAN:
                    // Librarian chỉ có thể xem request của thư viện mình
                    hasAccess = currentUser.getLibraryId() != null && 
                               response.getBookItemId() != null; // TODO: Check if book belongs to librarian's library
                    break;
                case READER:
                    // Reader chỉ có thể xem request của chính mình
                    hasAccess = response.getRequesterId().equals(currentUser.getId());
                    break;
            }
            
            if (!hasAccess) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You don't have permission to view this request"));
            }

            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while fetching borrow request"));
        }
    }

    // === UTILITY ENDPOINTS ===

    /**
     * Kiểm tra xem người dùng có yêu cầu mượn sách nào đang pending không
     * GET /api/borrow-requests/has-pending
     */
    @GetMapping("/has-pending")
    public ResponseEntity<?> hasActivePendingRequest() {
        try {
            Account currentUser = getCurrentUser();
            boolean hasPending = borrowRequestService.hasActivePendingRequest(currentUser.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("hasPendingRequest", hasPending);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while checking pending requests"));
        }
    }

    // === PRIVATE HELPER METHODS ===

    /**
     * Lấy thông tin người dùng hiện tại từ SecurityContext
     */
    private Account getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Account) authentication.getPrincipal();
    }

    /**
     * Tạo error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
