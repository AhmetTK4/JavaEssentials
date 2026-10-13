package com.example.rollback;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "blog_post", uniqueConstraints = @UniqueConstraint(name = "uk_blog_post_slug", columnNames = "slug"))
public class BlogPost {
    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false)
    private String title;

    protected BlogPost() {
    }

    public BlogPost(UUID id, String slug, String title) {
        this.id = id;
        this.slug = slug;
        this.title = title;
    }

    public UUID getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }
}
