package it.wylke.binpastes.paste.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public record CreateCmd (
                           String title,
                           @NotNull
                           @NotBlank
                           @Size(min = 5)
                           String content,
                           Boolean isEncrypted,
                           ExpirationRange expiry,
                           Exposure exposure
) {

    @Deprecated
    public CreateCmd(String title, String content, String isEncrypted, String expiry, String exposure) {
        this(title, content, Boolean.parseBoolean(isEncrypted), ExpirationRange.valueOf(expiry), Exposure.valueOf(exposure));
    }

    @Override
    public String title() {
        return StringUtils.hasText(title)
                ? title.strip()
                : null;
    }

    @Override
    public String content() {
        return StringUtils.hasText(content)
                ? content
                : null;
    }

    @Override
    public Boolean isEncrypted() {
        return isEncrypted != null && isEncrypted;
    }

    @Override
    @Deprecated
    public Exposure exposure() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public ExpirationRange expiry() {
        throw new UnsupportedOperationException();
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
