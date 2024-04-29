import {A} from "@solidjs/router";
import {JSX} from "solid-js";

const Header: () => JSX.Element = () => {
  return (
    <h1><A href={'/'}>BinPastes</A></h1>
  )
}

export default Header
