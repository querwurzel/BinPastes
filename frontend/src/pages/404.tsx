import {A} from '@solidjs/router';
import {JSX} from 'solid-js';

const NotFound: () => JSX.Element = () => {
  return (
    <div style="margin:1rem">
      <h3><i>404</i> Nothing around here :(</h3>
      <p>But your advertisement could be.</p>
      <p><A href={'/'}>Start over?</A></p>
    </div>
  )
}

export default NotFound;
