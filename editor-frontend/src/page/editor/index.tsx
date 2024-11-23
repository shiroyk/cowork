import { useEffect, useRef, useState } from "react";
import { Doc } from "yjs";
import { MonacoBinding } from "y-monaco";
import MonacoEditor, { monaco } from "react-monaco-editor";
import useUser from "./user.ts";
import useDoc, { DocInfo } from "./doc.ts";
import History from "./history.tsx";
import { Loading } from "../../components/loading";
import "./index.css";
import { ConnectStatus, DocAwareness, OnlineUser } from "./awareness.ts";

export default function Editor() {
  const editorRef = useRef<monaco.editor.IStandaloneCodeEditor>();
  const dialogRef = useRef<HTMLDialogElement>(null);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [content, setContent] = useState("");
  const [onlineUsers, setOnlineUsers] = useState<OnlineUser[] | null>(null);
  const [saving, setSaving] = useState(false);
  const [docInfo, setDocInfo] = useState<DocInfo | null>(null);

  const docAwarenessRef = useRef<DocAwareness>();
  const docBindingRef = useRef<MonacoBinding>();
  const [status, setStatus] = useState<ConnectStatus | null>(null);
  const [msg, setMsg] = useState("");

  const { getInfo, signUp, signIn } = useUser();
  const { getOrAddDoc } = useDoc();

  const doc = useRef<Doc>();

  const handleOpenLogin = () => dialogRef.current?.showModal();

  const handleClose = () => dialogRef.current?.close();

  const handleSignUp = async () => await signUp(username, password);

  const handleSignIn = async () => (await signIn(username, password)) && location.reload();

  const onChange = (v: string) => setContent(v);

  const initEditor = async () => {
    const info = await getOrAddDoc();
    setDocInfo(info);
    const ydoc = new Doc();
    ydoc.guid = info.did;
    doc.current = ydoc;
    const awareness = new DocAwareness(ydoc);
    docAwarenessRef.current = awareness;
    awareness.on("status", (e: ConnectStatus, msg: string) => {
      setStatus(e);
      setMsg(msg);
    });
    awareness.on("user", setOnlineUsers);
    awareness.on("save", setSaving);
    if (editorRef.current) {
      docBindingRef.current = new MonacoBinding(ydoc.getText("main"), editorRef.current.getModel()!,
        new Set([editorRef.current]), awareness);
      editorRef.current.focus();
    }
  }

  useEffect(() => {
    (async () => {
      const info = await getInfo();
      if (!info) return;
      await initEditor();
    })()
    return () => {
      docBindingRef.current?.destroy();
      docAwarenessRef.current?.destroy();
    }
  }, []);

  const showSaving = status === ConnectStatus.Loaded && saving;

  const cursorStyle = onlineUsers?.map(u => `
  .yRemoteSelection-${u.client_id} { background-color: ${u.color}; }
  .yRemoteSelectionHead-${u.client_id} { background-color: ${u.color}; }
  `).join("\n") ?? ""

  return (
    <>
      <div className="btn">
        {status ? (
          status === ConnectStatus.Disconnected ?
            <div style={{ color: "red" }}>{msg}</div> :
            <div style={{ display: "flex", gap: 5 }}>
              {onlineUsers?.map((u, i) =>
                <span key={u.id}>
                  <span style={{ color: u.color }}>{u.username}</span>
                  {i < onlineUsers.length - 1 && <span> | </span>}
                </span>
              )}
            </div>
        ) : (
          <button type="button" onClick={handleOpenLogin}>
            Login
          </button>
        )}
        <div style={{ flex: 1 }}></div>
        {status && status >= ConnectStatus.Connected && <History value={docInfo}/>}
        {showSaving && <div className="saving"><Loading/>{"Saving..."}</div>}
      </div>
      <div className="editor-box">
        <style dangerouslySetInnerHTML={{ __html: cursorStyle }}/>
        <MonacoEditor
          width={"100%"}
          height={window.innerHeight - 45}
          language="markdown"
          theme="vs-dark"
          value={content}
          options={{
            automaticLayout: true,
            readOnly: status !== ConnectStatus.Loaded,
            readOnlyMessage: { value: "Please login first" }
          }}
          onChange={onChange}
          editorDidMount={(e) => editorRef.current = e}
        />
      </div>
      <dialog className="login-dialog" ref={dialogRef}>
        <form>
          <div>Login</div>
          <div>
            <input onChange={(e) => setUsername(e.target.value)}/>
          </div>
          <div>
            <input
              type="password"
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          <div style={{ display: "flex", gap: 5, marginTop: 5 }}>
            <button type="button" onClick={handleSignIn}>
              SignIn
            </button>
            <button type="button" onClick={handleSignUp}>
              SignUp
            </button>
            <div style={{ flex: 1 }}></div>
            <button type="button" onClick={handleClose}>
              Close
            </button>
          </div>
        </form>
      </dialog>
    </>
  );
}