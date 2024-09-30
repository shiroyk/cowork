import { useState } from "react";
import api from "../../api";
import { showToast } from "../../components/toast";

export interface User {
    id: string;
    username: string;
    email: string;
}

export type Users = User[];

const useUser = () => {
  const [accessToken, setAccessToken] = useState(
    localStorage.getItem("access_token")
  );

  const signIn = async (username: string, password: string) => {
    const res = await api.post<{ access_token: string, refresh_token: string }>(`/auth/sign_in`, {
      username,
      password,
    });
    if (!res.data?.access_token) {
      showToast("auth failed", { type: "error" });
      return false;
    }

    localStorage.setItem("access_token", res.data.access_token);
    localStorage.setItem("refresh_token", res.data.refresh_token);
    setAccessToken(res.data.access_token);
    return true;
  };

  const signUp = async (username: string, password: string) => {
    await api.post(`/auth/sign_up`, { username, password });
    showToast("sign up success", { type: "info" });
  };

  const refreshToken = async () => {
    const token = localStorage.getItem("refresh_token");
    if (!token) return null;
    try {
      const res = await api.fetch<{ access_token: string, refresh_token?: string }>(`/auth/refresh`, {
        method: "POST",
        headers: {
          "X-Refresh-Token": token,
        }
      });
      if (!res.data?.access_token) {
        return null;
      }
      const { access_token } = res.data;
      localStorage.setItem("access_token", access_token);
      if (res.data.refresh_token) {
        localStorage.setItem("refresh_token", res.data.refresh_token);
      }
      setAccessToken(access_token);
      return access_token;
    } catch (e) {
      console.log(e);
      showToast(`failed to refresh token: ${e}`, { type: "error" });
    }
    return null;
  }

  const validToken = async () => {
    let token = accessToken;
    if (!accessToken) {
      token = await refreshToken();
    }
    if (!token) return null;
    try {
      const payload = parseToken(token);
      if (new Date().getTime() > payload.exp * 1000) {
        return await refreshToken();
      }
      return token;
    } catch (e) {
      console.log(e);
      localStorage.removeItem("access_token");
      showToast(`invalid token: ${e}`, { type: "error" });
      return null;
    }
  };

  const parseToken = (token: string) => JSON.parse(atob(token.split(".")[1])) as { iss: string, sub: string, exp: number }

  const getInfo = async () => {
    let token = await validToken();
    if (!token) return null;
    return parseToken(token);
  }

  return { getInfo, signUp, signIn };
};

export default useUser;