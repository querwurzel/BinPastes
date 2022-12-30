import {Route, Routes} from '@solidjs/router';
import {JSX, lazy} from 'solid-js';
import styles from './App.module.css';
import Footer from './components/Footer/Footer';
import Header from './components/Header/Header';
import RecentPastes from './components/RecentPastes/RecentPastes';
import Create from './pages/Create';
import Search from './pages/Search';
import View from './pages/View';
import './App.module.css';

const NotFound = lazy(() => import("./pages/404"));

const App: () => JSX.Element = () => {

  return (
    <>
        <div class={styles.head}>
          <Header />
        </div>

        <div class={styles.content}>

          <div class={styles.left}>
            <Routes>
              <Route path="/" component={Create} />
              <Route path="/paste/search" component={Search} />
              <Route path="/paste/:id" component={View} />
              <Route path="*" component={NotFound} />
            </Routes>
          </div>

          <div class={styles.right}>
            <RecentPastes />
          </div>

        </div>

        <div class={styles.footer}>
          <Footer />
        </div>
    </>
  )
}

export default App;
