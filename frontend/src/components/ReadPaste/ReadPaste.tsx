import {A} from '@solidjs/router';
import {Component, createSignal, JSX, Show} from 'solid-js';
import {deletePaste} from '../../api/client';
import {PasteView} from '../../api/model/PasteView';
import {decrypt} from '../../crypto/Crypto';
import openLock from './open-padlock.png';
import lock from './padlock.png';
import styles from './readPaste.module.css';

// TODO clone feature -> take paste, navigate to createForm, prefill form

const ReadPaste: Component<{paste: PasteView}> = ({paste}): JSX.Element => {

  const [clearText, setClearText] = createSignal<string>(null);

  let keyInput: HTMLInputElement;

  const urlify = (text: string): String => {
    const urls = text.match(/((((ftp|http|https?):\/\/)|(w{3}\.))[\-\w@:%_\+.~#?,&\/\/=]+)/g);

    if (urls) {
      urls.forEach(url => text = text.replace(url, `<a target="_blank" href="${url}">${url}</a>`));
    }

    return text.replace("(", "<br/>(")
  };

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

  const deleteIt = (e: Event) => {
    const msg = paste.title ? `Delete paste "${paste.title}"?` : 'Delete paste?';

    if (window.confirm(msg)) {
      deletePaste(paste.id);
    } else {
      e.preventDefault();
    }
  }

  return (
    <div class={styles.read}>

      <h3><Show when={paste.isEncrypted} keyed><img width="15px" src={clearText() ? openLock : lock} alt="lock" /></Show> {paste.title || 'Untitled'}</h3>

      <h4>
        Created: {paste.dateCreated} |
        Expires: {paste.dateOfExpiry || 'Never'} |
        Size: {paste.sizeInBytes} bytes |
        Views: {paste.views} |
        Last seen: {paste.lastViewed || '-'}
        <Show when={!paste.isPublic} keyed> | <A onClick={deleteIt} href={'/'} title="Delete">🗑</A></Show>
      </h4>

      <Show when={paste.isOneTime}>
        <h4 class={styles.onetime}><strong>For your eyes only! This paste will burn after reading.</strong></h4>
      </Show>

      <Show when={paste.isEncrypted && !clearText()}>
        <p class={styles.decrypt}>
          <strong>ENCRYPTED!</strong> Enter password to decode:
          &#32;
          <input ref={keyInput} type="password" onKeyUp={decryptContent}/>
          &#32;
          <button onClick={decryptContent}>🗝</button>
        </p>
      </Show>

      <Show when={clearText()} fallback={<pre>{paste.content}</pre>}>
        <pre>{clearText()}</pre>
      </Show>

    </div>
  )
}

export default ReadPaste
