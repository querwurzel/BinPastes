import {useParams} from '@solidjs/router';
import {createResource, JSX, Match, Switch} from 'solid-js';
import {findOne} from '../api/client';
import '../App.module.css';
import ReadPaste from '../components/ReadPaste/ReadPaste';
import NotFound from './404';

const View: () => JSX.Element = () => {

  const params = useParams<{id: string}>();

  const [paste] = createResource(() => params.id,(pasteId) => findOne(pasteId));

  console.log(paste.state)

  return (
    <>
      <Switch>
        <Match when={paste.loading}>

          <span>Loading ..</span>

        </Match>
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
