package com.github.binpastes.paste.application.model;

import com.github.binpastes.paste.application.model.SearchView.SearchItemView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SearchItemViewTest {

    private static final String content = """
        Lorum ipsum dolor sit amet, consectetur adipiscing elit. Curabitur egestas amet faucibus mi faucibus.
        Mauris pellentesque vitae urna sed vehicula. Mauris sollicitudin rutrum condimentum. Sed elit cursus neque,
        vel imperdiet justo. Integer et volutpat erat, at ullamcorper nisi. Praesent viverra interdum ex.
    """.trim();

    @ParameterizedTest
    @DisplayName("highlight - extract excerpt from content")
    @CsvSource(delimiter = '|', textBlock = """
        'Match at the beginning'   | 'Lorum'     | 'Lorum ipsum dolor sit amet, consectetur adipiscing elit. Cur'
        'Match case sensitive'     | 'lOrUm'     | 'Lorum ipsum dolor sit amet, consectetur adipiscing elit. Cur'
        'Match twice, first taken' | 'amet'      | 'Lorum ipsum dolor sit amet, consectetur adipiscing elit. Cur'
        'Excerpt is trimmed'       | 'Curabitur' | 'consectetur adipiscing elit. Curabitur egestas amet faucibu'
        'Match at the end'         | 'ex'        | 'pat erat, at ullamcorper nisi. Praesent viverra interdum ex.'
    """)
    void highlight(String scenario, String term, String expectedHighlight) {
        assertThat(SearchItemView.highlight(content, term))
            .as(scenario)
            .isEqualTo(expectedHighlight);
    }
}
