import {Component, createResource, createSignal, JSX, Match, Show, Switch} from 'solid-js';
import {findOne} from '../../api/client';
import {decrypt} from '../../crypto/Crypto';
import styles from './readPaste.module.css';
import './readPaste.module.css';
import openLock from './open-padlock.png';
import lock from './padlock.png';

const ReadPaste: Component<{pasteId: string}> = (props): JSX.Element => {

  const [paste] = createResource(() => props.pasteId,(pasteId) => findOne(pasteId));

  const [clearText, setClearText] = createSignal<string>(null);

  const decryptContent = (e: KeyboardEvent) => {
    if (e.key !== "Enter") {
      return;
    }

    const key = (e.target as HTMLInputElement).value;
    const clearText = decrypt(paste().content, key);

    setClearText(clearText);
    e.preventDefault();
  }

  return (
    <div class={styles.read}>

      <Switch fallback={<div>Loading ..</div>}>
        <Match when={paste.loading} keyed>
          <h3>Loading ..</h3>
        </Match>
        <Match when={paste.state === 'ready'} keyed>
          <h3><Show when={paste().isEncrypted} keyed><img width="15px" src={clearText() ? openLock : lock} alt="lock" /></Show> {paste()?.title || 'Untitled'}</h3>

          <h4>Created: {paste().dateCreated} | Expires: {paste().dateOfExpiry || 'Never'} | Size: {paste().sizeInBytes} bytes | Views: {paste().views + 1 || '1'} | Last seen: {paste().lastViewed || new Date().toISOString()}</h4>

          <Show when={paste()?.isOneTime}>
            <h4>For your eyes only! This paste can only be viewed once. </h4>
          </Show>

          <Show when={paste()?.isEncrypted && !clearText()}>
            <h4>ENCRYPTED! Enter password to decode:</h4>
            <input type="password" onKeyUp={decryptContent} />
          </Show>

          <Show when={clearText() != null} fallback={<pre>{paste().content}</pre>}>
          <pre>
              {clearText()}
          </pre>
          </Show>

        </Match>
      </Switch>

    </div>
  )
}

export default ReadPaste
