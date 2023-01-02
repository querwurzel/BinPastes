import {A} from '@solidjs/router';
import {createResource, For, JSX, Match, Show, Switch} from 'solid-js';
import {findAll} from '../../api/client';
import {relativeDiffLabel, toDateString} from '../../datetime/DateTimeUtil';
import lock from '../ReadPaste/padlock.png';
import styles from './recentPastes.module.css';

const RecentPastes: () => JSX.Element = () => {

  const [pastes, { refetch }] = createResource<any[]>(findAll);

  window.setInterval(refetch, 120_000);

  return (
    <div class={styles.recentPastes}>

      <Switch fallback={<div>Loading ..</div>}>
        <Match when={pastes.loading}>
          <h3>Loading ..</h3>
        </Match>
        <Match when={pastes.state === 'ready'}>
          <h3>
            <strong>{pastes()?.length} pastes</strong>
            &nbsp;
            <span onclick={refetch} style="cursor:pointer">↻</span>
          </h3>
          <ol>
            <For each={pastes()}>{item =>
            <li>
              <p class={styles.item}><A href={'/paste/' + item.id}>{item.title || 'Untitled' }</A> <Show when={item.isEncrypted} keyed><img width="15px" src={lock} alt="lock" /></Show></p>
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