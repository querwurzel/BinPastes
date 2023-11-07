import {JSX} from 'solid-js';

const Footer: () => JSX.Element = () => {
  return (
    <div>
      <span>© {new Date().getFullYear()}</span>
    </div>
  )
}

export default Footer
