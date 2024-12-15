import {Component, createEffect, createSignal, For, JSX, on, onMount, onCleanup, Show, Switch, Match} from 'solid-js';
import linkifyElement from 'linkify-element';
import {PasteView} from '../../api/model/PasteView';
import {decrypt} from '../../crypto/CryptoUtil';
import {relativeDiffLabel, toDateString, toDateTimeString} from '../../datetime/DateTimeUtil';
import {Lock, Unlock, Key, Trash, Link, Copy, Clone} from '../../assets/Vectors';
import styles from './readPaste.module.css';

type ReadPasteProps = {
  initialPaste: PasteView
  onBurnPaste: () => Promise<PasteView>
  onClonePaste: () => void
  onDeletePaste: () => void
}

const ReadPaste: Component<ReadPasteProps> = ({initialPaste, onBurnPaste, onClonePaste, onDeletePaste}): JSX.Element => {

  const [paste, setPaste] = createSignal<PasteView>(initialPaste);
  const [isEncrypted, setEncrypted] = createSignal<boolean>(initialPaste.isEncrypted);

  let keyInput: HTMLInputElement;
  let contentElement: HTMLPreElement;

  createEffect(on(paste, () => linkifyContent()));

  onMount(() => {
    window.addEventListener("keydown", onCopyContent);
  })

  onCleanup(() => {
    window.removeEventListener("keydown", onCopyContent);
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
    const plainText = decrypt(content, key);
    if (plainText) {
      setEncrypted(false);
      setPaste((prev) => ({
        ...prev,
        content: plainText
      }));
    } else {
      keyInput.style.backgroundColor = 'red';
    }
  }

  function onDecryptClick() {
    decryptContent(paste().content, keyInput.value);
  }

  function onDecryptSubmit(e: KeyboardEvent) {
    if (e.key === "Enter") {
      decryptContent(paste().content, keyInput.value);
    }
  }

  function onBurn(e: Event) {
    onBurnPaste()
      .then((payload) => setPaste(payload))
      .catch(() => {});
  }

  function onCopyLink(e: Event) {
    e.preventDefault();
    navigator.clipboard
      .writeText(window.location.href)
      .catch(() => {});
  }

  function onClone(e: Event) {
    e.preventDefault();
    onClonePaste();
  }

  function onCopyToClipboard(e: Event) {
    e.preventDefault();
    navigator.clipboard
      .writeText(paste().content)
      .catch(() => {});
  }

  function onDelete(e: Event) {
    e.preventDefault();

    const msg = paste().title ? `Delete paste "${paste().title}"?` : 'Delete paste?';
    if (window.confirm(msg)) {
      onDeletePaste();
    }
  }

  function onCopyContent(e: KeyboardEvent) {
    const shortCutActive = !(e.altKey) &&
      (e.code === 'KeyC' && e.shiftKey) &&
      ((e.ctrlKey || e.metaKey) && e.ctrlKey !== e.metaKey /* XOR */);

    if (shortCutActive) {
      onCopyToClipboard(e);
    }
  }

  function lines(): Array<string> | null {
    const lines = paste().content.split(/\n/g);
    return lines.length > 1
      ? lines
      : null;
  }

  return (
    <div class={styles.read}>

      <h2>
        <Show when={paste().isEncrypted}>
        <Switch>
        <Match when={isEncrypted()}>
          <Lock/>
        </Match>
        <Match when={!isEncrypted()}>
          <Unlock/>
        </Match>
        </Switch>
        </Show>
        {paste().title || 'Untitled'}
      </h2>

      <div class={styles.meta}>
        Created: <time title={toDateTimeString(paste().dateCreated)}>{toDateString(paste().dateCreated)}</time> |
        Expires: <Show when={paste().dateOfExpiry} fallback={<span>Never</span>}><time>{toDateTimeString(paste().dateOfExpiry)}</time></Show>
        <Show when={paste().sizeInBytes}> | Size: {paste().sizeInBytes}&nbsp;bytes</Show>
        <Show when={paste().views}>
        <br />
        Views: {paste().views} | Last viewed: <time title={toDateTimeString(paste().lastViewed)}>{relativeDiffLabel(paste().lastViewed)}</time>
        </Show>
        <br />
        <a onClick={onCopyLink} href="#" title="Copy link"><Link /></a>
        <Show when={paste().content && !isEncrypted()}><a onClick={onCopyToClipboard} href="#" title="Copy content" class={styles.clipboard}><Copy /></a></Show>
        <Show when={paste().isPublic && !paste().isEncrypted}><a onClick={onClone} href="#" title="Clone paste"><Clone /></a></Show>
        <Show when={paste().isErasable}><a onClick={onDelete} href="#" title="Delete item"><Trash /></a></Show>
      </div>

      <Show when={paste().isOneTime}>
        <div class={styles.onetime}>
        <Switch>
          <Match when={paste().content}>
            <strong>For your eyes only! All information is lost after leaving this page!</strong>
          </Match>
          <Match when={!paste().content}>
            <div><strong>This paste will burn after reading.</strong></div>
            <button onClick={onBurn}>Reveal content</button>
          </Match>
        </Switch>
        </div>
      </Show>

      <Show when={paste().content} fallback={<div ref={contentElement}></div>}>
        <Show when={paste().isEncrypted && isEncrypted()}>
          <div class={styles.encryption}>
            <strong>Encrypted!</strong> Enter password to decipher:
            &#32;
            <input ref={keyInput} type="password" autocomplete="one-time-code" onKeyUp={onDecryptSubmit}/>
            &#32;
            <button onClick={onDecryptClick}><Key /></button>
          </div>
        </Show>

        <pre ref={contentElement}>
        <For each={lines()} fallback={<span class={styles.line}>{paste().content}</span>}>{line =>
          <span class={styles.row}><span class={styles.count}></span><span class={styles.line}>{line}<br /></span></span>
        }
        </For>
        </pre>
      </Show>
    </div>
  )
}

export default ReadPaste
