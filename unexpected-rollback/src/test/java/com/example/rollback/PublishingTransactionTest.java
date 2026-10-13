package com.example.rollback;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PublishingTransactionTest {
    private static final String EXISTING_SLUG = "spring-transactions";

    @Autowired UnsafePublishingService unsafe;
    @Autowired SafePublishingFacade safe;
    @Autowired BlogPostRepository posts;
    @Autowired AuditEntryRepository audits;

    @BeforeEach
    void seedExistingPost() {
        posts.saveAndFlush(new BlogPost(UUID.randomUUID(), EXISTING_SLUG, "Existing post"));
    }

    @AfterEach
    void cleanUp() {
        audits.deleteAll();
        posts.deleteAll();
    }

    @Test
    void catchingDatabaseExceptionInsideTransactionStillCausesUnexpectedRollback() {
        assertThatThrownBy(() -> unsafe.publish(EXISTING_SLUG, "Duplicate post"))
                .isInstanceOf(UnexpectedRollbackException.class)
                .hasMessageContaining("rollback-only");

        assertThat(posts.count()).isEqualTo(1);
        assertThat(audits.count()).isZero();
    }

    @Test
    void catchingOutsideFailedTransactionAllowsIndependentAuditCommit() {
        PublishResult result = safe.publish(EXISTING_SLUG, "Duplicate post");

        assertThat(result.published()).isFalse();
        assertThat(result.message()).isEqualTo("Slug already exists");
        assertThat(posts.count()).isEqualTo(1);
        assertThat(audits.findAll()).singleElement().satisfies(audit -> {
            assertThat(audit.getAction()).isEqualTo("PUBLISH_FAILED");
            assertThat(audit.getDetail()).isEqualTo("Duplicate slug: " + EXISTING_SLUG);
        });
    }
}
