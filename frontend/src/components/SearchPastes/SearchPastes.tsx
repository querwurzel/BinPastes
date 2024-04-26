import {createResource, createSignal, JSX} from 'solid-js';
import ApiClient from '../../api/client';
import {PasteListView} from '../../api/model/PasteListView';

const SearchPastes: () => JSX.Element = () => {

  const [search, setSearch] = createSignal<string>();

  const [results, { refetch }] = createResource(() => search(), () => searchTerm());

  const searchTerm = (): Promise<Array<PasteListView>> => {
    if (search() && search().length >= 3) {
      return ApiClient.searchAll(search());
    }

    return Promise.resolve([]);
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

export default SearchPastes
