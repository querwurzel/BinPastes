import {A} from '@solidjs/router';
import {JSX} from 'solid-js';

const Header: () => JSX.Element = () => {
  return (
    <div>
      <h1><A href={'/'}>BinPastes</A></h1>
    </div>
  )
}

export default Header
