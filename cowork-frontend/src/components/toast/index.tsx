import "./index.css";
import { createRef, forwardRef, Ref, useImperativeHandle, useState } from "react";

export const toastRef = createRef<ToastRef>();

export const showToast = (msg: string, options?: Options) => {
  toastRef.current?.show(msg, options)
}

type ToastType = "error" | "info";

interface Options {
  timeout?: number;
  type?: ToastType;
}

export interface ToastRef {
  show: (msg: string, options?: Options) => void;
}

const Toast = forwardRef((_, ref: Ref<ToastRef>) => {
  const [msg, setMsg] = useState("");
  const [className, setClassName] = useState<string | undefined>();
  const [prev, setPrev] = useState<number | null>(null);

  const show = (msg: string, options?: Options) => {
    if (prev) {
      clearTimeout(prev);
    }
    setMsg(msg);
    setClassName(`show ${options?.type ?? ""}`);
    const id = setTimeout(() => {
      setClassName(undefined);
      setMsg("");
      setPrev(null);
    }, options?.timeout ?? 3000);
    setPrev(id);
  }

  useImperativeHandle(ref, () => ({ show }))


  return <div id="toast" className={ className }>{ msg }</div>
})

export default Toast;