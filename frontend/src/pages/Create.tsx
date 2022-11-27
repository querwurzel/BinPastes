import {useNavigate} from '@solidjs/router';
import {JSX} from 'solid-js';
import ApiClient from '../api/client';
import {PasteCreateCmd} from '../api/model/PasteCreateCmd';
import AppContext from '../AppContext';
import CreatePaste from '../components/CreatePaste/CreatePaste';

const Create: () => JSX.Element = () => {

  const appContext = AppContext;

  const navigate = useNavigate();

  const onCreatePaste = (cmd: PasteCreateCmd): Promise<string> => {
    return ApiClient.createPaste(cmd)
      .then(paste => {
        const path = '/paste/' + paste.id;
        const url = window.location.origin + path;

        navigator.clipboard
          .writeText(url)
          .catch(_ => {});

        if (!paste.isOneTime) {
          appContext.pushCreatedPaste(paste);
          navigate(path);
        }

        return url;
      });
  }

  return (
    <CreatePaste initialValues={appContext.popClonedPaste()} onCreatePaste={onCreatePaste} />
  )
}

export default Create;
