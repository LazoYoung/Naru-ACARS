let canvas, bg_img;

window.addEventListener('load', () => {
    canvas = document.getElementById('canvas');
    bg_img = document.getElementById('background');
    for (let img of document.querySelectorAll('img')) {
        img.style.display = 'none';
    }
    loadFonts().catch(reason => {
        window.alert('Failed to load font!\n' + reason);
        console.log(reason);
    }).then(() => drawCanvas());
});

window.addEventListener('resize', () => {
    drawCanvas();
});

function drawCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    let ctx = canvas.getContext('2d');

    const scale = (canvas.width / bg_img.width);
    let height = bg_img.height * scale;
    ctx.drawImage(bg_img, 0, 0, canvas.width, height);

    let flight_icon = document.getElementById('flight-icon');
    let flight_icon_x = 90 * scale;
    let flight_icon_y = 60 * scale;
    let flight_icon_width = flight_icon.width * scale;
    let flight_icon_height = flight_icon.height * scale;
    ctx.drawImage(flight_icon, flight_icon_x, flight_icon_y, flight_icon_width, flight_icon_height);

    let callsign = 'ANZ 157M'
    let callsign_x = 160 * scale;
    let callsign_y = 95 * scale;
    let callsign_size = 30 * scale;
    ctx.fillStyle = 'white';
    ctx.font = callsign_size + 'px staatliches';
    ctx.fillText(callsign, callsign_x, callsign_y);
}

async function loadFonts() {
    let font = new FontFace('staatliches', 'url(/fonts/staatliches.ttf)');
    return font.load()
        .then(() => {
            document.fonts.add(font);
        });
}