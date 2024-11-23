import api from "../../api";
import { showToast } from "../../components/toast";

export interface DocInfo {
  title: string;
  uid: string;
  did: string;
  clients: Record<string, number>
}

const useDoc = () => {
  const searchDoc = async () => (await api.get<DocInfo[]>(`/doc`, { title: `test` })).data

  const getOrAddDoc = async () => {
    const docs = await searchDoc();
    if (docs?.length) {
      return docs[0];
    }
    let newDoc = (await api.post<DocInfo>(`/doc`, { title: "test" })).data;
    if (!newDoc?.did) {
      showToast(`failed to create new doc`, { type: "error" });
      throw new Error("failed to create new doc");
    }
    return newDoc;
  };

  return { searchDoc, getOrAddDoc }
}

export default useDoc;