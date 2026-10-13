package com.example.rollback;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class SafePublishingFacade {
    private final PublishingAttempt attempt;
    private final FailureAuditService failureAudit;

    public SafePublishingFacade(PublishingAttempt attempt, FailureAuditService failureAudit) {
        this.attempt = attempt;
        this.failureAudit = failureAudit;
    }

    // Deliberately not transactional: each collaborator owns a complete transaction boundary.
    public PublishResult publish(String slug, String title) {
        try {
            attempt.publish(slug, title);
            return PublishResult.success();
        }
        catch (DataIntegrityViolationException ex) {
            failureAudit.recordDuplicateSlug(slug);
            return PublishResult.rejected("Slug already exists");
        }
    }
}
