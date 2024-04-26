import {Component, createEffect, createSignal, For, JSX, on, onMount, onCleanup, Show} from 'solid-js';
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
  let contentElement: HTMLPreElement;

  createEffect(on(clearText, () => linkifyContent()));

  onMount(() => {
    window.addEventListener("keydown", globalSelectContent);
  })

  onCleanup(() => {
    window.removeEventListener("keydown", globalSelectContent);
  })

  function linkifyContent() {
    linkifyElement(contentElement, {
      target: {
        url: '_blank',
        email: null,
      }
    });
  }

  function decryptContent(content: string, key: string) {
    setClearText(decrypt(content, key));
  }

  function onDecryptClick() {
    decryptContent(paste.content, keyInput.value);
  }

  function onDecryptSubmit(e: KeyboardEvent) {
    if (e instanceof KeyboardEvent && e.key === "Enter") {
      decryptContent(paste.content, keyInput.value);
    }
  }

  function onCloneClick(e: Event) {
    e.preventDefault();
    onClonePaste();
  }

  function onDeleteClick(e: Event) {
    e.preventDefault();

    const msg = paste.title ? `Delete paste "${paste.title}"?` : 'Delete paste?';
    if (window.confirm(msg)) {
      onDeletePaste();
    }
  }

  function globalSelectContent(e: KeyboardEvent) {
    if (e.altKey || e.shiftKey) {
      return;
    }

    if (e.code === 'KeyA' && ((e.ctrlKey || e.metaKey) && e.ctrlKey !== e.metaKey)) { // XOR
      selectContent();
      e.preventDefault();
    }
  }

  function selectContent() {
      if (window.getSelection && document.createRange) {
        let range = document.createRange();
        range.selectNodeContents(contentElement);

        let selection = window.getSelection();
        selection.removeAllRanges();
        selection.addRange(range);
        return;
      }

      // @ts-ignore
      if (document.body.createTextRange) {
          // @ts-ignore
          let range = document.body.createTextRange();
          range.moveToElementText(contentElement);
          range.select();
      }
  }

  function content() {
    const text = clearText() || paste.content;
    return text.split(/\r?\n|\r|\n/g);
  }

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
        <Show when={paste.views} keyed>
        <br />
        Views: {paste.views} | Last viewed: <time title={toDateTimeString(paste.lastViewed)}>{relativeDiffLabel(paste.lastViewed)}</time>
        </Show>
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

      <pre ref={contentElement}>
      <Show when={content().length > 1} keyed fallback={<span class={styles.line}>{content()}</span>}>
      <For each={content()}>{line =>
        <span class={styles.row}><span class={styles.count}></span><span class={styles.line}>{line}</span></span>
      }
      </For>
      </Show>
      </pre>

    </div>
  )
}

export default ReadPaste
