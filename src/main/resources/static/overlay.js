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

        if (this.type) {
            this.interval = this.getAttribute('interval') * 1000;
            this.timer = this.interval;
        }

        if (this.task_id) {
            window.clearInterval(this.task_id);
        }

        this.startTask();
    }

    startTask() {
        this.task_id = window.setInterval(() => this.updateText(), refresh_ms);
    }

    stopTask() {
        window.clearInterval(this.task_id);
    }

    getText() {
        return this.children.item(this.index);
    }

    updateText() {
        if (this.interval > 0) {
            this.timer -= refresh_ms;

            if (this.timer < 0) {
                let last = this.getText();
                this.timer = this.interval;
                this.index = (++this.index % this.childElementCount);
                this.animateText(last);
                return;
            }
        }

        let div = this.children.item(this.index);
        let scale = getScale();
        let x = this.x * scale;
        let y = this.y * scale;
        let size = this.size * scale;
        let width = this.width * scale;
        div.drawText(x, y, size, width, this.color);
    }

    animateText(last) {
        let fade_out, fade_in;

        switch (this.type) {
            case 'slide':
                fade_out = 'slide-out';
                fade_in = 'slide-in';
                break;
            case 'fade':
            default:
                fade_out = 'fade-out';
                fade_in = 'fade-in';
        }

        last.classList.add(fade_out);
        this.stopTask();
        setTimeout(() => {
            last.innerText = null;

            const text = this.getText();
            text.classList.add(fade_in);
            this.startTask();
            setTimeout(() => {
                last.classList.remove(fade_out)
                text.classList.remove(fade_in)
            }, 1000);
        }, 900);
    }
}
class OverlayText extends HTMLDivElement {
    text_prefix; text_suffix; round_scale; variable;

    constructor() {
        super();
    }

    connectedCallback() {
        this.init();
    }

    attributeChangedCallback() {
        this.init();
    }

    init() {
        this.innerText = null;
        this.text_prefix = this.getAttribute('text-prefix');
        this.text_suffix = this.getAttribute('text-suffix');
        this.round_scale = Number.parseInt(this.getAttribute('round-scale'));
        this.variable = this.getAttribute('variable');
        this.style.setProperty('position', 'absolute');
        this.style.setProperty('z-index', '1');

        if (isNaN(this.round_scale)) {
            this.round_scale = 0;
        }
        if (!this.text_prefix) {
            this.text_prefix = String();
        }
        if (!this.text_suffix) {
            this.text_suffix = String();
        }
    }

    drawText(x, y, size, width, color) {
        if (!this.variable || !sim_data || !sim_data[this.variable]) {
            this.innerText = 'N/A';
        } else {
            let data = sim_data[this.variable];

            if (typeof data === 'number') {
                data = data.toFixed(this.round_scale);
            }
            this.innerText = this.text_prefix.concat(data, this.text_suffix);
        }

        this.style.setProperty('left', `${x}px`);
        this.style.setProperty('top', `${y}px`);
        this.style.setProperty('font-family', font_family);
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
        .then(response => {
            if (response && response.ok) {
                return response.json();
            } else if (response) {
                console.error(`HTTP error: ${response.status}`);
            }
            return null;
        })
        .then(json => {
            if (json) {
                sim_data = json.map;
            }
        })
        .catch(() => {
            sim_data = null;
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
    let family = 'bebasneue';
    let font = new FontFace(family, `url(/font/${family}.ttf)`);
    return font.load()
        .then(() => {
            document.fonts.add(font);
            font_family = family;
        });
}