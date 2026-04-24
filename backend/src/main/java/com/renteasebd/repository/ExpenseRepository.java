package com.renteasebd.repository;

import com.renteasebd.domain.expense.Expense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByPropertyIdInOrderByExpenseDateDescIdDesc(List<Long> propertyIds);
}
