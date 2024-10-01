import { applyUpdateV2, Doc, RelativePosition, Transaction } from "yjs";
import { Awareness } from "y-protocols/awareness";
import { baseURL } from "../../api";
import { showToast } from "../../components/toast";
import { decode, decodeAsync, encode } from "@msgpack/msgpack";
import { User } from "./user.ts";
import { randomColor } from "../../utils";

export enum ConnectStatus {
  Disconnected = 1,
  Connected = 2,
  Loaded = 3,
}

export enum DocEvent {
  LoginEvent = 1,
  LogoutEvent = 2,
  SyncEvent = 3,
  UpdateEvent = 4,
  SaveEvent = 5,
  CursorEvent = 6,
}

export interface Message {
  event: DocEvent;
  uid?: string;
  did?: string;
  data: Uint8Array | null;
}

export type OnlineUser = User & { color: string; client_id: number };

interface Selection {
  clientID: number;
  anchor: RelativePosition;
  head: RelativePosition;
}

type MessageHandler = (msg: Message) => void;

export class DocAwareness extends Awareness {
  private ws: WebSocket;
  private status: ConnectStatus = ConnectStatus.Disconnected;
  private pending: number = -1;

  constructor(doc: Doc) {
    super(doc);
    clearInterval(this._checkInterval);
    this._checkInterval = 0;

    try {
      this.ws = new WebSocket(`${baseURL}/collab/${doc.guid}`, localStorage.getItem("access_token")!);
    } catch (e) {
      console.log(e);
      showToast(`failed to connect server: ${e}`, { type: "error" });
      throw e;
    }
    this.ws.onopen = () => {
      this.status = ConnectStatus.Connected;
      this.emit("status", [this.status]);
    };
    this.ws.onmessage = (e: MessageEvent<Blob>) => {
      (async () => {
        const msg = (await decodeAsync(e.data.stream())) as Message;
        console.log(msg);
        handlers[msg.event](msg);
      })();
    };
    this.ws.onclose = (e: CloseEvent) => {
      this.status = ConnectStatus.Disconnected;
      this.emit("status", [this.status, `Connection closed with ${e.reason || e.code}`]);
    };

    this.doc.on("updateV2", (update: Uint8Array, txn: Transaction) => {
      if (!txn) return;
      this.ws.send(encode({ data: update, event: DocEvent.UpdateEvent }));
      this.pending += 1;
      this.emit("save", [this.pending >= 0]);
    });

    const handlers: Record<DocEvent, MessageHandler> = {
      [DocEvent.LoginEvent]: this.onLoginInOut.bind(this),
      [DocEvent.LogoutEvent]: this.onLoginInOut.bind(this),
      [DocEvent.SyncEvent]: this.onSync.bind(this),
      [DocEvent.UpdateEvent]: this.onUpdate.bind(this),
      [DocEvent.SaveEvent]: this.onSave.bind(this),
      [DocEvent.CursorEvent]: this.onSelection.bind(this),
    };
  }

  destroy() {
    this.ws.close();
    super.destroy()
  }

  setLocalStateField(field: string, value: any) {
    if (this.status !== ConnectStatus.Loaded) return;
    this.states.set(this.doc.clientID, { [field]: value });
    let sel: Selection = { ...value, clientID: this.doc.clientID };
    this.ws.send(encode({ data: encode(sel), event: DocEvent.CursorEvent }));
  }

  onSync(msg: Message) {
    if (!msg.data) return;
    const content = decode(msg.data) as { client_id: number, data: Uint8Array };
    content.data && applyUpdateV2(this.doc, content.data);
    // set client id from server
    this.doc.clientID = content.client_id;
    this.status = ConnectStatus.Loaded;
    this.emit("status", [this.status]);
  }

  onUpdate(msg: Message) {
    if (!msg.data) return;
    applyUpdateV2(this.doc, msg.data);
  }

  onSave(msg: Message) {
    if (!msg.data) return;
    const saved = (decode(msg.data) as number);
    this.pending -= saved;
    this.emit("save", [this.pending >= 0]);
  }

  onLoginInOut(msg: Message) {
    const users = msg.data ? (decode(msg.data) as OnlineUser[]) : null;
    this.emit("user", [users?.map(u => ({
      ...u,
      color: randomColor()
    }))])
    if (msg.event === DocEvent.LogoutEvent) {
      const clients = users?.map(u => u.client_id) ?? []
      for (const key of this.states.keys()) {
        if (!clients.includes(key)) {
          this.states.delete(key);
        }
      }
      this.emit("change", []);
    }
  }

  onSelection(msg: Message) {
    if (!msg.data) return;
    const data = decode(msg.data) as Selection;
    this.states.set(data.clientID, { selection: data });
    this.emit("change", []);
  }

}