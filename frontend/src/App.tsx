import {Route, Routes} from '@solidjs/router';
import {JSX, lazy} from 'solid-js';
import styles from './App.module.css';
import './App.module.css';
import Footer from './components/Footer/Footer';
import Header from './components/Header/Header';
import RecentPastes from './components/RecentPastes/RecentPastes';
import Create from './pages/Create';

const View = lazy(() => import("./pages/View"));

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
            <Route path="/paste/:id" component={View} />
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
