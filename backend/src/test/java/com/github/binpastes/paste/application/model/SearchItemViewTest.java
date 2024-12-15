package com.github.binpastes.paste.application.model;

import com.github.binpastes.paste.application.model.SearchView.SearchItemView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SearchItemViewTest {

    private static final String content = """
        Lorum ipsum dolor sit amet, consectetur adipiscing elit. Curabitur egestas amet faucibus mi faucibus.
        Mauris pellentesque vitae urna sed vehicula. Mauris sollicitudin rutrum condimentum. Sed elit cursus neque,
        vel imperdiet justo. Integer et volutpat erat, at ullamcorper nisi. Praesent viverra interdum ex.
    """.trim();

    @ParameterizedTest(name = "{0}")
    @DisplayName("highlight - extract excerpt from content")
    @CsvSource(delimiter = '|', textBlock = """
        'match at the beginning'   | 'Lorum'     | 'Lorum ipsum dolor sit amet, consectetur adipiscing elit. Cur'
        'match case sensitive'     | 'LoRuM'     | 'Lorum ipsum dolor sit amet, consectetur adipiscing elit. Cur'
        'match twice, first taken' | 'sed'       | 'auris pellentesque vitae urna sed vehicula. Mauris sollicitu'
        'excerpt is trimmed'       | 'Curabitur' | 'consectetur adipiscing elit. Curabitur egestas amet faucibu'
        'match at the end'         | 'ex'        | 'pat erat, at ullamcorper nisi. Praesent viverra interdum ex.'
    """)
    void highlight(String scenario, String term, String expectedHighlight) {
        assertThat(SearchItemView.highlight(content, term))
            .isEqualTo(expectedHighlight);
    }

    @Test
    @DisplayName("highlight - content very short")
    void highlight() {
        assertThat(SearchItemView.highlight("Hello Short World", "short"))
            .isEqualTo("Hello Short World");
    }
}
