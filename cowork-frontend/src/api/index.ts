import { showToast } from "../components/toast";

export const baseURL = import.meta.env.PROD ?
  `${location.protocol}//api.cowork.local` : `http://${location.host}/api`;

export interface ApiRes<T> {
  data?: T;
  count?: number;
}

export function params(params: any): string {
  if (!params) return "";
  const type = typeof params;
  let str = "?";
  if (type === "string" || params instanceof String) {
    return str + params;
  } else if (type === "object") {
    for (const key in params) {
      let value = params[key];
      if (value === null || value === undefined) {
        continue;
      } else if (Array.isArray(value)) {
        if (!value.length) continue;
        let k = encodeURIComponent(key);
        for (const v of value) {
          str += k + "=" + encodeURIComponent(v) + "&";
        }
        continue;
      }
      str += encodeURIComponent(key) + "=" + encodeURIComponent(value) + "&";
    }
    return str.slice(0, -1);
  } else {
    return "";
  }
}

class HTTPError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "HTTPError";
  }
}

class HTTP {
  private readonly baseURL: string;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
  }

  async fetch<T = any>(
    endpoint: string,
    init?: RequestInit
  ): Promise<ApiRes<T>> {
    const url = `${this.baseURL}${endpoint}`;
    try {
      const response = await fetch(url, init);
      const isJSON = response.headers.get("content-type")?.includes("json");
      if (response.status >= 400) {
        if (response?.status === 401) {
          localStorage.removeItem("access_token");
        }
        throw new HTTPError(
          isJSON
            ? (await response.json())?.error ?? "Unknown error"
            : response.statusText
        );
      }
      if (response.status === 204) {
        return {};
      }
      const count = init?.method == "GET" ?
        parseInt(response.headers.get("x-total-count") ?? "0") : undefined;
      if (isJSON) {
        return {
          data: await response.json(),
          count: count,
        };
      }
      return {
        data: (await response.text()) as T,
        count: count,
      };
    } catch (e: any) {
      const msg = e?.message ?? "Unknown error";
      showToast(msg, { type: "error" });
      throw e;
    }
  }

  async request<T>(
    method: string,
    endpoint: string,
    body?: unknown
  ): Promise<ApiRes<T>> {
    const options: RequestInit = {
      method,
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("access_token") ?? ""}`,
      },
    };

    if (body) {
      options.body = JSON.stringify(body);
    }

    return this.fetch<T>(endpoint, options);
  }

  get<T>(endpoint: string, data?: any): Promise<ApiRes<T>> {
    return this.request("GET", endpoint + params(data));
  }

  post<T>(endpoint: string, data?: any): Promise<ApiRes<T>> {
    return this.request("POST", endpoint, data);
  }

  put<T>(endpoint: string, data?: any): Promise<ApiRes<T>> {
    return this.request("PUT", endpoint, data);
  }

  delete<T>(endpoint: string, data?: any): Promise<ApiRes<T>> {
    return this.request("DELETE", endpoint + params(data));
  }
}

export default new HTTP(baseURL);
