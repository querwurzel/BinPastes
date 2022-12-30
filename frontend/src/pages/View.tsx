import {useParams} from '@solidjs/router';
import {createResource, JSX, Match, Switch} from 'solid-js';
import {findOne} from '../api/client';
import '../App.module.css';
import ReadPaste from '../components/ReadPaste/ReadPaste';
import NotFound from './404';

const View: () => JSX.Element = () => {

  const params = useParams<{id: string}>();

  const [paste] = createResource(() => params.id,(pasteId) => findOne(pasteId));

  return (
    <>
      <Switch>
        <Match when={paste.error}>

          <NotFound />

        </Match>
        <Match when={paste.state === 'ready'}>

          <ReadPaste paste={paste()}/>

        </Match>
      </Switch>
    </>
  )
}

export default View;
