import {A} from '@solidjs/router';
import {Component, createSignal, JSX, Show} from 'solid-js';
import {createStore} from 'solid-js/store';
import {createPaste} from '../../api/client';
import {PasteCreateCmd} from '../../api/model/PasteCreateCmd';
import {PasteView} from '../../api/model/PasteView';
import {encrypt} from '../../crypto/Crypto';
import styles from './createPaste.module.css';

interface FormModel extends PasteCreateCmd {
  password: string
}

const CreatePaste: Component<{onCreated: (paste: PasteView) => void}> = ({onCreated}): JSX.Element => {

  let creationForm: HTMLFormElement

  const [form, setForm] = createStore<FormModel>(null);

  const [lastPaste, setLastPaste] = createSignal<string>(null);

  const updateFormField = (fieldName: keyof FormModel) => (event: Event) => {
    const inputElement = event.currentTarget as HTMLInputElement;

    setForm({
      [fieldName]: inputElement.value
    });
  };

  const resetCreateForm = () => {
    creationForm?.reset();
    setForm(null);
  }

  const submitCreateForm = (e: Event) => {
    e.preventDefault();

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

    createPaste(data)
      .then(resp => {
        resetCreateForm();

        const url = window.location.origin + '/paste/' + resp.id;

        setLastPaste(url);

        navigator.clipboard
          .writeText(url)
          .then(_ => onCreated(resp))
          .catch(_ => onCreated(resp))
      })
  }

  return (
    <div>

      <Show when={lastPaste()}>
        <p class={styles.lastPaste}><strong>One-Time Paste:</strong> <span>{lastPaste()}</span></p>
      </Show>

      <form ref={creationForm} onSubmit={submitCreateForm} autocomplete="off">

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
            <input type="radio" id="public" name="exposure" value="PUBLIC" checked="checked"
                   onChange={updateFormField('exposure')}/>
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
    </div>
  )
};

export default CreatePaste;
