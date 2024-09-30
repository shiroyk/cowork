import { useEffect, useRef, useState } from "react";
import { Doc, applyUpdateV2 } from "yjs";
import { MonacoBinding } from "y-monaco";
import MonacoEditor, { EditorDidMount, monaco } from "react-monaco-editor";
import useEvent, { Message, DocEvent, ConnectStatus } from "../../hooks/event.ts";
import useUser, { Users } from "./user.ts";
import useDoc from "./doc.ts";
import { decode } from "@msgpack/msgpack";
import History from "./history.tsx";
import { Loading } from "../../components/loading";
import "./index.css";

type MessageHandler = (msg: Message) => void;

export default function Editor() {
  const editorRef = useRef<monaco.editor.IStandaloneCodeEditor>();
  const dialogRef = useRef<HTMLDialogElement>(null);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [content, setContent] = useState("");
  const [onlineUsers, setOnlineUsers] = useState<Users | null>(null);
  const [saving, setSaving] = useState(-1);
  const [did, setDid] = useState<string | null>(null);

  const { getUserId, signUp, signIn, validToken } = useUser();
  const { getOrAddDoc } = useDoc();
  const { status, connect, send, close, disconnectMsg } = useEvent({
    onMessage: msg => handlers[msg.event]?.(msg)
  });

  const ydoc = new Doc();
  const ytext = ydoc.getText("text");

  const handleOpenLogin = () => dialogRef.current?.showModal();

  const handleClose = () => dialogRef.current?.close();

  const handleSignUp = async () => await signUp(username, password);

  const handleSignIn = async () => {
    (await signIn(username, password)) && location.reload();
  };

  const onChange = (v: string) => setContent(v);

  const editorDidMount: EditorDidMount = (editor) => {
    editorRef.current = editor;
    editor.focus();
    new MonacoBinding(ytext, editor.getModel()!, new Set([editor]));
  };

  const onLoginOut: MessageHandler = (msg) => setOnlineUsers(msg.data ? (decode(msg.data) as Users) : null)

  const onUpdate: MessageHandler = (msg) => applyUpdateV2(ydoc, msg.data);

  const onSave: MessageHandler = (msg) => {
    const saved = (decode(msg.data) as number);
    setSaving(p => p - saved);
  };

  const handlers: Record<DocEvent, MessageHandler> = {
    [DocEvent.LoginEvent]: onLoginOut,
    [DocEvent.LogoutEvent]: onLoginOut,
    [DocEvent.SyncEvent]: onUpdate,
    [DocEvent.UpdateEvent]: onUpdate,
    [DocEvent.SaveEvent]: onSave,
  };

  useEffect(() => {
    if (!validToken()) return;
    const uid = getUserId();
    (async () => {
      const did = await getOrAddDoc(uid);
      setDid(did);
      connect(did);
      ydoc.on("updateV2", (update, or) => {
        if (!or) return;
        send({ data: update, event: DocEvent.UpdateEvent });
        setSaving(p => p+1);
      });
    })();
    return () => close();
  }, []);

  const showSaving = status === ConnectStatus.Connected && saving >= 0;

  return (
    <>
      <div className="btn">
        {status ? (
          status === ConnectStatus.Disconnected ?
            <div style={{ color: "red" }}>
              {disconnectMsg}
            </div> :
            <div style={{ color: "#74ffb0" }}>
              {onlineUsers?.map(i => i.username).join(" | ")}
            </div>
        ) : (
          <button type="button" onClick={handleOpenLogin}>
            Login
          </button>
        )}
        <div style={{ flex: 1 }}></div>
        {did && <History did={did}/>}
        {showSaving && <div className="saving"><Loading/>{"Saving..."}</div>}
      </div>
      <div className="editor-box">
        <MonacoEditor
          width={"100%"}
          height={window.innerHeight - 45}
          language="javascript"
          theme="vs-dark"
          value={content}
          options={{ automaticLayout: true }}
          onChange={onChange}
          editorDidMount={editorDidMount}
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