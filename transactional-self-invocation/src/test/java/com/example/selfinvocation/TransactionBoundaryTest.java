package com.example.selfinvocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TransactionBoundaryTest {
    @Autowired UnsafeTransferService unsafe;
    @Autowired SafeTransferService safe;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactionManager;

    // Deliberately no test-level @Transactional: observe completed database work.
    @BeforeEach
    void cleanUp() {
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
        jdbc.update("delete from ledger_entry");
        jdbc.update("delete from audit_entry");
    }

    @Test
    void selfInvocationWithoutOuterTransactionLeavesPartialWrite() {
        assertThatThrownBy(() -> unsafe.transfer("unsafe-debit"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Credit step failed");
        assertThat(ledgerRows()).isEqualTo(1);
    }

    @Test
    void externalCallToSameAnnotatedMethodRollsBack() {
        assertThatThrownBy(() -> unsafe.debitThenFail("external-debit"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ledgerRows()).isZero();
    }

    @Test
    void separateBeanRollsBackFailedTransfer() {
        assertThatThrownBy(() -> safe.transfer("safe-debit"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Credit step failed");
        assertThat(ledgerRows()).isZero();
    }

    @Test
    void activeTransactionDoesNotProveRequiresNewWasApplied() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            assertThat(unsafe.recordThroughSelf("lost-audit")).isTrue();
            assertThat(auditRows()).isEqualTo(1);
            status.setRollbackOnly();
        });
        assertThat(auditRows()).isZero();
    }

    @Test
    void separateBeanAuditSurvivesOuterRollback() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            assertThat(safe.recordThroughCollaborator("kept-audit")).isTrue();
            status.setRollbackOnly();
        });
        assertThat(auditRows()).isEqualTo(1);
        assertThat(jdbc.queryForObject("select id from audit_entry", String.class))
                .isEqualTo("kept-audit");
    }

    @Test
    void selfInvokedAuditAppearsToWorkWhenOuterTransactionCommits() {
        assertThat(unsafe.recordThroughSelf("normal-audit")).isTrue();
        assertThat(auditRows()).isEqualTo(1);
    }

    @Test
    void externalRequiresNewCallSurvivesOuterRollback() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            assertThat(unsafe.recordAudit("external-audit")).isTrue();
            status.setRollbackOnly();
        });
        assertThat(auditRows()).isEqualTo(1);
    }

    private int ledgerRows() {
        return jdbc.queryForObject("select count(*) from ledger_entry", Integer.class);
    }

    private int auditRows() {
        return jdbc.queryForObject("select count(*) from audit_entry", Integer.class);
    }
}
