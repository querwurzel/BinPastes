package it.wylke.binpastes.paste.api.model;

import java.util.List;

public record ListView(
        List<SingleView> pastes
) {
    public static ListView from(List<SingleView> pastes) {
        return new ListView(pastes);
    }
}
