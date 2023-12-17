import {JSX} from 'solid-js';
import styles from './App.module.css';
import Footer from './components/Footer/Footer';
import Header from './components/Header/Header';
import RecentPastes from './components/RecentPastes/RecentPastes';

const App: () => JSX.Element = (props) => {
  return (
    <>
      <header class={styles.head}>
        <Header />
      </header>

      <div class={styles.content}>

        <main class={styles.left}>
          <div class={styles.leftContainer}>
          {props.children}
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
