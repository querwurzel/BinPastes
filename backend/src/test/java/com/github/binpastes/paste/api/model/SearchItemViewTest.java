package com.github.binpastes.paste.api.model;

import com.github.binpastes.paste.api.model.SearchView.SearchItemView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SearchItemViewTest {

    private static final String content = """
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur egestas odio faucibus mi commodo faucibus. Mauris pellentesque vitae urna sed vehicula. Mauris sollicitudin rutrum condimentum. Sed vel cursus neque, vel imperdiet justo. Integer et volutpat erat, at ullamcorper nisi. Praesent viverra interdum ex, eget scelerisque augue. Nunc sagittis libero quis tristique rutrum. Vestibulum dapibus ex vel auctor mattis. Donec vel vulputate sem, at posuere magna. Curabitur sodales condimentum erat, et pellentesque est viverra quis.
    """.trim();

    @ParameterizedTest
    @DisplayName("highlight - extract excerpt from content")
    @CsvSource(delimiter = '|', textBlock = """
        'elit'   | 'amet, consectetur adipiscing elit. Curabitur egestas odio fauci'
        'foobar' | 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cur'
    """)
    void highlight(String term, String expectedHighlight) {

        assertThat(SearchItemView.highlight(content, term)).isEqualTo(expectedHighlight);

    }
}
