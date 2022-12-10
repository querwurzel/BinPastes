package it.wylke.binpastes.paste.api.model;

import java.util.List;

public record ListPastes(
        List<ReadPaste> pastes
) {
    public static ListPastes from(List<ReadPaste> pastes) {
        return new ListPastes(pastes);
    }
}
