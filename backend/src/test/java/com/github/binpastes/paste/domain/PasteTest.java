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
                () -> Paste.newInstance(null, null, null, false, PasteExposure.PUBLIC, null),
                "content is mandatory"
        );

        assertThrows(
                NullPointerException.class,
                () -> Paste.newInstance(null, "someContent", null, false, null, null),
                "exposure is mandatory"
        );
    }

    @Test
    @DisplayName("new paste - default values are set")
    void newInstanceDefaults() {
        var newPaste = Paste.newInstance(null, "someContent", null, false, PasteExposure.PUBLIC, null);

        assertThat(newPaste.getId()).isNotEmpty();
        assertThat(newPaste.getViews()).isZero();
        assertThat(newPaste.getLastViewed()).isEmpty();
    }

    @Test
    @DisplayName("track paste - increases view count")
    void trackPasteViewCount() {
        var newPaste = Paste.newInstance(null, "someContent", null, false, PasteExposure.PUBLIC, null);

        newPaste.trackView(LocalDateTime.now());
        newPaste.trackView(LocalDateTime.now());

        assertThat(newPaste.getViews()).isEqualTo(2);
    }

    @Test
    @DisplayName("track paste - updates lastViewed timestamp to most recent one")
    void trackPasteLastViewed() {
        var newPaste = Paste.newInstance(null, "someContent", null, false, PasteExposure.PUBLIC, null);
        var today = LocalDateTime.now();
        var yesterday = today.minusDays(1);

        newPaste.trackView(today);
        newPaste.trackView(yesterday);

        assertThat(newPaste.getLastViewed()).hasValue(today);
        assertThat(newPaste.getViews()).isEqualTo(2);
    }

    @ParameterizedTest
    @DisplayName("markAsExpired - sets date of expiry to current timestamp")
    @MethodSource("datesOfExpiry")
    void markAsExpired(LocalDateTime dateOfExpiry, boolean exceptionExpected) {
        var paste = Paste.newInstance(
                "someTitle",
                "someContent",
                dateOfExpiry,
                false,
                PasteExposure.PUBLIC,
                null);

        if (exceptionExpected) {
            assertThrows(IllegalStateException.class, paste::markAsExpired);
            assertThat(paste.getDateOfExpiry()).hasValue(dateOfExpiry);
        } else {
            paste.markAsExpired();

            assertThat(paste.getDateOfExpiry().get())
                    .isCloseTo(LocalDateTime.now(), new TemporalUnitWithinOffset(59, ChronoUnit.SECONDS));
        }
    }

    private static Stream<Arguments> datesOfExpiry() {
        return Stream.of(
                arguments(named("permanent paste that never expires", null), false),
                arguments(named("paste expires in the future", LocalDateTime.now().plusDays(1)), false),
                arguments(named("paste already expired", LocalDateTime.now().minusDays(1)), true)
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
        var unlistedPaste = Paste.newInstance(null, "someContent", null, false, PasteExposure.UNLISTED, "Alice");
        var oneTimePaste = Paste.newInstance(null, "someContent", null, false, PasteExposure.ONCE, "Alice");

        var publicPasteRecentlyCreated = Paste.newInstance(null, "someContent", null, false, PasteExposure.PUBLIC, "Alice");
        publicPasteRecentlyCreated.setDateCreated(LocalDateTime.now().minusMinutes(60).plusSeconds(1));

        var publicPasteCreatedSomeTimeAgo = Paste.newInstance(null, "someContent", null, false, PasteExposure.PUBLIC, "Alice");
        publicPasteCreatedSomeTimeAgo.setDateCreated(LocalDateTime.now().minusMinutes(60));

        return Stream.of(
                arguments(named("unlisted paste", unlistedPaste), null, true),
                arguments(named("one-time paste", oneTimePaste), null, true),

                arguments(named("public paste with recent dateCreated", publicPasteRecentlyCreated), "Alice", true),
                arguments(named("public paste with recent dateCreated requested by foreigner", publicPasteRecentlyCreated), "Bob", false),

                arguments(named("public paste with older dateCreated", publicPasteCreatedSomeTimeAgo), "Alice", false),
                arguments(named("public paste with older dateCreated requested by foreigner", publicPasteCreatedSomeTimeAgo), "Bob", false)
        );
    }
}
