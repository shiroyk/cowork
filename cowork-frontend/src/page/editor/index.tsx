import { useEffect, useRef, useState } from "react";
import { Doc, applyUpdateV2, Transaction } from "yjs";
import { MonacoBinding } from "y-monaco";
import MonacoEditor, { monaco } from "react-monaco-editor";
import useEvent, { Message, DocEvent, ConnectStatus } from "../../hooks/event.ts";
import useUser, { Users } from "./user.ts";
import useDoc, { DocInfo } from "./doc.ts";
import { decode } from "@msgpack/msgpack";
import History from "./history.tsx";
import { Loading } from "../../components/loading";
import "./index.css";
import { randomColor } from "../../utils";

type MessageHandler = (msg: Message) => void;

export default function Editor() {
  const editorRef = useRef<monaco.editor.IStandaloneCodeEditor>();
  const dialogRef = useRef<HTMLDialogElement>(null);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [content, setContent] = useState("");
  const [onlineUsers, setOnlineUsers] = useState<Users | null>(null);
  const [saving, setSaving] = useState(-1);
  const [docInfo, setDocInfo] = useState<DocInfo | null>(null);

  const { getInfo, signUp, signIn } = useUser();
  const { getOrAddDoc } = useDoc();
  const { status, connect, send, disconnectMsg } = useEvent({
    onMessage: msg => handlers[msg.event]?.(msg)
  });

  const doc = useRef<Doc>();

  const handleOpenLogin = () => dialogRef.current?.showModal();

  const handleClose = () => dialogRef.current?.close();

  const handleSignUp = async () => await signUp(username, password);

  const handleSignIn = async () => (await signIn(username, password)) && location.reload();

  const onChange = (v: string) => setContent(v);

  const onLoginOut: MessageHandler = (msg) => setOnlineUsers(msg.data ? (decode(msg.data) as Users) : null)

  const onUpdate: MessageHandler = (msg) => {
    if (!doc.current || !msg.data) return;
    const content = decode(msg.data) as { client_id: number, data: Uint8Array };
    content.data && applyUpdateV2(doc.current, content.data);
    // set client id from server
    doc.current.clientID = content.client_id;
  };

  const onSave: MessageHandler = (msg) => {
    if (!msg.data) return;
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

  const initEditor = async () => {
    const info = await getOrAddDoc();
    setDocInfo(info);
    connect(info.did);
    const ydoc = new Doc();
    ydoc.guid = info.did;
    doc.current = ydoc;
    if (editorRef.current) {
      editorRef.current.focus();
      new MonacoBinding(ydoc.getText("main"), editorRef.current.getModel()!, new Set([editorRef.current]));
    }
    ydoc.on("updateV2", (update: Uint8Array, txn: Transaction) => {
      if (!txn) return;
      send({ data: update, event: DocEvent.UpdateEvent });
      setSaving(p => p + 1);
    });
  }

  useEffect(() => {
    (async () => {
      const info = await getInfo();
      if (!info) return;
      await initEditor();
    })()
    return close;
  }, []);

  const showSaving = status === ConnectStatus.Connected && saving > -1;

  return (
    <>
      <div className="btn">
        {status ? (
          status === ConnectStatus.Disconnected ?
            <div style={{ color: "red" }}>
              {disconnectMsg}
            </div> :
            <div style={{ display: "flex", gap: 5 }}>
              {onlineUsers?.map((u, i) =>
                <span key={u.id}>
                  <span style={{ color: randomColor() }}>{u.username}</span>
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
        {status === ConnectStatus.Connected && <History value={docInfo}/>}
        {showSaving && <div className="saving"><Loading/>{"Saving..."}</div>}
      </div>
      <div className="editor-box">
        <MonacoEditor
          width={"100%"}
          height={window.innerHeight - 45}
          language="markdown"
          theme="vs-dark"
          value={content}
          options={{
            automaticLayout: true,
            readOnly: status !== ConnectStatus.Connected,
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