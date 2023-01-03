import {useNavigate} from '@solidjs/router';
import {JSX} from 'solid-js';
import {createPaste} from '../api/client';
import {PasteCreateCmd} from '../api/model/PasteCreateCmd';
import CreatePaste from '../components/CreatePaste/CreatePaste';

const Create: () => JSX.Element = () => {

  const navigate = useNavigate();

  const onCreatePaste = (cmd: PasteCreateCmd): Promise<string> => {
    return createPaste(cmd)
      .then(paste => {
        const url = window.location.origin + '/paste/' + paste.id;

        navigator.clipboard
          .writeText(url)
          .catch(_ => {});

        if (!paste.isOneTime) {
          navigate('/paste/' + paste.id)
        }

        return url;
      });
  }

  return (
    <CreatePaste onCreatePaste={onCreatePaste}/>
  )
}

export default Create;
