package com.github.binpastes.paste.application.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.binpastes.paste.domain.Paste;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record ListView(
        List<ListItemView> pastes
) {
    public static ListView of(final List<ListItemView> pastes) {
        return new ListView(pastes);
    }

    @JsonInclude(Include.NON_DEFAULT)
    public record ListItemView(
            String id,
            Optional<String> title,
            int sizeInBytes,
            boolean isEncrypted,
            boolean isPermanent,
            LocalDateTime dateCreated,
            Optional<LocalDateTime> dateOfExpiry
    ) {
        public static ListItemView of(final Paste reference) {
            return new ListItemView(
                    reference.getId(),
                    reference.getTitle(),
                    reference.getContent().getBytes().length,
                    reference.isEncrypted(),
                    reference.isPermanent(),
                    reference.getDateCreated(),
                    reference.getDateOfExpiry()
            );
        }
    }

}
