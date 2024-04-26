import {A} from '@solidjs/router';
import {createResource, For, JSX, Match, onCleanup, onMount, Show, Switch} from 'solid-js';
import ApiClient from '../../api/client';
import {PasteListView} from '../../api/model/PasteListView';
import AppContext from '../../AppContext';
import {relativeDiffLabel} from '../../datetime/DateTimeUtil';
import styles from './recentPastes.module.css';

const RecentPastes: () => JSX.Element = () => {

  const [pastes, { mutate, refetch }] = createResource(ApiClient.findAll);

  const appContext = AppContext;

  let refreshTimer = window.setInterval(refetch, 60_000)

  const restartJob = () => {
    window.clearInterval(refreshTimer);
    refreshTimer = window.setInterval(refetch, 60_000);
  }

  onMount(() => {
    appContext.onPasteCreated((paste) => {
      if (!paste.isPublic) {
        return;
      }

      const newItem: PasteListView = {
        id: paste.id,
        dateCreated: paste.dateCreated,
        title: paste.title,
        dateOfExpiry: paste.dateOfExpiry,
        isEncrypted: paste.isEncrypted,
        sizeInBytes: paste.sizeInBytes
      };

      restartJob();
      mutate(prev => [newItem].concat(prev))
    })
  })

  onCleanup(() => window.clearInterval(refreshTimer));

  return (
    <div class={styles.recentPastes}>

      <Switch>
        <Match when={pastes.error}>
          <h3>Loading ..</h3>
        </Match>
        <Match when={pastes.latest}>
          <h3>
            <strong>Last {pastes()?.length} pastes</strong>
            &nbsp;
            <span class={styles.refresh} onClick={refetch}>↻</span>
          </h3>

          <ol>
            <For each={pastes()}>{item =>
            <li>
              <p class={styles.item}><A href={'/paste/' + item.id}>{item.title || 'Untitled' }</A> <Show when={item.isEncrypted} keyed><img src="/assets/images/padlock.png" alt="lock" /></Show></p>
              <p>Created: {relativeDiffLabel(item.dateCreated)} | Size: {item.sizeInBytes} bytes</p>
            </li>
            }
            </For>
          </ol>
        </Match>
      </Switch>
    </div>
  )
}

export default RecentPastes
