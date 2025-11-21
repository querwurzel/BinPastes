import {JSX, Component, ParentProps} from 'solid-js';
import { useLocation } from "@solidjs/router";
import Footer from './components/Footer/Footer';
import Header from './components/Header/Header';
import RecentPastes from './components/RecentPastes/RecentPastes';
import styles from './App.module.css';

const App: Component = (props: ParentProps): JSX.Element => {

  const location = useLocation();

  return (
    <>
      <header class={styles.header}>
        <Header />
      </header>

      <div class={styles.content}>

        <main class={styles.left}>
          <div class={styles.leftContainer}>
            {props.children}
          </div>
        </main>

        <aside classList={{ [styles.right] : true, [styles.rightMobile]: location.pathname.startsWith("/paste/") }}>
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
