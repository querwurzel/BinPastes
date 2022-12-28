import {createResource, createSignal, JSX} from 'solid-js';
import {searchAll} from '../../api/client';

const Search: () => JSX.Element = () => {

  const [search, setSearch] = createSignal<string>(null);

  const [data, { refetch }] = createResource<any[]>(searchTerm);

  const searchTerm = (): Promise<any[]> => {
    if (search() && search().length >= 3) {
      return searchAll(search());
    }

    return Promise.resolve(null);
  }

  const resetSearchForm = () => {
    setSearch(null);
    refetch();
  }

  const submitSearchForm = (e: Event) => {
    e.preventDefault();
    refetch();
  }

  return (
    <div>

      <h1>BinPastes</h1>

    </div>
  )
}

export default Search
