import {useNavigate} from '@solidjs/router';
import {JSX} from 'solid-js';
import ApiClient from '../api/client';
import {PasteCreateCmd} from '../api/model/PasteCreateCmd';
import AppContext from '../AppContext';
import CreatePaste from '../components/CreatePaste/CreatePaste';

const Create: () => JSX.Element = () => {

  const navigate = useNavigate();

  function createPaste(cmd: PasteCreateCmd): Promise<void> {
    return ApiClient.createPaste(cmd)
      .then(paste => {
        const path = '/paste/' + paste.id;
        const url = window.location.origin + path;

        navigator.clipboard
          .writeText(url)
          .catch(() => {});

        if (paste.isPublic) {
          AppContext.pushPasteCreated(paste);
        }

        navigate(path);
      });
  }

  return (
    <CreatePaste initialPaste={AppContext.popPasteCloned()} onCreatePaste={createPaste} />
  )
}

export default Create;
