import { useState } from "react";
import Dialog from "../../components/dialog";
import api from "../../api";
import { applyUpdateV2, Doc } from "yjs";

interface Vector {
  uid: string;
  data: number[];
}

interface Delta {
  insert?: string | object | undefined;
  delete?: number | undefined;
  retain?: number | undefined;
}

type Deltas = Delta[];

export default function History({ did }: { did: string }) {
  const [loading, setLoading] = useState(false);
  const [deltas, setDeltas] = useState<Deltas[]>([]);

  const load = async () => {
    setLoading(true);
    try {
      const doc = new Doc();
      const text = doc.getText("text");
      const deltas: Deltas[] = [];
      text.observe((e) => {
        deltas.push((e.delta as Deltas));
      })
      const data = (await api.get<Vector[]>(`/doc/${did}/vectors`)).data ?? [];
      for (const v of data) {
        applyUpdateV2(doc, new Uint8Array(v.data));
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
      {v.map((d, j) =>
        <span key={j}>
          {d.retain && <code>&gt; {d.retain}</code>}
          {d.delete && <code style={{ color: "red" }}>- {d.delete}</code>}
          {d.insert && <code style={{ color: "green" }}>+ {d.insert.toString()}</code>}
        </span>
      )}
    </div>)}
  </Dialog>;
}