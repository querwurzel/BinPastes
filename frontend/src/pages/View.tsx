import {useParams} from '@solidjs/router';
import {JSX} from 'solid-js';
import '../App.module.css';
import ReadPaste from '../components/ReadPaste/ReadPaste';

const View: () => JSX.Element = () => {

  const params = useParams<{id: string}>();

  return (
    <ReadPaste pasteId={params.id} />
  )
}

export default View;
