import api from "../../api";
import { showToast } from "../../components/toast";

interface DocInfo {
  title: string;
  uid: string;
  did: string;
}

const useDoc = () => {
  const searchDoc = async (): Promise<DocInfo[] | undefined> => (await api.get<DocInfo[]>(`/doc`, { title: `test` })).data

  const getOrAddDoc = async (uid: string): Promise<string> => {
    const docs = await searchDoc();
    if (docs?.length) {
      return docs[0].did;
    }
    let newDoc = await api.post<{ id: string }>(`/doc`, {
      title: "test",
      uid: uid
    });
    if (!newDoc.data?.id) {
      showToast(`failed to create new doc`, { type: "error" });
      throw new Error("failed to create new doc");
    }
    return newDoc.data.id;
  };

  return { searchDoc, getOrAddDoc }
}

export default useDoc;