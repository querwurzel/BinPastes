package com.github.binpastes.paste.domain;

import com.github.binpastes.paste.domain.Paste.PasteExposure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @DisplayName("new paste - sets defaults")
    void newInstanceDefaults() {
        var newPaste = Paste.newInstance(null, "someContent", null, false, PasteExposure.PUBLIC, null);

        assertThat(newPaste.getId()).isNotEmpty();
        assertThat(newPaste.getViews()).isZero();
        assertThat(newPaste.getLastViewed()).isNull();

        assertThat(newPaste.getTitle()).isNull();
        assertThat(newPaste.getContent()).isEqualTo("someContent");
        assertThat(newPaste.isPermanent()).isTrue();
        assertThat(newPaste.isEncrypted()).isFalse();
        assertThat(newPaste.isPublic()).isTrue();
        assertThat(newPaste.getRemoteAddress()).isNull();
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
        var now = LocalDateTime.now();
        var yesterday = LocalDateTime.now().minusDays(1);

        newPaste.trackView(now);
        newPaste.trackView(yesterday);

        assertThat(newPaste.getLastViewed()).isEqualTo(now);
        assertThat(newPaste.getViews()).isEqualTo(2);
    }

    @ParameterizedTest
    @DisplayName("erase paste - allowed only under certain conditions")
    @MethodSource("pastesToErase")
    void isErasable(Paste paste, String requestedBy, boolean erasable) {
        assertThat(paste.isErasable(requestedBy))
                .isEqualTo(erasable);
    }

    private static Stream<Arguments> pastesToErase() {
        var unlistedPaste = Paste.newInstance(null, "someContent", null, false, PasteExposure.UNLISTED, "Alice");
        var oneTimePaste = Paste.newInstance(null, "someContent", null, false, PasteExposure.ONCE, "Alice");

        var publicPasteRecentlyCreatedByAlice = Paste.newInstance(null, "someContent", null, false, PasteExposure.PUBLIC, "Alice");
        publicPasteRecentlyCreatedByAlice.setDateCreated(LocalDateTime.now().minusMinutes(59));

        var publicPasteCreatedSomeTimeAgoByAlice = Paste.newInstance(null, "someContent", null, false, PasteExposure.PUBLIC, "Alice");
        publicPasteCreatedSomeTimeAgoByAlice.setDateCreated(LocalDateTime.now().minusMinutes(60));

        return Stream.of(
                arguments(unlistedPaste, null, true),
                arguments(oneTimePaste, null, true),

                arguments(publicPasteRecentlyCreatedByAlice, "Alice", true),
                arguments(publicPasteRecentlyCreatedByAlice, "Bob", false),

                arguments(publicPasteCreatedSomeTimeAgoByAlice, "Alice", false),
                arguments(publicPasteCreatedSomeTimeAgoByAlice, "Bob", false)
        );
    }
}
