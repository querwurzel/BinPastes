import {Component, createSignal, JSX, Show} from "solid-js";
import {createStore} from "solid-js/store";
import {PasteCreateCmd} from "../../api/model/PasteCreateCmd";
import {encrypt} from "../../crypto/CryptoUtil";
import {Copy} from '../../assets/Vectors';
import styles from "./createPaste.module.css";

export type PasteClone = {
  title?: string
  content: string
}

type CreatePasteProps = {
  onCreatePaste: (paste: PasteCreateCmd) => Promise<string>
  initialPaste?: PasteClone
}

type FormModel = {
  title?: string
  content: string
  expiry?: 'ONE_HOUR' | 'ONE_DAY' | 'ONE_WEEK' | 'ONE_MONTH' | 'THREE_MONTHS' | 'ONE_YEAR' | 'NEVER'
  exposure?: 'PUBLIC' | 'UNLISTED' | 'ONCE'
  password: string
}

const CreatePaste: Component<CreatePasteProps> = ({onCreatePaste, initialPaste}): JSX.Element => {

  const [form, setForm] = createStore<FormModel>({
    title: initialPaste?.title || null,
    content: initialPaste?.content || null,
    password: null,
    expiry: null,
    exposure: null
  });

  const [lastPasteUrl, setLastPasteUrl] = createSignal<string>();

  let creationForm: HTMLFormElement

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
    setLastPasteUrl(null);
    setForm({
      title: null,
      password: null,
      content: null,
      expiry: null,
      exposure: null
    } as FormModel)
  }

  const createPaste = (e: Event) => {
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

    onCreatePaste(data)
      .then(url => {
        resetCreateForm();
        resetStore();
        setLastPasteUrl(url);
      })
  }

  return (
    <form ref={creationForm} onSubmit={createPaste} onReset={resetStore} autocomplete="off" class={styles.createForm}>
      <fieldset>
        <div>
          <label for="expiry">Expires in: </label>
          <select id="expiry" name="expiry" onChange={updateFormField("expiry")}>
            <option value="ONE_HOUR">1 Hour</option>
            <option value="ONE_DAY" selected>1 Day</option>
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
            <input type="radio" id="public" name="exposure" value="PUBLIC" checked
                   onInput={updateFormField("exposure")}/>
            Public
          </label>
          <label for="unlisted">
            <input type="radio" id="unlisted" name="exposure" value="UNLISTED" onInput={updateFormField("exposure")}/>
            Unlisted
          </label>
          <label for="once">
            <input type="radio" id="once" name="exposure" value="ONCE" onInput={updateFormField("exposure")}/>
            Once (One-Time)
          </label>
        </div>
        <hr/>
        <div>
          <label for="title">Title (optional): </label>
          <input type="text"
                 id="title"
                 name="title"
                 placeholder="Title"
                 maxLength={255}
                 value={form.title}
                 onInput={updateFormField("title")}/>
        </div>
        <hr/>
        <div>
          <label for="key">Password (optional): </label>
          <input id="key"
                 name="key"
                 type="password"
                 placeholder="Password"
                 autocomplete="one-time-code"
                 onInput={updateFormField("password")}/>
        </div>
        <hr/>
        <div>
          <textarea minLength="5"
                    maxLength="4096"
                    required={true}
                    autofocus={true}
                    rows="20"
                    cols="75"
                    placeholder="Paste here"
                    onInput={updateFormField("content")}>{form.content}</textarea>
        </div>
      </fieldset>

      <fieldset>
        <Show when={lastPasteUrl()}>
          <p class={styles.lastPaste}>{lastPasteUrl()}<Copy/></p>
        </Show>
        <input type="submit" value="Paste"/>
        <input type="reset" value="Reset"/>
      </fieldset>

    </form>
  )
};

export default CreatePaste;
