package com.renteasebd.invoice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class InvoicePaymentEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyUnitRepository propertyUnitRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Test
    @Transactional
    void directPaymentEndpoint_shouldReturn200() throws Exception {
        User owner = new User();
        owner.setFullName("Owner One");
        owner.setEmail("owner-endpoint@example.com");
        owner.setPasswordHash(passwordEncoder.encode("Owner@123"));
        owner.setRole(UserRole.OWNER);
        owner.setPreferredLanguage("en");
        owner.setActive(true);
        owner = userRepository.save(owner);

        Property property = new Property();
        property.setPropertyName("Debug Property");
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
        tenant.setFullName("Tenant One");
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
        tenant = tenantRepository.save(tenant);

        var created = invoiceService.generateSingle(
            new CreateInvoiceRequest(tenant.getId(), LocalDate.of(2026, 3, 1), List.of()),
            owner.getId(),
            UserRole.OWNER
        );
        invoiceService.send(created.invoice().id(), owner.getId(), UserRole.OWNER);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"owner-endpoint@example.com\",\"password\":\"Owner@123\"}"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.path("data").path("accessToken").asText();

        mockMvc.perform(post("/api/v1/invoices/{id}/payments", created.invoice().id())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amountBdt\":500.00,\"paymentDate\":\"2026-04-03\",\"paymentMethod\":\"CASH\",\"notes\":\"debug\"}"))
            .andExpect(status().isOk());
    }
}
