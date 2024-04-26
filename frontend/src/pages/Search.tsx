import {JSX, createResource, Switch, Match} from 'solid-js';
import {A, useLocation, useSearchParams} from '@solidjs/router';
import ApiClient from '../api/client';
import {toDateTimeString} from '../datetime/DateTimeUtil';
import SearchPastes from '../components/SearchPastes/SearchPastes';
import Spinner from '../components/Spinner/Spinner';

const Search: () => JSX.Element = () => {

  const location = useLocation();

  const [searchTerm, setSearchTerm] = useSearchParams();

  const effectiveTerm = () => {
    return (searchTerm.q && searchTerm.q.length >= 3) ? searchTerm.q : null;
  }

  const [pastes] = createResource(
    effectiveTerm,
    (term) => ApiClient.searchAll(term),
    {initialValue: []}
  );

  function onSearchEnter(term: String) {
    setSearchTerm({q: term})
  }

  return (
    <>
      <Switch>
        <Match when={pastes.loading}>

          <Spinner />

        </Match>
        <Match when={pastes.latest}>

          <SearchPastes term={effectiveTerm()} pastes={pastes()} onSearchEnter={onSearchEnter} />

        </Match>
      </Switch>
    </>
  )
}

export default Search;
