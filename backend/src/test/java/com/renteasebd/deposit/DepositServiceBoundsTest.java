package com.renteasebd.deposit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.renteasebd.common.AppException;
import com.renteasebd.deposit.dto.CreateDepositTransactionRequest;
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
import com.renteasebd.repository.PropertyRepository;
import com.renteasebd.repository.PropertyUnitRepository;
import com.renteasebd.repository.TenantRepository;
import com.renteasebd.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class DepositServiceBoundsTest {

    @Autowired
    private DepositService depositService;

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
    void createTransaction_shouldRejectOutflowAboveCurrentBalance() {
        User owner = new User();
        owner.setFullName("Deposit Owner");
        owner.setEmail("deposit-owner@example.com");
        owner.setPasswordHash("hash");
        owner.setRole(UserRole.OWNER);
        owner.setPreferredLanguage("en");
        owner.setActive(true);
        owner = userRepository.save(owner);

        Property property = new Property();
        property.setPropertyName("Deposit Property");
        property.setAddressLine1("Road 11");
        property.setThana("Banani");
        property.setDistrict("Dhaka");
        property.setDivision(BdDivision.DHAKA);
        property.setPropertyType(PropertyType.RESIDENTIAL_FLAT);
        property.setTotalUnits(1);
        property.setOwnerId(owner.getId());
        property = propertyRepository.save(property);

        PropertyUnit unit = new PropertyUnit();
        unit.setPropertyId(property.getId());
        unit.setUnitIdentifier("D-1");
        unit.setOccupancyStatus(OccupancyStatus.OCCUPIED);
        unit = propertyUnitRepository.save(unit);

        Tenant tenant = new Tenant();
        tenant.setFullName("Tenant Deposit");
        tenant.setPhonePrimary("+8801712345678");
        tenant.setPhoneSecondary("+8801912345678");
        tenant.setNidNumber("12345678901234567");
        tenant.setNidImage(new byte[] {1, 2, 3});
        tenant.setNidImageMimeType("image/png");
        tenant.setPermanentAddress("Dhaka Main Address");
        tenant.setCurrentAddress("Dhaka Main Address");
        tenant.setEmergencyContactName("Guardian");
        tenant.setEmergencyContactPhone("+8801812345678");
        tenant.setEmergencyContactRelation(EmergencyRelation.FATHER);
        tenant.setLeaseStartDate(LocalDate.of(2026, 1, 1));
        tenant.setLeaseEndDate(LocalDate.of(2026, 12, 31));
        tenant.setMonthlyRentBdt(new BigDecimal("18000.00"));
        tenant.setSecurityDepositBdt(new BigDecimal("1000.00"));
        tenant.setSecurityDepositStatus(SecurityDepositStatus.HELD);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setPropertyUnitId(unit.getId());
        tenant.setCreatedBy(owner.getId());
        tenant = tenantRepository.save(tenant);
        final Long tenantId = tenant.getId();
        final Long ownerId = owner.getId();

        AppException error = assertThrows(
            AppException.class,
            () -> depositService.createTransaction(
                tenantId,
                new CreateDepositTransactionRequest(
                    "REFUND",
                    new BigDecimal("1200.00"),
                    "Over-refund",
                    LocalDate.of(2026, 3, 5)
                ),
                ownerId,
                UserRole.OWNER
            )
        );

        assertEquals("DEPOSIT_BOUNDS_EXCEEDED", error.getCode());
    }
}
