const img = document.getElementById('frame') as HTMLImageElement;
const stats = document.getElementById('stats') as HTMLParagraphElement;

img.onload = () => {
  stats.textContent = `FPS: sample | Resolution: ${img.naturalWidth}x${img.naturalHeight}`;
};

// Optionally, load a base64 sample if you have it.
// img.src = "data:image/png;base64,....";
