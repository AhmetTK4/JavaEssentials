package com.example.selfinvocation;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferWriter {
    private final JdbcTemplate jdbc;

    public TransferWriter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void debitThenFail(String id) {
        jdbc.update("insert into ledger_entry (id) values (?)", id);
        throw new IllegalStateException("Credit step failed");
    }
}
