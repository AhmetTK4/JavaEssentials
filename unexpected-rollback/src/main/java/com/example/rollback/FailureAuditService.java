package com.example.rollback;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FailureAuditService {
    private final AuditEntryRepository audits;

    public FailureAuditService(AuditEntryRepository audits) {
        this.audits = audits;
    }

    @Transactional
    public void recordDuplicateSlug(String slug) {
        audits.save(new AuditEntry(
                UUID.randomUUID(),
                "PUBLISH_FAILED",
                "Duplicate slug: " + slug,
                Instant.now()));
    }
}
