import {useNavigate, useParams} from '@solidjs/router';
import {Component, createResource, JSX, lazy, Match, Switch} from 'solid-js';
import ApiClient from '../api/client';
import AppContext from '../AppContext';
import ReadPaste from '../components/ReadPaste/ReadPaste';

const NotFound = lazy(() => import('./404'));

const Read: Component = (): JSX.Element => {

  const appContext = AppContext;

  const navigate = useNavigate();

  const params = useParams<{id: string}>();

  const [paste] = createResource(() => params.id, (id) => appContext.popPasteCreated() || ApiClient.findOne(id));

  const clonePaste = () => {
    appContext.pushPasteCloned({
      title: paste().title,
      content: paste().content
    })
    navigate('/')
  }

  const deletePaste = () => {
    ApiClient.deletePaste(paste().id)
      .then(_ => {
        appContext.pushPasteDeleted(paste());
        navigate('/')
      })
      .catch(() => {})
  }

  return (
    <>
      <Switch>
        <Match when={paste.error}>

          <NotFound />

        </Match>
        <Match when={paste.state === 'ready'}>

          <ReadPaste paste={paste()} onClonePaste={clonePaste} onDeletePaste={deletePaste}/>

        </Match>
      </Switch>
    </>
  )
}

export default Read;
