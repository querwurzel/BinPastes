package com.github.binpastes.paste.api.model;

import com.github.binpastes.paste.domain.Paste;

import java.time.LocalDateTime;
import java.util.List;

public record SearchView(
        List<SearchItemView> pastes
) {

    public static SearchView of(List<SearchItemView> pastes) {
        return new SearchView(pastes);
    }

    public record SearchItemView(
            String id,
            String title,
            String highlight,
            int sizeInBytes,
            LocalDateTime dateCreated,
            LocalDateTime dateOfExpiry
    ) {

        private static final short HIGHLIGHT_RANGE = 25;

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
            final int idx = content.indexOf(term);

            if (idx == -1) {
                return content.substring(0, Math.min(2 * HIGHLIGHT_RANGE, content.length()));
            }

            return content.substring(
                    Math.max(0, idx - HIGHLIGHT_RANGE),
                    Math.min(content.length(), idx + term.length() + HIGHLIGHT_RANGE)
            );
        }
    }
}
