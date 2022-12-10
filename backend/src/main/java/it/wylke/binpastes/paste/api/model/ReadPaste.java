package it.wylke.binpastes.paste.api.model;

import it.wylke.binpastes.paste.domain.Paste;

import java.time.LocalDateTime;

public record ReadPaste(
        String id,
        String title,
        String content,
        LocalDateTime dateCreated,
        LocalDateTime expiry
) {
    public static ReadPaste from(Paste reference) {
        return new ReadPaste(
                reference.getId(),
                reference.getTitle(),
                reference.getContent(),
                reference.getDateCreated(),
                reference.getDateOfExpiry()
        );
    }
}
