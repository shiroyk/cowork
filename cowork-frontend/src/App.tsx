import { useEffect, useRef, useState } from "react";
import "./App.css";
import { Doc, applyUpdateV2 } from "yjs";
import { MonacoBinding } from "y-monaco";
import MonacoEditor, { EditorDidMount, monaco } from "react-monaco-editor";
import { decode, decodeAsync, encode } from "@msgpack/msgpack";

let baseURL = `http://${location.host}/api`;

if (import.meta.env.PROD) {
  baseURL = `${location.protocol}//api.cowork.local`;
}

enum Event {
  LoginEvent = 1,
  LogoutEvent = 2,
  SyncEvent = 3,
  UpdateEvent = 4,
  SaveEvent = 5,
}

interface Message {
  event: Event;
  uid: string;
  did: string;
  data: Uint8Array;
}

type MessageHandler = (msg: Message) => void;

type Users = {
  username: string;
}[];

const useUser = () => {
  const [token, setToken] = useState(
    localStorage.getItem("access_token") ?? ""
  );

  const signIn = async (username: string, password: string) => {
    const res = await fetch(`${baseURL}/auth/sign_in`, {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        username: username,
        password: password,
      }),
    });
    if (!res.ok) {
      alert("Login failed: " + (await res.text()));
      return false;
    }
    const body: { access_token: string } = await res.json();
    localStorage.setItem("access_token", body.access_token);
    setToken(body.access_token);
    return true;
  };

  const signUp = async (username: string, password: string) => {
    const res = await fetch(`${baseURL}/auth/sign_up`, {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        username: username,
        password: password,
      }),
    });
    if (!res.ok) {
      alert("Sign up failed: " + (await res.text()));
    } else {
      alert("Sign up success: " + (await res.text()));
    }
  };

  const checkToken = () => {
    if (!token) return true;
    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      if (new Date().getTime() > payload.exp * 1000) {
        localStorage.removeItem("access_token");
        alert("Token expired");
      }
    } catch (e) {
      console.log(e);
      localStorage.removeItem("access_token");
    }
  };

  const getUserId = () => JSON.parse(atob(token.split(".")[1])).iss as string;

  return { token, getUserId, signUp, signIn, checkToken };
};

interface DocInfo {
  title: string;
  uid: string;
  did: string;
}

const searchDoc = async (token: string): Promise<DocInfo[] | null> => {
  const res = await fetch(`${baseURL}/doc?title=test`, {
    headers: {
      Accept: "application/json",
      Authorization: "Bearer " + token,
    },
  });
  if (!res.ok) {
    alert(await res.text());
    return null;
  }
  return await res.json();
}

const getDocId = async (token: string, uid: string): Promise<string> => {
  const docs = await searchDoc(token);
  if (docs?.length) {
    return docs[0].did;
  }
  let newDoc = await fetch(`${baseURL}/doc`, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      Authorization: "Bearer " + token,
    },
    body: JSON.stringify({
      title: "test1",
      uid: uid
    }),
  });
  if (!newDoc.ok) {
    alert(await newDoc.text());
  }
  return ((await newDoc.json()) as { id: string }).id;
};

enum ConnectStatus {
  Disconnected = 1,
  Connecting = 2,
  Connected = 3,
}

function App() {
  const editorRef = useRef<monaco.editor.IStandaloneCodeEditor>();
  const dialogRef = useRef<HTMLDialogElement>(null);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [content, setContent] = useState("");
  const [connect, setConnect] = useState<ConnectStatus | null>(null);
  const [connectMsg, setConnectMsg] = useState("");
  const [lastSave, setLastSave] = useState<string | null>(null);
  const { token, getUserId, signUp, signIn, checkToken } = useUser();

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

  const onLogin: MessageHandler = (msg) => {
    const list = msg.data ? (decode(msg.data) as Users) : null;
    setConnectMsg(list ? list.map((u) => u.username).join(" | ") : "");
  };

  const onLogout: MessageHandler = (msg) => {
    const list = msg.data ? (decode(msg.data) as Users) : null;
    setConnectMsg(list ? list.map((u) => u.username).join(" | ") : "");
  };

  const onSync: MessageHandler = (msg) => {
    ((decode(msg.data) as Uint8Array[]) || null)?.forEach((u) =>
      applyUpdateV2(ydoc, u)
    );
  };

  const onUpdate: MessageHandler = (msg) => {
    applyUpdateV2(ydoc, msg.data);
  };

  const onSave: MessageHandler = (msg) => {
    const date = new Date((decode(msg.data) as number) * 1000);
    setLastSave(
      "Saved at ✔️ " +
        date.toLocaleString("en-US", {
          timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
        })
    );
  };

  const handlers: Record<Event, MessageHandler> = {
    [Event.LoginEvent]: onLogin,
    [Event.LogoutEvent]: onLogout,
    [Event.SyncEvent]: onSync,
    [Event.UpdateEvent]: onUpdate,
    [Event.SaveEvent]: onSave,
  };

  useEffect(() => {
    checkToken();
    if (token == "") return;
    const uid = getUserId();
    let ws: WebSocket | null = null;
    (async () => {
      const did = await getDocId(token, uid);
      setConnect(ConnectStatus.Connecting);
      try {
        ws = new WebSocket(`${baseURL}/collab/${did}`, token);
      } catch (e) {
        console.log(e);
        alert(e);
        return;
      }
      ws.onopen = () => {
        setConnect(ConnectStatus.Connected);
        ydoc.on("updateV2", (update, or) => {
          if (!or || ws!.readyState != ws!.OPEN) return;
          ws!.send(
              encode({ did: did, uid: `${uid}`, data: update, event: Event.UpdateEvent })
          );
        });
      };
      ws.onmessage = (e: MessageEvent<Blob>) => {
        (async () => {
          const msg = (await decodeAsync(e.data.stream())) as Message;
          console.log(msg);
          handlers[msg.event]?.(msg);
        })();
      };
      ws.onclose = (e: CloseEvent) => {
        setConnect(ConnectStatus.Disconnected);
        setConnectMsg(`Connection closed with ${e.reason || e.code}`);
      };
    })();
    return () => ws?.close();
  }, []);

  return (
    <>
      <div className="btn">
        {connect ? (
          <div style={{
            color: connect === ConnectStatus.Disconnected ? "red" : "green",
          }}>
            {connectMsg}
          </div>
        ) : (
          <button type="button" onClick={handleOpenLogin}>
            Login
          </button>
        )}

        <div style={{ flex: 1 }}></div>
        <div style={{ marginRight: 10, color: "green" }}>{lastSave}</div>
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
            <input onChange={(e) => setUsername(e.target.value)} />
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

export default App;
