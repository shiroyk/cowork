import "./index.css";

export function Loading({ size = 14 }: { size?: number }) {
  return <div className="loading-container" style={{ width: size, height: size }}></div>;
}