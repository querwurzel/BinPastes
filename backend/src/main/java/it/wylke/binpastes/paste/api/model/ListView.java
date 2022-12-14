package it.wylke.binpastes.paste.api.model;

import it.wylke.binpastes.paste.domain.Paste;

import java.time.LocalDateTime;
import java.util.List;

public record ListView(
        List<ListItemView> pastes
) {
    public static ListView from(List<ListItemView> pastes) {
        return new ListView(pastes);
    }

    public record ListItemView (
            String id,
            String title,
            boolean isEncrypted,
            LocalDateTime dateCreated,
            LocalDateTime dateOfExpiry
    ) {
        public static ListItemView from(Paste reference) {
            return new ListItemView(
                    reference.getId(),
                    reference.getTitle(),
                    reference.isEncrypted(),
                    reference.getDateCreated(),
                    reference.getDateOfExpiry()
            );
        }
    }

}
