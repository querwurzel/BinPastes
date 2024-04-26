import linkifyElement from 'linkify-element';
import {Component, createEffect, createSignal, JSX, on, Show} from 'solid-js';
import {PasteView} from '../../api/model/PasteView';
import {decrypt} from '../../crypto/CryptoUtil';
import {relativeDiffLabel, toDateString, toDateTimeString} from '../../datetime/DateTimeUtil';
import styles from './readPaste.module.css';

interface ReadPasteProps {
  paste: PasteView
  onClonePaste: () => void
  onDeletePaste: () => void
}

const ReadPaste: Component<ReadPasteProps> = ({paste, onClonePaste, onDeletePaste}): JSX.Element => {

  const [clearText, setClearText] = createSignal<string>(null);

  let keyInput: HTMLInputElement;
  let content: HTMLPreElement;

  const linkifyContent = () => {
    linkifyElement(content, {
      target: {
        url: '_blank',
        email: null,
      }
    });
  }
  const decryptContent = (content: string, key: string) => {
    setClearText(decrypt(content, key));
  }
  const onDecryptClick = () => {
    decryptContent(paste.content, keyInput.value);
  }
  const onDecryptSubmit = (e: KeyboardEvent) => {
    if (e instanceof KeyboardEvent && e.key === "Enter") {
      decryptContent(paste.content, keyInput.value);
    }
  }
  const onCloneClick = (e: Event) => {
    e.preventDefault();
    onClonePaste();
  }
  const onDeleteClick = (e: Event) => {
    e.preventDefault();

    const msg = paste.title ? `Delete paste "${paste.title}"?` : 'Delete paste?';
    if (window.confirm(msg)) {
      onDeletePaste();
    }
  }

  createEffect(on(clearText, () => linkifyContent()));

  return (
    <div class={styles.read}>

      <h2><Show when={paste.isEncrypted} keyed><img width="15px" src={clearText() ? '/assets/images/padlock_open.png' : '/assets/images/padlock.png'} alt="lock" /></Show> {paste.title || 'Untitled'}</h2>

      <p>
        Created: <time title={toDateTimeString(paste.dateCreated)}>{toDateString(paste.dateCreated)}</time> |
        Expires: <time>{paste.dateOfExpiry ? toDateTimeString(paste.dateOfExpiry) : 'Never'}</time> |
        Size: {paste.sizeInBytes} bytes
        <br />
        Views: {paste.views}
        <Show when={paste.views} keyed> | Last viewed: <time title={toDateTimeString(paste.lastViewed)}>{relativeDiffLabel(paste.lastViewed)}</time></Show>
        <Show when={paste.isPublic && !paste.isEncrypted} keyed> | <a onClick={onCloneClick} href="#" title="Clone" class={styles.clone}>⎘</a></Show>
        <Show when={paste.isErasable} keyed> | <a onClick={onDeleteClick} href="#" title="Delete">🗑</a></Show>
      </p>

      <Show when={paste.isEncrypted && !clearText()}>
        <p class={styles.decrypt}>
          <strong>ENCRYPTED!</strong> Enter password to decode:
          &#32;
          <input ref={keyInput} type="password" onKeyUp={onDecryptSubmit}/>
          &#32;
          <button onClick={onDecryptClick}>🗝</button>
        </p>
      </Show>

      <Show when={paste.isOneTime}>
        <h3 class={styles.onetime}><strong>For your eyes only! This paste will burn after reading.</strong></h3>
      </Show>

      <Show when={clearText()} fallback={<pre ref={content}>{paste.content}</pre>}>
        <pre ref={content}>{clearText()}</pre>
      </Show>

    </div>
  )
}

export default ReadPaste
