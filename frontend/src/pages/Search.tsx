import {JSX, createResource} from 'solid-js';
import {A, useNavigate, useLocation} from '@solidjs/router';
import ApiClient from '../api/client';
import {toDateTimeString} from '../datetime/DateTimeUtil';

const Search: () => JSX.Element = () => {

  const navigate = useNavigate();

  const location = useLocation();

  const [pastes, { mutate, refetch }] = createResource(() => location.query.q, (term) => ApiClient.searchAll(term));

  return (
    <>
      <h2>Search</h2>
      <Show when={pastes() && pastes().length} keyed fallback={<p>Nothing found</p>}>
      <ol>
        <For each={pastes()}>{item =>
        <li>
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
    </>
  )
}

export default Search;
