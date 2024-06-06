package com.github.binpastes.paste.application.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.binpastes.paste.domain.Paste;

import java.time.LocalDateTime;

@JsonInclude(Include.NON_DEFAULT)
public record DetailView(
        String id,
        String title,
        String content,
        int sizeInBytes,
        boolean isPublic,
        boolean isErasable,
        boolean isEncrypted,
        boolean isOneTime,
        boolean isPermanent,
        LocalDateTime dateCreated,
        LocalDateTime dateOfExpiry,
        LocalDateTime lastViewed,
        long views
) {
    public static DetailView of(final Paste reference, final String remoteAddress) {
        return new DetailView(
                reference.getId(),
                reference.getTitle(),
                reference.getContent(),
                reference.getContent().getBytes().length,
                reference.isPublic(),
                reference.isErasable(remoteAddress),
                reference.isEncrypted(),
                reference.isOneTime(),
                reference.isPermanent(),
                reference.getDateCreated(),
                reference.getDateOfExpiry(),
                reference.getLastViewed(),
                reference.getViews()
        );
    }
}
