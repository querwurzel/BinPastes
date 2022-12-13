package it.wylke.binpastes.paste.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public record CreateCmd (
                           String title,
                           @NotNull
                           @NotBlank
                           @Size(min = 5)
                           String content,
                           Boolean isEncrypted,
                           String expiry
) {

    private static final Logger log = LoggerFactory.getLogger(CreateCmd.class);

    @Override
    public String title() {
        return StringUtils.hasText(title) ? title.strip() : null;
    }

    @Override
    public String content() {
        return StringUtils.hasText(content) ? content.strip() : null;
    }

    @Override
    public Boolean isEncrypted() {
        return this.isEncrypted != null && this.isEncrypted;
    }

    public LocalDateTime dateOfExpiry() {
        try {
            return ExpirationRanges.valueOf(expiry).toTimestamp();
        } catch (RuntimeException e) {
            log.warn("Could not parse ExpirationRanges of {}: {}", expiry, e.getMessage());
            return ExpirationRanges.ONE_HOUR.toTimestamp();
        }
    }

    private enum ExpirationRanges {
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
