let sim_data;
let canvas, bg_img;
let refresh_ms = 500;
let font_family = 'serif';

class OverlayLabel extends HTMLElement {
    x; y; size; width; color; type; index; interval; timer; task_id;

    constructor() {
        super();
    }

    connectedCallback() {
        if (this.isConnected) {
            this.init();
        }
    }

    attributeChangedCallback() {
        this.init();
    }

    init() {
        this.x = this.getAttribute('x');
        this.y = this.getAttribute('y');
        this.size = this.getAttribute('size');
        this.width = this.getAttribute('width');
        this.color = this.getAttribute('color');
        this.type = this.getAttribute('type');
        this.index = 0;
        this.interval = 0;
        this.timer = 0;

        if (this.type) { // is animated label
            this.interval = this.getAttribute('interval') * 1000;
            this.timer = this.interval;
        }

        if (this.task_id) {
            window.clearInterval(this.task_id);
        }

        window.setInterval(this.updateText, refresh_ms, this);
    }

    updateText(elem) {
        if (elem.interval > 0) {
            elem.timer -= refresh_ms;

            if (elem.timer < 0) {
                let lastDiv = elem.children.item(elem.index);
                lastDiv.erase();
                elem.timer = elem.interval;
                elem.index = (elem.index + 1 < elem.childElementCount) ? ++elem.index : 0;
            }
        }

        let div = elem.children.item(elem.index);
        let scale = getScale();
        let x = elem.x * scale;
        let y = elem.y * scale;
        let size = elem.size * scale;
        let width = elem.width * scale;
        div.drawText(x, y, size, width, elem.color);
    }
}
class OverlayText extends HTMLDivElement {
    simvar;

    constructor() {
        super();
    }

    connectedCallback() {
        this.innerText = null;
        this.simvar = this.getAttribute('simvar');
        this.style.setProperty('position', 'absolute');
        this.style.setProperty('z-index', '1');
    }

    attributeChangedCallback() {
        this.simvar = this.getAttribute('simvar');
    }

    drawText(x, y, size, width, color) {
        if (!this.simvar || !sim_data || !sim_data[this.simvar]) {
            this.innerText = 'N/A';
        } else {
            this.innerText = sim_data[this.simvar];
        }

        this.style.setProperty('left', `${x}px`);
        this.style.setProperty('top', `${y}px`);
        this.style.setProperty('font-size', `${size}px`);
        if (width) {
            this.style.setProperty('overflow', 'hidden');
            this.style.setProperty('white-space', 'nowrap');
            this.style.setProperty('text-overflow', 'ellipsis')
            this.style.setProperty('max-width', `${width}px`);
        }
        if (color) {
            this.style.setProperty('color', color);
        }
        this.style.setProperty('font-family', font_family);
    }

    erase() {
        this.innerText = null;
    }
}

customElements.define('overlay-label', OverlayLabel);
customElements.define('overlay-text', OverlayText, { extends: 'div' });
window.setInterval(fetchSimData, 500);
window.addEventListener('load', () => {
    canvas = document.getElementById('canvas');
    bg_img = document.getElementById('background');
    for (let img of document.querySelectorAll('img')) {
        img.style.display = 'none';
    }
    loadFonts().catch(reason => {
        window.alert('Failed to load font!\n' + reason);
        console.log(reason);
    }).then(() => {
        drawCanvas();
    });
});
window.addEventListener('resize', () => {
    drawCanvas();
});

function fetchSimData() {
    fetch('/fetch')
        .catch(() => {
            sim_data = null;
        })
        .then(response => {
            if (response && response.ok) {
                return response.json();
            } else if (response) {
                console.error(`HTTP error: ${response.status}`);
            }
        })
        .then(json => {
            if (json) {
                sim_data = json.map;
            }
        });
}

function drawCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    let ctx = canvas.getContext('2d');

    const scale = getScale();
    let height = bg_img.height * scale;
    ctx.drawImage(bg_img, 0, 0, canvas.width, height);

    for (const icon of document.querySelectorAll('.icon')) {
        let data = icon.dataset;
        let x = data.x * scale;
        let y = data.y * scale;
        let width = data.width * scale;
        let height = data.height * scale;
        ctx.drawImage(icon, x, y, width, height);
    }
}

function getScale() {
    return (canvas.width / bg_img.width);
}

async function loadFonts() {
    let family = 'staatliches';
    let font = new FontFace(family, `url(/font/${family}.ttf)`);
    return font.load()
        .then(() => {
            document.fonts.add(font);
            font_family = family;
        });
}