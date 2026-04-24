package com.renteasebd.expense;

import com.renteasebd.common.ApiResponse;
import com.renteasebd.expense.dto.CreateExpenseRequest;
import com.renteasebd.expense.dto.ExpenseResponse;
import com.renteasebd.expense.dto.UpdateExpenseRequest;
import com.renteasebd.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<List<ExpenseResponse>> list(
        @RequestParam(required = false) Long propertyId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(expenseService.list(principal.id(), principal.role(), propertyId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<ExpenseResponse> get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(expenseService.get(id, principal.id(), principal.role()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<ExpenseResponse> create(
        @Valid @ModelAttribute CreateExpenseRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(expenseService.create(request, principal.id(), principal.role()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<ExpenseResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateExpenseRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(expenseService.update(id, request, principal.id(), principal.role()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        expenseService.delete(id, principal.id(), principal.role());
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/receipt")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<byte[]> getReceipt(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        ExpenseService.ReceiptFile file = expenseService.getReceipt(id, principal.id(), principal.role());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=expense-receipt-" + id)
            .contentType(MediaType.parseMediaType(file.mimeType()))
            .body(file.bytes());
    }

    @PutMapping(path = "/{id}/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<ExpenseResponse> upsertReceipt(
        @PathVariable Long id,
        @RequestParam("receipt") MultipartFile receipt,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(expenseService.upsertReceipt(id, receipt, principal.id(), principal.role()));
    }

    @DeleteMapping("/{id}/receipt")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<ExpenseResponse> removeReceipt(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(expenseService.removeReceipt(id, principal.id(), principal.role()));
    }
}
