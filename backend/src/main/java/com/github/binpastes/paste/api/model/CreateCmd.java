package com.github.binpastes.paste.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class CreateCmd {
    @NotBlank
    @Size(max = 255)
    private final String title;
    @NotNull
    @NotBlank
    @Size(min = 5, max = 4096)
    private final String content;
    private final Boolean isEncrypted;
    private final ExpirationRange expiry;
    private final Exposure exposure;

    public CreateCmd(
            final String title,
            final String content,
            final Boolean isEncrypted,
            final ExpirationRange expiry,
            final Exposure exposure
    ) {
        this.title = title;
        this.content = content;
        this.isEncrypted = isEncrypted;
        this.expiry = expiry;
        this.exposure = exposure;
    }

    public String title() {
        return title == null
                ? null
                : title.strip();
    }

    public String content() {
        return content;
    }

    public boolean isEncrypted() {
        return isEncrypted != null && isEncrypted;
    }

    public LocalDateTime dateOfExpiry() {
        return expiry == null
                ? ExpirationRange.ONE_DAY.toTimestamp() // default expiry if not set
                : expiry.toTimestamp();
    }

    public String pasteExposure() {
        return exposure == null
                ? Exposure.PUBLIC.name() // default exposure if not set
                : exposure.name();
    }

    private enum Exposure {
        PUBLIC,
        UNLISTED,
        ONCE
    }

    private enum ExpirationRange {
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
        THREE_MONTHS {
            @Override
            public LocalDateTime toTimestamp() {
                return LocalDateTime.now().plusMonths(3);
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
