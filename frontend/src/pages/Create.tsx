import {useNavigate} from '@solidjs/router';
import {JSX} from 'solid-js';
import '../App.module.css';
import {PasteView} from '../api/model/PasteView';
import CreatePaste from '../components/CreatePaste/CreatePaste';

const Create: () => JSX.Element = () => {

  const navigate = useNavigate();

  const onPasteCreated = (paste: PasteView) => {
    if (!paste.isOneTime) {
      navigate('/paste/' + paste.id)
    }
  }

  return (
    <CreatePaste onCreated={onPasteCreated}/>
  )
}

export default Create;
