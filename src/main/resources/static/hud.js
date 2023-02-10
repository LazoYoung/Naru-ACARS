let canvas, img;

window.addEventListener('load', () => {
    canvas = document.getElementById('canvas');
    img = document.getElementById('background');
    img.style.display = 'none';
    drawCanvas();
});

window.addEventListener('resize', () => {
    drawCanvas();
});

function drawCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    let ctx = canvas.getContext('2d');
    let width = canvas.width;
    let height = img.height * width / img.width;
    ctx.drawImage(img, 0, 0, width, height);
}