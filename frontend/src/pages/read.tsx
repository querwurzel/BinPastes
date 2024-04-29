import {useNavigate, useParams} from '@solidjs/router';
import {Component, createResource, JSX, lazy, Match, Switch} from 'solid-js';
import ApiClient from '../api/client';
import type {PasteView} from '../api/model/PasteView';
import AppContext from '../AppContext';
import ReadPaste from '../components/ReadPaste/ReadPaste';

const NotFound = lazy(() => import('./404'));

const Read: Component = (): JSX.Element => {

  const navigate = useNavigate();

  const params = useParams<{id: string}>();

  const [paste] = createResource(
    () => params.id,
    (id) => AppContext.popPasteCreated() || ApiClient.findPaste(id)
  );

  function burnPaste(): Promise<PasteView> {
      return ApiClient.findOneTimePaste(paste().id);
  }

  function clonePaste() {
    AppContext.pushPasteCloned({
      title: paste().title,
      content: paste().content
    })
    navigate('/')
  }

  function deletePaste() {
    ApiClient.deletePaste(paste().id)
      .then(() => {
        AppContext.pushPasteDeleted(paste());
        navigate('/');
      })
      .catch(() => {})
  }

  return (
    <Switch>
      <Match when={paste.error}>
        <NotFound/>
      </Match>
      <Match when={paste.state === 'ready'}>
        <ReadPaste initialPaste={paste()} onBurnPaste={burnPaste} onClonePaste={clonePaste} onDeletePaste={deletePaste}/>
      </Match>
    </Switch>
  )
}

export default Read;
