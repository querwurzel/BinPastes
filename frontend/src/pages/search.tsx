import {JSX, createResource, Switch, Match} from 'solid-js';
import {useSearchParams} from '@solidjs/router';
import ApiClient from '../api/client';
import SearchPastes from '../components/SearchPastes/SearchPastes';
import Spinner from '../components/Spinner/Spinner';

const Search: () => JSX.Element = () => {

  const [searchTerm, setSearchTerm] = useSearchParams();

  const [pastes] = createResource(
    effectiveTerm,
    (term) => ApiClient.searchAll(term),
    { initialValue: [] }
  );

  function effectiveTerm(): string | null {
    return (searchTerm.q && searchTerm.q.length >= 3) ? searchTerm.q : null;
  }

  function onSearchPastes(term: string) {
    setSearchTerm({q: term})
  }

  return (
    <>
      <Switch>
        <Match when={pastes.loading}>

          <Spinner />

        </Match>
        <Match when={pastes.latest}>

          <SearchPastes term={effectiveTerm()} pastes={pastes()} onSearchPastes={onSearchPastes} />

        </Match>
      </Switch>
    </>
  )
}

export default Search;
