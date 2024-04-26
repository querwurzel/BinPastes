import {Component, createEffect, createSignal, JSX, lazy, on, Show} from 'solid-js';
import linkifyElement from 'linkify-element';
import {PasteView} from '../../api/model/PasteView';
import {decrypt} from '../../crypto/CryptoUtil';
import {relativeDiffLabel, toDateString, toDateTimeString} from '../../datetime/DateTimeUtil';
import {Lock, Unlock, Key, Trash, Copy} from '../../assets/Vectors';
import styles from './readPaste.module.css';

type ReadPasteProps = {
  paste: PasteView
  onClonePaste: () => void
  onDeletePaste: () => void
}

const ReadPaste: Component<ReadPasteProps> = ({paste, onClonePaste, onDeletePaste}): JSX.Element => {

  const [clearText, setClearText] = createSignal<string>();

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

      <h2>
        <Show when={paste.isEncrypted} keyed>
          <Show when={clearText()} keyed fallback={<Lock/>}>
            <Unlock/>
          </Show>
        </Show>
        {paste.title || 'Untitled'}
      </h2>

      <p>
        Created: <time title={toDateTimeString(paste.dateCreated)}>{toDateString(paste.dateCreated)}</time> |
        Expires: <time>{paste.dateOfExpiry ? toDateTimeString(paste.dateOfExpiry) : 'Never'}</time> |
        Size: {paste.sizeInBytes} bytes
        <br />
        Views: {paste.views}
        <Show when={paste.views} keyed> | Last viewed: <time title={toDateTimeString(paste.lastViewed)}>{relativeDiffLabel(paste.lastViewed)}</time></Show>
        <Show when={paste.isPublic && !paste.isEncrypted} keyed> | <a onClick={onCloneClick} href="#" title="Clone" class={styles.clone}><Copy /></a></Show>
        <Show when={paste.isErasable} keyed> | <a onClick={onDeleteClick} href="#" title="Delete"><Trash /></a></Show>
      </p>

      <Show when={paste.isEncrypted && !clearText()}>
        <p class={styles.decrypt}>
          <strong>ENCRYPTED!</strong> Enter password to decode:
          &#32;
          <input ref={keyInput} type="password" onKeyUp={onDecryptSubmit}/>
          &#32;
          <button onClick={onDecryptClick}><Key /></button>
        </p>
      </Show>

      <Show when={paste.isOneTime}>
        <h3 class={styles.onetime}><strong>For your eyes only! This paste has just been burnt after reading.</strong></h3>
      </Show>

      <Show when={clearText()} fallback={<pre ref={content}>{paste.content}</pre>}>
        <pre ref={content}>{clearText()}</pre>
      </Show>

    </div>
  )
}

export default ReadPaste
