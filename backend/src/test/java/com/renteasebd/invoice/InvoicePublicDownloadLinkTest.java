package com.renteasebd.invoice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.renteasebd.domain.property.BdDivision;
import com.renteasebd.domain.property.OccupancyStatus;
import com.renteasebd.domain.property.Property;
import com.renteasebd.domain.property.PropertyType;
import com.renteasebd.domain.property.PropertyUnit;
import com.renteasebd.domain.tenant.EmergencyRelation;
import com.renteasebd.domain.tenant.SecurityDepositStatus;
import com.renteasebd.domain.tenant.Tenant;
import com.renteasebd.domain.tenant.TenantStatus;
import com.renteasebd.domain.user.User;
import com.renteasebd.domain.user.UserRole;
import com.renteasebd.invoice.dto.CreateInvoiceRequest;
import com.renteasebd.invoice.dto.InvoiceResponse;
import com.renteasebd.repository.PropertyRepository;
import com.renteasebd.repository.PropertyUnitRepository;
import com.renteasebd.repository.TenantRepository;
import com.renteasebd.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class InvoicePublicDownloadLinkTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyUnitRepository propertyUnitRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    @Transactional
    void publicInvoiceLink_shouldAllowSingleUseOnly() throws Exception {
        User owner = createOwner();
        Tenant tenant = createTenant(owner);

        var created = invoiceService.generateSingle(
            new CreateInvoiceRequest(tenant.getId(), LocalDate.of(2026, 3, 1), List.of()),
            owner.getId(),
            UserRole.OWNER
        );
        InvoiceResponse sent = invoiceService.send(created.invoice().id(), owner.getId(), UserRole.OWNER);

        String token = extractToken(sent.invoiceDownloadUrl());

        mockMvc.perform(get("/api/v1/public/invoices/download/{token}", token))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/pdf"));

        mockMvc.perform(get("/api/v1/public/invoices/download/{token}", token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVOICE_LINK_ALREADY_USED"));
    }

    private User createOwner() {
        User owner = new User();
        owner.setFullName("Owner Public Link");
        owner.setEmail("owner-public-link@example.com");
        owner.setPasswordHash("hash");
        owner.setRole(UserRole.OWNER);
        owner.setPreferredLanguage("en");
        owner.setActive(true);
        return userRepository.save(owner);
    }

    private Tenant createTenant(User owner) {
        Property property = new Property();
        property.setPropertyName("Public Link Property");
        property.setAddressLine1("Road 1");
        property.setThana("Banani");
        property.setDistrict("Dhaka");
        property.setDivision(BdDivision.DHAKA);
        property.setPropertyType(PropertyType.RESIDENTIAL_FLAT);
        property.setTotalUnits(1);
        property.setOwnerId(owner.getId());
        property = propertyRepository.save(property);

        PropertyUnit unit = new PropertyUnit();
        unit.setPropertyId(property.getId());
        unit.setUnitIdentifier("A-1");
        unit.setOccupancyStatus(OccupancyStatus.OCCUPIED);
        unit = propertyUnitRepository.save(unit);

        Tenant tenant = new Tenant();
        tenant.setFullName("Tenant Public Link");
        tenant.setPhonePrimary("+8801712345678");
        tenant.setPhoneSecondary("+8801912345678");
        tenant.setNidNumber("12345678901234567");
        tenant.setNidImage(new byte[] {1, 2, 3});
        tenant.setNidImageMimeType("image/png");
        tenant.setPermanentAddress("Dhaka City Main Address");
        tenant.setCurrentAddress("Dhaka City Main Address");
        tenant.setEmergencyContactName("Guardian");
        tenant.setEmergencyContactPhone("+8801812345678");
        tenant.setEmergencyContactRelation(EmergencyRelation.FATHER);
        tenant.setLeaseStartDate(LocalDate.of(2026, 1, 1));
        tenant.setLeaseEndDate(LocalDate.of(2026, 12, 31));
        tenant.setMonthlyRentBdt(new BigDecimal("20000.00"));
        tenant.setSecurityDepositBdt(new BigDecimal("25000.00"));
        tenant.setSecurityDepositStatus(SecurityDepositStatus.HELD);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setPropertyUnitId(unit.getId());
        tenant.setCreatedBy(owner.getId());
        return tenantRepository.save(tenant);
    }

    private String extractToken(String downloadUrl) {
        int idx = downloadUrl.lastIndexOf('/');
        return idx < 0 ? downloadUrl : downloadUrl.substring(idx + 1);
    }
}
