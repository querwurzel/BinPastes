import {JSX} from 'solid-js';
import './spinner.css';

const Spinner: () => JSX.Element = () => {
  return (
    <div class="lds-ellipsis"><div></div><div></div><div></div><div></div></div>
  )
}

export default Spinner;
