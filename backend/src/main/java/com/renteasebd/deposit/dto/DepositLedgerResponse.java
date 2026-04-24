package com.renteasebd.deposit.dto;

import java.util.List;

public record DepositLedgerResponse(
    DepositBalanceResponse balance,
    List<DepositTransactionResponse> transactions
) {
}
