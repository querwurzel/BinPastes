package com.github.binpastes.paste.api.model;

import com.github.binpastes.paste.domain.Paste;

import java.time.LocalDateTime;
import java.util.List;

public record ListView(
        List<ListItemView> pastes
) {
    public static ListView of(final List<ListItemView> pastes) {
        return new ListView(pastes);
    }

    public record ListItemView (
            String id,
            String title,
            int sizeInBytes,
            boolean isEncrypted,
            LocalDateTime dateCreated,
            LocalDateTime dateOfExpiry
    ) {
        public static ListItemView of(final Paste reference) {
            return new ListItemView(
                    reference.getId(),
                    reference.getTitle(),
                    reference.getContent().getBytes().length,
                    reference.isEncrypted(),
                    reference.getDateCreated(),
                    reference.getDateOfExpiry()
            );
        }
    }

}
