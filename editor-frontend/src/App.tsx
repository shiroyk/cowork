import "./App.css";
import Toast, { toastRef } from "./components/toast";
import Editor from "./page/editor";

function App() {
  return <div>
    <Toast ref={toastRef} />
    <Editor />
  </div>
}

export default App;
