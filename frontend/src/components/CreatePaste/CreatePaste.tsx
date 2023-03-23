import {Component, createSignal, JSX, onMount, Show} from 'solid-js';
import {createStore} from 'solid-js/store';
import {PasteCreateCmd} from '../../api/model/PasteCreateCmd';
import {encrypt} from '../../crypto/CryptoUtil';
import styles from './createPaste.module.css';

export interface CloneModel {
  title?: string
  content: string
}

interface CreatePasteProps {
  onCreatePaste: (paste: PasteCreateCmd) => Promise<string>
  initialValues?: CloneModel
}

interface FormModel extends Omit<PasteCreateCmd, 'isEncrypted'> {
  password: string
}

const CreatePaste: Component<CreatePasteProps> = ({onCreatePaste, initialValues}): JSX.Element => {

  const [form, setForm] = createStore<FormModel>({
    title: initialValues?.title || null,
    content: initialValues?.content || null,
    password: null,
    expiry: null,
    exposure: null
  });

  const [lastPaste, setLastPaste] = createSignal<string>(null);

  let creationForm: HTMLFormElement

  onMount(() => {
    creationForm.onkeydown = submitCreateFormHotKey
  })

  const submitCreateFormHotKey = (e: KeyboardEvent) => {
    if (e.altKey || e.shiftKey || e.metaKey) {
      return;
    }

    if (e.ctrlKey && e.code === 'Enter') {
      creationForm.requestSubmit();
    }
  }

  const updateFormField = (fieldName: keyof FormModel) => (event: Event) => {
    const inputElement = event.currentTarget as HTMLInputElement;

    setForm({
      [fieldName]: inputElement.value
    });
  };

  const resetCreateForm = () => {
    creationForm?.reset();
  }

  const resetStore = () => {
    setForm({
      title: null,
      password: null,
      content: null,
      expiry: null,
      exposure: null
    } as FormModel)
  }

  const submitCreateForm = (e: Event) => {
    const data: PasteCreateCmd = {
      title: form.title,
      content: form.content,
      expiry: form.expiry,
      exposure: form.exposure
    }

    if (form.password) {
      data.content = encrypt(data.content, form.password);
      data.isEncrypted = true;
    }

    onCreatePaste(data)
      .then(url => {
        setLastPaste(url);
        resetCreateForm();
        resetStore();
      })

    e.preventDefault();
  }

  return (
    <form ref={creationForm} onSubmit={submitCreateForm} onReset={resetStore} autocomplete="off" class={styles.createForm}>
      <fieldset>
        <div>
          <label for="expiry">Expires in: </label>
          <select id="expiry" name="expiry" onChange={updateFormField('expiry')}>
            <option value="ONE_HOUR">1 Hour</option>
            {/* @ts-ignore selected={true} malfunctioning, is not rendered */}
            <option value="ONE_DAY" /* selected={true} */ selected="selected">1 Day</option>
            <option value="ONE_WEEK">1 Week</option>
            <option value="ONE_MONTH">1 Month</option>
            <option value="THREE_MONTHS">3 Months</option>
            <option value="ONE_YEAR">1 Year</option>
            <option value="NEVER">Never</option>
          </select>
        </div>
        <hr/>
        <div>
          <label>Visibility: </label>
          <label for="public">
            {/* @ts-ignore checked={true} malfunctioning, is not rendered */}
            <input type="radio" id="public" name="exposure" value="PUBLIC" /* checked={true} */ checked="checked" onInput={updateFormField('exposure')}/>
            Public
          </label>
          <label for="unlisted">
            <input type="radio" id="unlisted" name="exposure" value="UNLISTED" onInput={updateFormField('exposure')}/>
            Unlisted
          </label>
          <label for="once">
            <input type="radio" id="once" name="exposure" value="ONCE" onInput={updateFormField('exposure')}/>
            Once (One-Time)
          </label>
        </div>
        <hr/>
        <div>
          <label htmlFor="title">Title (optional): </label>
          <input type="text"
                 id="title"
                 name="title"
                 placeholder={'Title'}
                 maxLength={255}
                 value={form.title}
                 onInput={updateFormField('title')}/>
        </div>
        <hr/>
        <div>
          <label htmlFor="key">Password (optional): </label>
          <input id="key"
                 name="key"
                 type="password"
                 placeholder={'Password'}
                 autoComplete="one-time-code"
                 onInput={updateFormField('password')}/>
        </div>
        <hr/>
        <div>
          <textarea minLength="5"
                    maxLength="4096"
                    required={true}
                    autofocus={true}
                    rows="20"
                    cols="75"
                    placeholder={'Paste here'}
                    onInput={updateFormField('content')}>{form.content}</textarea>
        </div>
      </fieldset>

      <fieldset>
        <Show when={lastPaste()}>
          <p class={styles.lastPaste}><span>{lastPaste()}</span> ⎘</p>
        </Show>
        <input type="submit" value="Paste Ctrl+⏎" />
        <input type="reset" value="Reset" />
      </fieldset>

    </form>
  )
};

export default CreatePaste;
