package com.library.dto.response;

import java.util.List;

public class BorrowByTitleResponse {
    private Integer bookTitleId;
    private Integer requestedQuantity;
    private Integer createdRequests;
    private Integer unavailableQuantity;
    private List<BorrowRequestResponse> requests;

    public BorrowByTitleResponse() {}

    public BorrowByTitleResponse(Integer bookTitleId, Integer requestedQuantity, Integer createdRequests, Integer unavailableQuantity, List<BorrowRequestResponse> requests) {
        this.bookTitleId = bookTitleId;
        this.requestedQuantity = requestedQuantity;
        this.createdRequests = createdRequests;
        this.unavailableQuantity = unavailableQuantity;
        this.requests = requests;
    }

    public Integer getBookTitleId() {
        return bookTitleId;
    }

    public void setBookTitleId(Integer bookTitleId) {
        this.bookTitleId = bookTitleId;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(Integer requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public Integer getCreatedRequests() {
        return createdRequests;
    }

    public void setCreatedRequests(Integer createdRequests) {
        this.createdRequests = createdRequests;
    }

    public Integer getUnavailableQuantity() {
        return unavailableQuantity;
    }

    public void setUnavailableQuantity(Integer unavailableQuantity) {
        this.unavailableQuantity = unavailableQuantity;
    }

    public List<BorrowRequestResponse> getRequests() {
        return requests;
    }

    public void setRequests(List<BorrowRequestResponse> requests) {
        this.requests = requests;
    }
}


