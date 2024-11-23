
// generate random hls color
export function randomColor() {
  let h = Math.floor(Math.random() * 360);
  let s = Math.floor(Math.random() * 40) + 50;
  let l = Math.floor(Math.random() * 30) + 40;

  return `hsl(${h}, ${s}%, ${l}%)`;
}
