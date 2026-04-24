package com.renteasebd.expense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.renteasebd.domain.property.BdDivision;
import com.renteasebd.domain.property.Property;
import com.renteasebd.domain.property.PropertyType;
import com.renteasebd.domain.user.User;
import com.renteasebd.domain.user.UserRole;
import com.renteasebd.expense.dto.CreateExpenseRequest;
import com.renteasebd.repository.PropertyRepository;
import com.renteasebd.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class ExpenseServiceReceiptTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Test
    @Transactional
    void createExpense_shouldPersistAndServeReceipt() {
        User owner = new User();
        owner.setFullName("Expense Owner");
        owner.setEmail("expense-owner@example.com");
        owner.setPasswordHash("hash");
        owner.setRole(UserRole.OWNER);
        owner.setPreferredLanguage("en");
        owner.setActive(true);
        owner = userRepository.save(owner);

        Property property = new Property();
        property.setPropertyName("Expense Property");
        property.setAddressLine1("Road 2");
        property.setThana("Banani");
        property.setDistrict("Dhaka");
        property.setDivision(BdDivision.DHAKA);
        property.setPropertyType(PropertyType.RESIDENTIAL_FLAT);
        property.setTotalUnits(8);
        property.setOwnerId(owner.getId());
        property = propertyRepository.save(property);

        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setPropertyId(property.getId());
        request.setCategory("Maintenance");
        request.setAmountBdt(new BigDecimal("550.50"));
        request.setExpenseDate(LocalDate.of(2026, 3, 4));
        request.setDescription("Lift repair");
        request.setReceipt(new MockMultipartFile(
            "receipt",
            "receipt.pdf",
            "application/pdf",
            new byte[] {'%', 'P', 'D', 'F', '-', '1', '.', '7', '\n'}
        ));

        var created = expenseService.create(request, owner.getId(), UserRole.OWNER);
        assertNotNull(created.id());
        assertTrue(created.hasReceipt());
        assertEquals("application/pdf", created.receiptMimeType());

        var receipt = expenseService.getReceipt(created.id(), owner.getId(), UserRole.OWNER);
        assertEquals("application/pdf", receipt.mimeType());
        assertTrue(receipt.bytes().length > 0);

        var removed = expenseService.removeReceipt(created.id(), owner.getId(), UserRole.OWNER);
        assertFalse(removed.hasReceipt());
    }
}
