import { ReactNode, useEffect, useState } from "react";
import "./index.css";

export default function Dialog({ label, title, children, onOpen, onClose }: {
  label: string,
  title: string,
  children: ReactNode,
  onOpen?: () => void,
  onClose?: () => void
}) {
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    isOpen && onOpen?.()
  }, [isOpen]);

  const openDialog = () => setIsOpen(true);
  const closeDialog = () => {
    setIsOpen(false);
    onClose?.();
  };
  return <>
    <button type="button" style={{ marginRight: 10 }} onClick={openDialog}>
      {label}
    </button>
    {isOpen && (
      <div className="dialog-overlay" onClick={closeDialog}>
        <div className="dialog-content" onClick={e => e.stopPropagation()}>
          <div className="dialog-header">
            <span style={{ fontWeight: "bold" }}>{title}</span>
            <button onClick={closeDialog}>X</button>
          </div>
          <div className={"dialog-body"}>{children}</div>
        </div>
      </div>
    )}
  </>;
}