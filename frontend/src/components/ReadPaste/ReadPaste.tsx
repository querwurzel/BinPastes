import {Component, createSignal, JSX, Show} from 'solid-js';
import {PasteView} from '../../api/model/PasteView';
import {decrypt} from '../../crypto/Crypto';
import openLock from './open-padlock.png';
import lock from './padlock.png';
import styles from './readPaste.module.css';
import './readPaste.module.css';

const ReadPaste: Component<{paste: PasteView}> = ({paste}): JSX.Element => {

  const [clearText, setClearText] = createSignal<string>(null);

  let keyInput: HTMLInputElement;

  const decryptContent = (e: KeyboardEvent | MouseEvent) => {
    if (e instanceof KeyboardEvent && e.key !== "Enter") {
      return;
    }

    const key = keyInput.value;
    const cipherText = paste.content;
    const clearText = decrypt(cipherText, key);

    setClearText(clearText);
    e.preventDefault();
  }

  return (
    <div class={styles.read}>

      <h3><Show when={paste.isEncrypted} keyed><img width="15px" src={clearText() ? openLock : lock} alt="lock" /></Show> {paste.title || 'Untitled'}</h3>

      <h4>Created: {paste.dateCreated} | Expires: {paste.dateOfExpiry || 'Never'} | Size: {paste.sizeInBytes} bytes | Views: {paste.views} | Last seen: {paste.lastViewed || '-'}</h4>

      <Show when={paste.isOneTime}>
        <h4 class={styles.onetime}><strong>For your eyes only! This paste will burn after reading.</strong></h4>
      </Show>

      <Show when={paste.isEncrypted && !clearText()}>
        <p>
          <strong>ENCRYPTED!</strong> Enter password to decode:
          <input ref={keyInput} type="password" onKeyUp={decryptContent}/>
          <button onClick={decryptContent} class={styles.key}>🗝</button>
        </p>
      </Show>

      <Show when={clearText()} fallback={<pre>{paste.content}</pre>}>
        <pre>
            {clearText()}
        </pre>
      </Show>

    </div>
  )
}

export default ReadPaste
