/* @refresh reload */
import {Router, Route} from '@solidjs/router';
import {JSX, lazy} from 'solid-js';
import {render} from 'solid-js/web';
import App from './App';
import Create from './pages/create';
import Search from './pages/search';
import './index.css';

const Read = lazy(() => import('./pages/read'));
const NotFound = lazy(() => import('./pages/404'));

render((): JSX.Element => (
  <Router root={App}>
    <Route path="/" component={Create}  />
    <Route path="/paste/:id" component={Read} />
    <Route path="/paste/search" component={Search} />
    <Route path="*" component={NotFound} />
  </Router>
), document.getElementById('root'));
