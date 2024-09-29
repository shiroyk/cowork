import { useState } from "react";
import api from "../../api";
import { showToast } from "../../components/toast";

export type Users = {
  username: string;
}[];

const useUser = () => {
  const [token, setToken] = useState(
    localStorage.getItem("access_token") ?? ""
  );

  const signIn = async (username: string, password: string) => {
    const res = await api.post<{ access_token: string }>(`/auth/sign_in`, {
      username,
      password,
    });
    if (!res.data?.access_token) {
      showToast("auth failed", { type: "error" });
      return false;
    }

    localStorage.setItem("access_token", res.data.access_token);
    setToken(res.data.access_token);
    return true;
  };

  const signUp = async (username: string, password: string) => {
    await api.post(`/auth/sign_up`, { username, password });
    showToast("sign up success", { type: "info" });
  };

  const validToken = () => {
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      if (new Date().getTime() > payload.exp * 1000) {
        throw new Error("token expired");
      }
      return true;
    } catch (e) {
      console.log(e);
      localStorage.removeItem("access_token");
      showToast(`invalid token: ${e}`, { type: "error" });
      return false;
    }
  };

  const getUserId = () => JSON.parse(atob(token.split(".")[1])).iss as string;

  return { token, getUserId, signUp, signIn, validToken };
};

export default useUser;