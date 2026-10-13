package com.example.rollback;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublishingAttempt {
    private final BlogPostRepository posts;

    public PublishingAttempt(BlogPostRepository posts) {
        this.posts = posts;
    }

    @Transactional
    public void publish(String slug, String title) {
        posts.saveAndFlush(new BlogPost(UUID.randomUUID(), slug, title));
    }
}
