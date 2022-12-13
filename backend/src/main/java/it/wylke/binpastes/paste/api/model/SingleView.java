package it.wylke.binpastes.paste.api.model;

import it.wylke.binpastes.paste.domain.Paste;

import java.time.LocalDateTime;

public record SingleView(
        String id,
        String title,
        String content,
        boolean isEncrypted,
        LocalDateTime dateCreated,
        LocalDateTime dateOfExpiry
) {
    public static SingleView from(Paste reference) {
        return new SingleView(
                reference.getId(),
                reference.getTitle(),
                reference.getContent(),
                reference.isEncrypted(),
                reference.getDateCreated(),
                reference.getDateOfExpiry()
        );
    }
}
