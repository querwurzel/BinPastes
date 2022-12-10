package it.wylke.binpastes.paste.api.model;

import java.time.LocalDateTime;

public record CreatePaste( // TODO add validation
                           String title,
                           String content,
                           ExpirationRanges expiry
) {

    public enum ExpirationRanges {
        ONE_HOUR {
            @Override
            public LocalDateTime toTimestamp() {
                return LocalDateTime.now().plusHours(1);
            }
        },
        ONE_DAY {
            @Override
            public LocalDateTime toTimestamp() {
                return LocalDateTime.now().plusDays(1);
            }
        },
        ONE_WEEK {
            @Override
            public LocalDateTime toTimestamp() {
                return LocalDateTime.now().plusWeeks(1);
            }
        },
        ONE_MONTH {
            @Override
            public LocalDateTime toTimestamp() {
                return LocalDateTime.now().plusMonths(1);
            }
        },
        ONE_YEAR {
            @Override
            public LocalDateTime toTimestamp() {
                return LocalDateTime.now().plusYears(1);
            }
        },
        NEVER {
            @Override
            public LocalDateTime toTimestamp() {
                return null;
            }
        };

        public abstract LocalDateTime toTimestamp();
    }

}
