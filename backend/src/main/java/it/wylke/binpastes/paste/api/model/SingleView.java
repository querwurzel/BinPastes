package it.wylke.binpastes.paste.api.model;

import it.wylke.binpastes.paste.domain.Paste;

import java.time.LocalDateTime;

public record SingleView(
        String id,
        String title,
        String content,
        long views,
        boolean isEncrypted,
        boolean isOneTime,
        LocalDateTime dateCreated,
        LocalDateTime dateOfExpiry
) {
    public static SingleView from(Paste reference) {
        return new SingleView(
                reference.getId(),
                reference.getTitle(),
                reference.getContent(),
                reference.getViews(),
                reference.isEncrypted(),
                reference.isOneTime(),
                reference.getDateCreated(),
                reference.getDateOfExpiry()
        );
    }
}
