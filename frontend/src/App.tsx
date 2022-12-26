import {createResource, createSignal, For, JSX, Match, Switch} from 'solid-js';
import {createStore} from 'solid-js/store';
import {createPaste, findAll, searchAll} from './api/client';
import AES from 'crypto-js/aes';
import './App.module.css';
import styles from './App.module.css';

interface FormModel extends CreatePasteCmd {
  password: string
}

const App: () => JSX.Element = () => {

  const [lastPaste, setLastPaste] = createSignal<string>(null);
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
  const [form, setForm] = createStore<FormModel>(null);

  let creationForm: HTMLFormElement

  const updateFormField = (fieldName: keyof FormModel) => (event: Event) => {
    const inputElement = event.currentTarget as HTMLInputElement;

    setForm({
      [fieldName]: inputElement.value
    });
  };

  const submitCreateForm = (e: Event) => {
    e.preventDefault();

    const data: CreatePasteCmd = {
      title: form.title,
      content: form.content,
      expiry: form.expiry,
      exposure: form.exposure
    }

    if (form.password) {
      data.content = AES.encrypt(form.content, form.password).toString();
      data.isEncrypted = true;
    }

    createPaste(data)
      .then(resp => {
        setLastPaste(resp.id);
        window.location.hash = resp.id;
        navigator.clipboard.writeText(resp.id);
      })
      .then(_ => displaySuccess())
      .then(_ => resetCreateForm())
      .then(_ => refetch())
  }

  const displaySuccess = () => {
    if (creationForm) {
      for (const fieldset of creationForm.children) {
        fieldset.classList.add(styles.notification)

        window.setTimeout(() => {
          fieldset.classList.remove(styles.notification)
        }, 2000)
      }
    }
  }

  const resetCreateForm = () => {
    creationForm?.reset();
  }

  return (
    <>
      <h1>BinPastes</h1>

      <form ref={creationForm} onSubmit={submitCreateForm}>

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
            <textarea id="content"
                      name="content"
                      minLength="5"
                      maxLength="4096"
                      required="required"
                      autofocus="autofocus"
                      rows="20"
                      cols="75"
                      placeholder={'Paste here'}
                      onChange={updateFormField('content')}></textarea>
          </div>
          <hr/>
          <div>
            <label for="key">Password (optional) </label>
            <input id="key"
                   name="key"
                   type="password"
                   placeholder={'Password'}
                   autocomplete="one-time-code"
                   onChange={updateFormField('password')}/>
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
            &#32;
            <span>{lastPaste()}</span>
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
                    <th>Encrypted?</th>
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
                    <td>{item.isEncrypted ? 'true' : 'false'}</td>
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
