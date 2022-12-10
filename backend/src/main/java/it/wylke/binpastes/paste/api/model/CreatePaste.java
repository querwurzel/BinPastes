package it.wylke.binpastes.paste.api.model;

public record CreatePaste( // TODO validation
        String title,
        String content,
        ExpirationRanges expiry
) {

    public enum ExpirationRanges {
        ONE_HOUR,
        ONE_DAY,
        ONE_WEEK,
        ONE_MONTH,
        ONE_YEAR,
        NEVER
    }

}
