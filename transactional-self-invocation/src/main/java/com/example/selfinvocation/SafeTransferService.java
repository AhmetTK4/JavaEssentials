package com.example.selfinvocation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SafeTransferService {
    private final TransferWriter transfers;
    private final AuditWriter audits;

    public SafeTransferService(TransferWriter transfers, AuditWriter audits) {
        this.transfers = transfers;
        this.audits = audits;
    }

    public void transfer(String id) {
        transfers.debitThenFail(id);
    }

    @Transactional
    public boolean recordThroughCollaborator(String id) {
        return audits.recordAudit(id);
    }
}
