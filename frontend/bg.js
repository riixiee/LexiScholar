(function () {
  const canvas = document.getElementById("stars");
  const ctx = canvas.getContext("2d");
  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  const LINK_DIST = 130;
  const MOUSE_DIST = 170;
  let w, h, stars = [];
  const mouse = { x: null, y: null };

  function resize() {
    const dpr = window.devicePixelRatio || 1;
    w = window.innerWidth;
    h = window.innerHeight;
    canvas.width = w * dpr;
    canvas.height = h * dpr;
    canvas.style.width = w + "px";
    canvas.style.height = h + "px";
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

    const count = Math.min(110, Math.floor((w * h) / 14000));
    stars = Array.from({ length: count }, () => ({
      x: Math.random() * w,
      y: Math.random() * h,
      vx: (Math.random() - 0.5) * 0.25,
      vy: (Math.random() - 0.5) * 0.25,
      r: Math.random() * 1.4 + 0.4,
      tw: Math.random() * Math.PI * 2
    }));
  }

  function drawLine(x1, y1, x2, y2, alpha) {
    ctx.strokeStyle = "rgba(165, 180, 252," + alpha + ")";
    ctx.lineWidth = 0.7;
    ctx.beginPath();
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.stroke();
  }

  function frame() {
    ctx.clearRect(0, 0, w, h);

    for (const s of stars) {
      if (!reduceMotion) {
        s.x += s.vx;
        s.y += s.vy;
        if (s.x < 0) s.x = w; else if (s.x > w) s.x = 0;
        if (s.y < 0) s.y = h; else if (s.y > h) s.y = 0;
        s.tw += 0.02;
      }
      const alpha = 0.55 + 0.45 * Math.sin(s.tw);
      ctx.fillStyle = "rgba(255, 255, 255," + alpha + ")";
      ctx.beginPath();
      ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
      ctx.fill();
    }

    // Constellation lines between nearby stars
    for (let i = 0; i < stars.length; i++) {
      for (let j = i + 1; j < stars.length; j++) {
        const dx = stars[i].x - stars[j].x;
        const dy = stars[i].y - stars[j].y;
        const d = Math.sqrt(dx * dx + dy * dy);
        if (d < LINK_DIST) drawLine(stars[i].x, stars[i].y, stars[j].x, stars[j].y, (1 - d / LINK_DIST) * 0.35);
      }
      // Lines to the mouse pointer
      if (mouse.x !== null) {
        const mx = stars[i].x - mouse.x;
        const my = stars[i].y - mouse.y;
        const md = Math.sqrt(mx * mx + my * my);
        if (md < MOUSE_DIST) drawLine(stars[i].x, stars[i].y, mouse.x, mouse.y, (1 - md / MOUSE_DIST) * 0.5);
      }
    }

    if (!reduceMotion) requestAnimationFrame(frame);
  }

  window.addEventListener("resize", () => { resize(); if (reduceMotion) frame(); });
  window.addEventListener("mousemove", e => { mouse.x = e.clientX; mouse.y = e.clientY; });
  window.addEventListener("mouseout", () => { mouse.x = null; mouse.y = null; });

  resize();
  frame();
})();