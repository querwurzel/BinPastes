import {A} from "@solidjs/router";
import {JSX} from "solid-js";
import './header.css';

const Header: () => JSX.Element = () => {
  return (
    <h1>
      <A href={'/'}>BinPastes</A>
      <img src="/favicon.png" alt="" />
    </h1>
  )
}

export default Header
