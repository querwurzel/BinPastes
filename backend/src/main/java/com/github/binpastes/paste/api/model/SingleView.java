package com.github.binpastes.paste.api.model;

import com.github.binpastes.paste.domain.Paste;

import java.time.LocalDateTime;

public record SingleView(
        String id,
        String title,
        String content,
        int sizeInBytes,
        boolean isErasable,
        boolean isEncrypted,
        boolean isOneTime,
        LocalDateTime dateCreated,
        LocalDateTime dateOfExpiry,
        LocalDateTime lastViewed,
        long views
) {
    public static SingleView from(Paste reference) {
        return new SingleView(
                reference.getId(),
                reference.getTitle(),
                reference.getContent(),
                reference.getContent().getBytes().length,
                reference.isErasable(),
                reference.isEncrypted(),
                reference.isOneTime(),
                reference.getDateCreated(),
                reference.getDateOfExpiry(),
                reference.getLastViewed(),
                reference.getViews()
        );
    }
}
