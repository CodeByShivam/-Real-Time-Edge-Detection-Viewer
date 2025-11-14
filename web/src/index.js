document.addEventListener("DOMContentLoaded", function () {
    const img = document.getElementById("frame");
    const stats = document.getElementById("stats");

    if (!img || !stats) {
        console.error("Missing DOM elements: #frame or #stats");
        return;
    }

    // When the image loads, update resolution text
    img.onload = function () {
        const width = img.naturalWidth;
        const height = img.naturalHeight;

        stats.textContent = `FPS: sample | Resolution: ${width}x${height}`;
    };

    // If you want to use a base64-encoded image instead of PNG:
    // img.src = "data:image/png;base64,....";
});
