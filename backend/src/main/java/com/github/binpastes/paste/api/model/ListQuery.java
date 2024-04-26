package com.github.binpastes.paste.api.model;

import com.github.binpastes.paste.domain.Paste;

import java.time.LocalDateTime;
import java.util.List;

public record ListQuery(
        List<ListItemQuery> pastes
) {
    public static ListQuery of(final List<ListItemQuery> pastes) {
        return new ListQuery(pastes);
    }

    public record ListItemQuery(
            String id,
            String title,
            int sizeInBytes,
            boolean isEncrypted,
            LocalDateTime dateCreated,
            LocalDateTime dateOfExpiry
    ) {
        public static ListItemQuery of(final Paste reference) {
            return new ListItemQuery(
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
