/* @refresh reload */
import {Router} from '@solidjs/router';
import {JSX} from 'solid-js';
import {render} from 'solid-js/web';
import App from './App';
import './index.css';

render((): JSX.Element => (
  <Router>
    <App />
  </Router>
), document.getElementById('root'));
