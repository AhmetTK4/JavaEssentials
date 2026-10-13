package com.example.rollback;

import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnsafePublishingService {
    private final BlogPostRepository posts;
    private final AuditEntryRepository audits;

    public UnsafePublishingService(BlogPostRepository posts, AuditEntryRepository audits) {
        this.posts = posts;
        this.audits = audits;
    }

    @Transactional
    public PublishResult publish(String slug, String title) {
        try {
            posts.saveAndFlush(new BlogPost(UUID.randomUUID(), slug, title));
            return PublishResult.success();
        }
        catch (DataIntegrityViolationException ex) {
            // The catch changes control flow, but the current transaction is already rollback-only.
            audits.save(new AuditEntry(
                    UUID.randomUUID(),
                    "PUBLISH_FAILED",
                    "Duplicate slug: " + slug,
                    Instant.now()));
            return PublishResult.rejected("Slug already exists");
        }
    }
}
