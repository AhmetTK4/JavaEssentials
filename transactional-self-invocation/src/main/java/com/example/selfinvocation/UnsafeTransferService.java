package com.example.selfinvocation;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class UnsafeTransferService {
    private final JdbcTemplate jdbc;

    public UnsafeTransferService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void transfer(String id) {
        debitThenFail(id);
    }

    @Transactional
    public void debitThenFail(String id) {
        jdbc.update("insert into ledger_entry (id) values (?)", id);
        throw new IllegalStateException("Credit step failed");
    }

    @Transactional
    public boolean recordThroughSelf(String id) {
        return recordAudit(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean recordAudit(String id) {
        jdbc.update("insert into audit_entry (id) values (?)", id);
        return TransactionSynchronizationManager.isActualTransactionActive();
    }
}
