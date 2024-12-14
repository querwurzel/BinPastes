package com.github.binpastes.paste.domain;

import com.github.binpastes.paste.domain.Paste.PasteExposure;
import org.assertj.core.data.TemporalUnitWithinOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PasteTest {

    @Test
    @DisplayName("new paste - requires mandatory fields")
    void newInstanceMandatoryFields() {
        assertThrows(
                NullPointerException.class,
                () -> Paste.newInstance(null, null, false, PasteExposure.PUBLIC, null, null),
                "content is mandatory"
        );

        assertThrows(
                NullPointerException.class,
                () -> Paste.newInstance(null, "someContent", false, null, null, null),
                "exposure is mandatory"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> Paste.newInstance(null, "someContent", false, null, LocalDateTime.now(), null),
                "expiry must be in the future"
        );
    }

    @Test
    @DisplayName("new paste - default values are set")
    void newInstanceDefaults() {
        var newPaste = Paste.newInstance(null, "someContent", false, PasteExposure.PUBLIC, null, null);

        assertThat(newPaste.getId()).isNotEmpty();
        assertThat(newPaste.getViews()).isZero();
        assertThat(newPaste.getLastViewed()).isEmpty();
    }

    @Test
    @DisplayName("track paste - increases view count")
    void trackPasteViewCount() {
        var newPaste = Paste.newInstance(null, "someContent", false, PasteExposure.PUBLIC, null, null);

        newPaste.trackView(LocalDateTime.now());
        newPaste.trackView(LocalDateTime.now());

        assertThat(newPaste.getViews()).isEqualTo(2);
    }

    @Test
    @DisplayName("track paste - updates lastViewed timestamp to most recent one")
    void trackPasteLastViewed() {
        var newPaste = Paste.newInstance(null, "someContent", false, PasteExposure.PUBLIC, null, null);
        var today = LocalDateTime.now();
        var yesterday = today.minusDays(1);

        newPaste.trackView(today);
        newPaste.trackView(yesterday);

        assertThat(newPaste.getLastViewed()).hasValue(today);
        assertThat(newPaste.getViews()).isEqualTo(2);
    }

    @ParameterizedTest
    @DisplayName("markAsExpired - set date of expiry to timestamp if not already expired")
    @MethodSource("datesOfExpiry")
    void markAsExpired(LocalDateTime dateOfExpiry, LocalDateTime expectedExpiry) {
        var paste = Paste.newInstance(null, "someContent", false, PasteExposure.PUBLIC, null, null);
        paste.setDateOfExpiry(dateOfExpiry);

        paste.markAsExpired();

        assertThat(paste.getDateOfExpiry().get())
                .isCloseTo(expectedExpiry, new TemporalUnitWithinOffset(3, ChronoUnit.SECONDS));
    }

    private static Stream<Arguments> datesOfExpiry() {
        return Stream.of(
                arguments(named("permanent paste that never expires", null), LocalDateTime.now()),
                arguments(named("paste expires in the future", LocalDateTime.now().plusDays(1)), LocalDateTime.now()),
                arguments(named("paste already expired", LocalDateTime.now().minusDays(1)), LocalDateTime.now().minusDays(1))
        );
    }

    @ParameterizedTest
    @DisplayName("paste is erasable - only under certain conditions")
    @MethodSource("pastesToErase")
    void isErasable(Paste paste, String requestedBy, boolean erasable) {
        assertThat(paste.isErasable(requestedBy))
                .isEqualTo(erasable);
    }

    private static Stream<Arguments> pastesToErase() {
        var unlistedPaste = Paste.newInstance(null, "someContent", false, PasteExposure.UNLISTED, null, "Alice");
        var oneTimePaste = Paste.newInstance(null, "someContent", false, PasteExposure.ONCE, null, "Alice");
        var oneTimePasteExpired = Paste.newInstance(null, "someContent", false, PasteExposure.ONCE, null, "Alice");
        oneTimePasteExpired.setDateOfExpiry(LocalDateTime.now().minusHours(1));

        var publicPasteCreatedLastHour = Paste.newInstance(null, "someContent", false, PasteExposure.PUBLIC, null, "Alice");
        publicPasteCreatedLastHour.setDateCreated(LocalDateTime.now().minusMinutes(60).plusSeconds(1));

        var publicPasteCreatedAnHourAgo = Paste.newInstance(null, "someContent", false, PasteExposure.PUBLIC, null, "Alice");
        publicPasteCreatedAnHourAgo.setDateCreated(LocalDateTime.now().minusMinutes(60));

        return Stream.of(
                arguments(named("unlisted paste", unlistedPaste), null, true),
                arguments(named("one-time paste", oneTimePaste), null, true),
                arguments(named("one-time paste expired", oneTimePasteExpired), null, false),

                arguments(named("public paste created last hour", publicPasteCreatedLastHour), "Alice", true),
                arguments(named("public paste created last hour requested by foreigner", publicPasteCreatedLastHour), "Bob", false),

                arguments(named("public paste created one hour ago", publicPasteCreatedAnHourAgo), "Alice", false),
                arguments(named("public paste created one hour ago requested by foreigner", publicPasteCreatedAnHourAgo), "Bob", false)
        );
    }
}
