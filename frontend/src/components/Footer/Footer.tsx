import {JSX} from "solid-js";

const Footer: () => JSX.Element = () => {
  return (
    <div style="color: #f09801">
      <span>© {new Date().getFullYear()} - Forgetting things so you don't have to.</span>
    </div>
  )
}

export default Footer
