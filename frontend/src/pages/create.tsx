import {useNavigate} from '@solidjs/router';
import {JSX} from 'solid-js';
import {useSearchParams} from '@solidjs/router';
import ApiClient from '../api/client';
import {PasteCreateCmd} from '../api/model/PasteCreateCmd';
import {AppContext, PasteClone} from '../AppContext';
import CreatePaste from '../components/CreatePaste/CreatePaste';

const Create: () => JSX.Element = () => {

  const navigate = useNavigate();

  const [params] = useSearchParams();

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

  function effectiveInitialPaste(): PasteClone | undefined {
    const popped = AppContext.popPasteCloned();

    if (popped) {
      return popped;
    }

    if (params.c) {
      return {
        content: params.c.toString()
      };
    }

    if (params.u) {
      return {
        content: params.u.toString()
      };
    }

    return undefined;
  }

  return (
    <CreatePaste initialPaste={effectiveInitialPaste()} onCreatePaste={createPaste} />
  )
}

export default Create;
