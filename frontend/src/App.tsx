import {createResource, createSignal, For, JSX, Match, Switch} from 'solid-js';
import {createStore} from 'solid-js/store';
import {createPaste, findAll, searchAll} from './api/client';
import './App.module.css';
import styles from './App.module.css';

const App: () => JSX.Element = () => {

  const [search, setSearch] = createSignal<string>(null);

  const resetSearchForm = () => {
    setSearch(null);
    refetch();
  }

  const submitSearchForm = (e: Event) => {
    e.preventDefault();
    refetch();
  }

  const listOrSearch = (): Promise<any[]> => {
    if (search() && search().length >= 3) {
      return searchAll(search());
    }

    return findAll();
  }

  const [data, { refetch }] = createResource<any[]>(listOrSearch);
  const [form, setForm] = createStore<CreatePasteCmd>(null);

  let creationForm: HTMLFormElement

  const updateFormField = (fieldName: keyof CreatePasteCmd) => (event: Event) => {
    const inputElement = event.currentTarget as HTMLInputElement;

    setForm({
      [fieldName]: inputElement.value
    });
  };

  const submitCreateForm = (e: Event) => {
    e.preventDefault();

    createPaste(form)
      .then(_ => resetCreateForm())
      .then(_ => refetch())
  }

  const resetCreateForm = () => {
    creationForm?.reset();
  }

  return (
    <>
      <h1>BinPastes</h1>

      <form id="createForm" ref={creationForm} onSubmit={submitCreateForm}>

        <fieldset>
          <div>
            <label for="title">Title (optional) </label>
            <input type="text" id="title" name="title" placeholder={'Title'} onChange={updateFormField('title')}/>
          </div>
          <hr/>
          <div>
            <label for="expiry">Expires in </label>
            <select id="expiry" name="expiry" onChange={updateFormField('expiry')}>
              <option value="ONE_HOUR">One Hour</option>
              <option value="ONE_DAY" selected="selected">One Day</option>
              <option value="ONE_WEEK">One Week</option>
              <option value="ONE_MONTH">One Month</option>
              <option value="ONE_YEAR">One Year</option>
              <option value="NEVER">Never</option>
            </select>
          </div>
          <hr/>
          <div>
            <span>Visible</span>
            <input type="radio" id="public" name="exposure" value="PUBLIC" checked="checked" onChange={updateFormField('exposure')} />
            <label for="public">Public</label>
            <input type="radio" id="unlisted" name="exposure" value="UNLISTED" onChange={updateFormField('exposure')}/>
            <label for="unlisted">Unlisted</label>
            <input type="radio" id="once" name="exposure" value="ONCE" onChange={updateFormField('exposure')}/>
            <label for="once">Once (One-Time)</label>
          </div>
          <hr/>
          <div>
            <label for="content">Content</label>
            <textarea id="content" name="content" minLength="5" maxLength="4096" required="required"
                      autofocus="autofocus" rows="20" cols="75" placeholder={'Paste here'} onChange={updateFormField('content')}></textarea>
          </div>
        </fieldset>

        <fieldset>
          <input type="submit" value="Paste"/>
        </fieldset>

      </form>

      <br/>

      <form onReset={resetSearchForm} onSubmit={submitSearchForm}>

        <fieldset>
          <div>
            <label for="text">Search </label>
            <input
              type="text"
              id="text"
              name="text"
              required="required"
              minlength="3"
              placeholder={'Term'}
              onInput={(event) => setSearch(event.currentTarget.value)}
            />
            &#32;
            <input type="submit" value="Find" />
            &#32;
            <input type="reset" value="All" />
          </div>
        </fieldset>

        <fieldset>
          <Switch fallback={<div>Loading ..</div>}>
            <Match when={data.loading}>
              <p>Loading ..</p>
            </Match>
            <Match when={data.state === 'ready'}>
              <p><strong>{data().length} pastes</strong></p>
              <table class={styles.pasteTable}>
                <thead>
                  <tr>
                    <th>Id</th>
                    <th>Title</th>
                    <th>Size (bytes)</th>
                    <th>Date Created <span>▼</span></th>
                    <th>Date Of Expiry</th>
                  </tr>
                </thead>
                <tbody>
                <For each={data()}>{item =>
                  <tr>
                    <td>{item.id}</td>
                    <td>{item.title}</td>
                    <td>{item.sizeInBytes}</td>
                    <td>{item.dateCreated}</td>
                    <td>{item.dateOfExpiry}</td>
                  </tr>
                }
                </For>
                </tbody>
              </table>
            </Match>
          </Switch>
        </fieldset>

      </form>

    </>
  )
}

export default App;
