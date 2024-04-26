import {Component, JSX, Show, For} from 'solid-js';
import {A} from '@solidjs/router';
import {PasteSearchView} from '../../api/model/PasteSearchView';
import {toDateTimeString} from '../../datetime/DateTimeUtil';
import styles from "./searchPastes.module.css";

type SearchPastesProps = {
  term: string
  pastes: Array<PasteSearchView>
  onSearchPastes: (term: string) => void
}

const SearchPastes: Component<SearchPastesProps> = ({term, pastes, onSearchPastes}): JSX.Element => {

  let searchInput: HTMLInputElement;

  function submitOnClick(e: Event) {
    e.preventDefault();

    if (searchInput.value?.length >= 3) {
      onSearchPastes(searchInput.value);
    }
  }

  function submitOnEnter(e: Event) {
    if (e instanceof KeyboardEvent && e.key === "Enter") {
      submitOnClick(e);
    }
  }

  return (
    <>
      <form autocomplete="off" class={styles.searchForm} onSubmit={submitOnClick}>
        <fieldset>
          <input ref={searchInput} onKeyUp={submitOnEnter} name="term" value={term} type="search" required minlength="3" maxlength="25" placeholder="Search for pastes" autofocus />
          <input type="submit" value="Search"/>
          <input type="reset" value="Reset"/>
        </fieldset>
      </form>

      <Show when={term}>
      <div class={styles.searchResults}>
        <Show when={pastes.length} fallback={<span>Nothing found</span>}>
        <ol>
          <For each={pastes}>{item =>
          <li class={styles.item}>
            <p><A href={'/paste/' + item.id}>{item.title || 'Untitled' }</A></p>
            <p>
              Created: <time>{toDateTimeString(item.dateCreated)}</time> |
              Expires: <time>{item.dateOfExpiry ? toDateTimeString(item.dateOfExpiry) : 'Never'}</time> |
              Size: {item.sizeInBytes} bytes
            </p>
            <pre><i>“{item.highlight} [..]”</i></pre>
          </li>
          }
          </For>
        </ol>
        </Show>
      </div>
      </Show>
    </>
  )
}

export default SearchPastes
