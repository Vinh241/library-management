package com.library.controller;

import com.library.dto.response.BorrowRequestResponse;
import com.library.dto.response.BorrowingTransactionResponse;
import com.library.dto.response.PagedResponse;
import com.library.entity.Account;
import com.library.entity.BorrowingTransaction;
import com.library.service.BorrowRequestService;
import com.library.service.BorrowingTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/borrowing-transactions")
public class BorrowingTransactionController {

    @Autowired
    private BorrowingTransactionService borrowingTransactionService;

    @Autowired
    private BorrowRequestService borrowRequestService;

    /**
     * Tạo transaction từ approved borrow request (manual processing)
     */
    @PostMapping("/from-request/{requestId}")
    public ResponseEntity<?> createTransactionFromRequest(@PathVariable Integer requestId) {
        try {
            Account currentUser = getCurrentUser();
            
            // Chỉ LIBRARIAN, PROVINCE_MANAGER, hoặc ADMIN mới có thể tạo transaction
            if (currentUser.getRole() != Account.Role.LIBRARIAN && 
                currentUser.getRole() != Account.Role.PROVINCE_MANAGER && 
                currentUser.getRole() != Account.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You don't have permission to create borrowing transactions"));
            }

            // Lấy borrow request
            BorrowRequestResponse borrowRequestResponse = borrowRequestService.getBorrowRequestById(requestId);
            if (!"APPROVED".equals(borrowRequestResponse.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Only approved requests can be processed"));
            }

            // Kiểm tra xem đã có transaction chưa
            if (borrowingTransactionService.transactionExistsForBorrowRequest(requestId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Transaction already exists for this request"));
            }

            // Tạo transaction
            throw new RuntimeException("This endpoint is for manual processing only. " +
                    "Transactions are automatically created when requests are approved.");
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while creating transaction"));
        }
    }

    /**
     * Trả sách
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Integer id, 
                                       @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            Account currentUser = getCurrentUser();
            
            // Chỉ LIBRARIAN, PROVINCE_MANAGER, hoặc ADMIN mới có thể xử lý trả sách
            if (currentUser.getRole() != Account.Role.LIBRARIAN && 
                currentUser.getRole() != Account.Role.PROVINCE_MANAGER && 
                currentUser.getRole() != Account.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You don't have permission to process book returns"));
            }

            String notes = requestBody != null ? requestBody.get("notes") : "";
            
            BorrowingTransaction transaction = borrowingTransactionService.returnBook(id, currentUser, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Book returned successfully");
            response.put("transactionId", transaction.getId());
            response.put("returnDate", transaction.getReturnDate());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while returning book"));
        }
    }

    /**
     * Đánh dấu sách bị mất
     */
    @PutMapping("/{id}/lost")
    public ResponseEntity<?> markAsLost(@PathVariable Integer id, 
                                       @RequestBody Map<String, String> requestBody) {
        try {
            Account currentUser = getCurrentUser();
            
            // Chỉ LIBRARIAN, PROVINCE_MANAGER, hoặc ADMIN mới có thể đánh dấu lost
            if (currentUser.getRole() != Account.Role.LIBRARIAN && 
                currentUser.getRole() != Account.Role.PROVINCE_MANAGER && 
                currentUser.getRole() != Account.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You don't have permission to mark books as lost"));
            }

            String notes = requestBody.get("notes");
            if (notes == null || notes.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Notes are required when marking book as lost"));
            }
            
            BorrowingTransaction transaction = borrowingTransactionService.markAsLost(id, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Book marked as lost");
            response.put("transactionId", transaction.getId());
            response.put("status", transaction.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while marking book as lost"));
        }
    }

    /**
     * Kiểm tra xem book item có đang được mượn không
     */
    @GetMapping("/check-borrowed/{bookItemId}")
    public ResponseEntity<?> checkIfBookBorrowed(@PathVariable Integer bookItemId) {
        try {
            boolean isBorrowed = borrowingTransactionService.isBookItemCurrentlyBorrowed(bookItemId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("bookItemId", bookItemId);
            response.put("isBorrowed", isBorrowed);
            
            if (isBorrowed) {
                Optional<BorrowingTransaction> transaction = 
                        borrowingTransactionService.getActiveTransactionByBookItem(bookItemId);
                
                if (transaction.isPresent()) {
                    BorrowingTransaction t = transaction.get();
                    response.put("borrowerName", t.getBorrower().getFullName());
                    response.put("borrowDate", t.getBorrowDate());
                    response.put("dueDate", t.getDueDate());
                    response.put("status", t.getStatus());
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while checking book status"));
        }
    }

    // === GET ENDPOINTS ===

    /**
     * Lấy tất cả borrowing transactions với phân trang
     */
    @GetMapping
    public ResponseEntity<?> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            Account currentUser = getCurrentUser();
            
            // Chỉ LIBRARIAN, PROVINCE_MANAGER, hoặc ADMIN mới có thể xem tất cả transactions
            if (currentUser.getRole() != Account.Role.LIBRARIAN && 
                currentUser.getRole() != Account.Role.PROVINCE_MANAGER && 
                currentUser.getRole() != Account.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You don't have permission to view all transactions"));
            }
            
            PagedResponse<BorrowingTransactionResponse> response = 
                    borrowingTransactionService.getAllTransactions(page, size, sortBy, sortDirection);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while fetching transactions"));
        }
    }

    /**
     * Lấy transaction theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Integer id) {
        try {
            Account currentUser = getCurrentUser();
            
            BorrowingTransactionResponse transaction = borrowingTransactionService.getTransactionById(id);
            
            // User chỉ có thể xem transaction của mình, admin/manager có thể xem tất cả
            if (!currentUser.getId().equals(transaction.getBorrowerId()) && 
                currentUser.getRole() != Account.Role.ADMIN && 
                currentUser.getRole() != Account.Role.PROVINCE_MANAGER && 
                currentUser.getRole() != Account.Role.LIBRARIAN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You can only view your own transactions"));
            }
            
            return ResponseEntity.ok(transaction);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while fetching transaction"));
        }
    }

    /**
     * Lấy transactions theo borrower
     */
    @GetMapping("/borrower/{borrowerId}")
    public ResponseEntity<?> getTransactionsByBorrower(
            @PathVariable Integer borrowerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            Account currentUser = getCurrentUser();
            
            // User chỉ có thể xem transaction của mình, admin/manager có thể xem của người khác
            if (!currentUser.getId().equals(borrowerId) && 
                currentUser.getRole() != Account.Role.ADMIN && 
                currentUser.getRole() != Account.Role.PROVINCE_MANAGER && 
                currentUser.getRole() != Account.Role.LIBRARIAN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You can only view your own transactions"));
            }
            
            PagedResponse<BorrowingTransactionResponse> response = 
                    borrowingTransactionService.getTransactionsByBorrower(borrowerId, page, size, sortBy, sortDirection);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while fetching borrower transactions"));
        }
    }

    /**
     * Lấy transactions theo trạng thái
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getTransactionsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            Account currentUser = getCurrentUser();
            
            // Chỉ LIBRARIAN, PROVINCE_MANAGER, hoặc ADMIN mới có thể xem transactions theo status
            if (currentUser.getRole() != Account.Role.LIBRARIAN && 
                currentUser.getRole() != Account.Role.PROVINCE_MANAGER && 
                currentUser.getRole() != Account.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You don't have permission to view transactions by status"));
            }
            
            BorrowingTransaction.TransactionStatus transactionStatus;
            try {
                transactionStatus = BorrowingTransaction.TransactionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Invalid status. Valid values are: ACTIVE, RETURNED, OVERDUE, LOST"));
            }
            
            PagedResponse<BorrowingTransactionResponse> response = 
                    borrowingTransactionService.getTransactionsByStatus(transactionStatus, page, size, sortBy, sortDirection);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while fetching transactions by status"));
        }
    }

    /**
     * Lấy transactions quá hạn
     */
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueTransactions() {
        try {
            Account currentUser = getCurrentUser();
            
            // Chỉ LIBRARIAN, PROVINCE_MANAGER, hoặc ADMIN mới có thể xem overdue transactions
            if (currentUser.getRole() != Account.Role.LIBRARIAN && 
                currentUser.getRole() != Account.Role.PROVINCE_MANAGER && 
                currentUser.getRole() != Account.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You don't have permission to view overdue transactions"));
            }
            
            List<BorrowingTransactionResponse> response = borrowingTransactionService.getOverdueTransactions();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while fetching overdue transactions"));
        }
    }

    /**
     * Lấy transactions sắp đến hạn
     */
    @GetMapping("/due-soon")
    public ResponseEntity<?> getTransactionsDueSoon(@RequestParam(defaultValue = "7") int daysAhead) {
        try {
            Account currentUser = getCurrentUser();
            
            // Chỉ LIBRARIAN, PROVINCE_MANAGER, hoặc ADMIN mới có thể xem due soon transactions
            if (currentUser.getRole() != Account.Role.LIBRARIAN && 
                currentUser.getRole() != Account.Role.PROVINCE_MANAGER && 
                currentUser.getRole() != Account.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You don't have permission to view due soon transactions"));
            }
            
            List<BorrowingTransactionResponse> response = 
                    borrowingTransactionService.getTransactionsDueSoonResponse(daysAhead);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while fetching due soon transactions"));
        }
    }

    /**
     * Lấy transactions của user hiện tại
     */
    @GetMapping("/my-transactions")
    public ResponseEntity<?> getMyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            Account currentUser = getCurrentUser();
            
            PagedResponse<BorrowingTransactionResponse> response = 
                    borrowingTransactionService.getTransactionsByBorrower(currentUser.getId(), page, size, sortBy, sortDirection);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while fetching your transactions"));
        }
    }


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
