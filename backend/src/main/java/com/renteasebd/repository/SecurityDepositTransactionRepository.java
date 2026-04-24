package com.renteasebd.repository;

import com.renteasebd.domain.deposit.SecurityDepositTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityDepositTransactionRepository extends JpaRepository<SecurityDepositTransaction, Long> {
    List<SecurityDepositTransaction> findByTenantIdOrderByTransactionDateAscIdAsc(Long tenantId);
    List<SecurityDepositTransaction> findByTenantIdOrderByTransactionDateDescIdDesc(Long tenantId);
    List<SecurityDepositTransaction> findByTenantIdInOrderByTransactionDateDescIdDesc(List<Long> tenantIds);
}
