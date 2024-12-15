package com.github.binpastes.paste.application.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.binpastes.paste.domain.Paste;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record SearchView(
    List<SearchItemView> pastes
) {

    @JsonInclude(Include.NON_DEFAULT)
    public record SearchItemView(
        String id,
        Optional<String> title,
        String highlight,
        int sizeInBytes,
        LocalDateTime dateCreated,
        Optional<LocalDateTime> dateOfExpiry
    ) {

        private static final short HIGHLIGHT_RANGE = 30;

        public static SearchItemView of(final Paste reference, final String term) {
            return new SearchItemView(
                reference.getId(),
                reference.getTitle(),
                highlight(reference.getContent(), term),
                reference.getContent().getBytes().length,
                reference.getDateCreated(),
                reference.getDateOfExpiry()
            );
        }

        public static String highlight(final String content, final String term) {
            final int idx = content.toLowerCase().indexOf(term.toLowerCase());

            if (idx == -1) {
                return content.substring(0, Math.min(2 * HIGHLIGHT_RANGE, content.length())).trim();
            }

            final int leftRemainder = Math.abs(Math.min(0, idx - HIGHLIGHT_RANGE));
            final int rightRemainder = Math.max(0, (idx + HIGHLIGHT_RANGE) - content.length());

            return content.substring(
                Math.max(0, idx - HIGHLIGHT_RANGE - rightRemainder),
                Math.min(content.length(), idx + HIGHLIGHT_RANGE + leftRemainder)
            ).trim();
        }
    }
}
