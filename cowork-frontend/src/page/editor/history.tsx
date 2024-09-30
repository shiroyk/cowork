import { useState } from "react";
import Dialog from "../../components/dialog";
import api from "../../api";
import { applyUpdateV2, Doc, Transaction } from "yjs";
import { DocInfo } from "./doc.ts";
import { User } from "./user.ts";
import { randomColor } from "../../utils";

interface Vector {
  uid: string;
  data: number[];
}

interface Delta {
  insert?: string | object | undefined;
  delete?: number | undefined;
  retain?: number | undefined;
}

type UserDelta = {
  uid?: string;
  deltas: Delta[]
};

// show doc history
export default function History({ value }: { value: DocInfo | null }) {
  const [loading, setLoading] = useState(false);
  const [deltas, setDeltas] = useState<UserDelta[]>([]);
  const [users, setUsers] = useState<Record<string, User & {color: string}>>({});

  const load = async () => {
    if (!value) return;
    setLoading(true);
    try {
      const doc = new Doc();
      const text = doc.getText("main");
      const deltas: UserDelta[] = [];
      text.observe((e) => {
        const uid = e.transaction.origin?.meta?.get("uid") as string | undefined;
        deltas.push({ uid, deltas: (e.delta as Delta[]) });
      })
      const data = (await api.get<Vector[]>(`/doc/${value.did}/vectors`)).data ?? [];
      for (const v of data) {
        const txn = new Transaction(doc, null, true);
        txn.meta.set("uid", v.uid);
        applyUpdateV2(doc, new Uint8Array(v.data), txn);
      }

      // client_id to uid
      const clients = Object.entries(value.clients).reduce((acc, [key, value]) => {
        acc[value] = key;
        return acc;
      }, {} as Record<string, string>)

      // get users
      const uids = [...doc.store.clients.keys()].map(id => clients[id]);
      const users = (await api.get<User[]>(`/user`, { ids: uids })).data ?? [];
      for (let user of users) {
        const color = randomColor();
        setUsers(p => ({ ...p, [user.id]: { ...user, color } }));
      }

      setDeltas(deltas);
    } finally {
      setLoading(false);
    }
  }

  const clear = () => setDeltas([]);

  return <Dialog label={"History"} title={"History"} onOpen={load} onClose={clear}>
    {loading && <div>Loading...</div>}
    {deltas.map((v, i) => <div key={i} className="history-line">
      {v.uid && <code style={{ color: users[v.uid].color }}>{users[v.uid].username}: </code>}
      {v.deltas.map((d, j) =>
        <span key={j}>
          {d.retain && <code>&gt; {d.retain}</code>}
          {d.delete && <code style={{ color: "red" }}>- {d.delete}</code>}
          {d.insert && <code style={{ color: "green" }}>+ {d.insert.toString()}</code>}
        </span>
      )}
    </div>)}
  </Dialog>;
}