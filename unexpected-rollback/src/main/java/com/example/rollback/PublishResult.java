package com.example.rollback;

public record PublishResult(boolean published, String message) {
    public static PublishResult success() {
        return new PublishResult(true, "Post published");
    }

    public static PublishResult rejected(String message) {
        return new PublishResult(false, message);
    }
}
