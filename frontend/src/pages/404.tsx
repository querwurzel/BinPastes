import {A} from '@solidjs/router';
import {JSX} from 'solid-js';
import '../App.module.css';

const NotFound: () => JSX.Element = () => {

  return (
    <div>
      <h3>Your advertising could be here.</h3>
      <p>Because nothing is here yet.</p>

      <p><A href={'/'}>Start over?</A></p>
    </div>
  )
}

export default NotFound;
