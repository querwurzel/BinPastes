import {Route, Routes} from '@solidjs/router';
import {JSX, lazy} from 'solid-js';
import styles from './App.module.css';
import Footer from './components/Footer/Footer';
import Header from './components/Header/Header';
import RecentPastes from './components/RecentPastes/RecentPastes';
import Create from './pages/Create';
import Search from './pages/Search';

const Read = lazy(() => import('./pages/Read'));
const NotFound = lazy(() => import('./pages/404'));

const App: () => JSX.Element = () => {
  return (
    <>
      <header class={styles.head}>
        <Header />
      </header>

      <div class={styles.content}>

        <main class={styles.left}>
          <div class={styles.leftContainer}>
          <Routes>
            <Route path="/" component={Create}  />
            <Route path="/paste/:id" component={Read} />
            <Route path="/paste/search" component={Search} />
            <Route path="*" component={NotFound} />
          </Routes>
          </div>
        </main>

        <aside class={styles.right}>
          <RecentPastes />
        </aside>

      </div>

      <footer class={styles.footer}>
        <Footer />
      </footer>
    </>
  )
}

export default App;
