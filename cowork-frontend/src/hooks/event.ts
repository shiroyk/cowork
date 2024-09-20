import { useRef, useState } from "react";
import { baseURL } from "../api";
import { decodeAsync, encode } from "@msgpack/msgpack";
import { showToast } from "../components/toast";

export enum ConnectStatus {
  Disconnected = 1,
  Connected = 2,
}

export enum DocEvent {
  LoginEvent = 1,
  LogoutEvent = 2,
  SyncEvent = 3,
  UpdateEvent = 4,
  SaveEvent = 5,
}

export interface Message {
  event: DocEvent;
  uid: string;
  did: string;
  data: Uint8Array;
}

interface Props {
  onMessage: (msg: Message) => void
}

export default function useEvent(props: Props) {
  let ws = useRef<WebSocket | undefined>();

  const [status, setStatus] = useState<ConnectStatus | null>(null);
  const [disconnectMsg, setDisconnectMsg] = useState("");

  const connect = (did: string) => {
    try {
      ws.current = new WebSocket(`${ baseURL }/collab/${did}`, localStorage.getItem("access_token")!);
    } catch (e) {
      console.log(e);
      showToast(`failed to connect server: ${e}`, { type: "error" });
      return;
    }
    ws.current.onopen = () => {
      setStatus(ConnectStatus.Connected);
    };
    ws.current.onmessage = (e: MessageEvent<Blob>) => {
      (async () => {
        const msg = (await decodeAsync(e.data.stream())) as Message;
        console.log(msg);
        props.onMessage(msg);
      })();
    };
    ws.current.onclose = (e: CloseEvent) => {
      setStatus(ConnectStatus.Disconnected);
      setDisconnectMsg(`Connection closed with ${ e.reason || e.code }`);
    };
  }

  const send = (msg: Message) => ws.current?.send(encode(msg));

  const close = () => ws.current?.close();

  return {
    connect,
    send,
    close,
    status,
    disconnectMsg,
  }
}