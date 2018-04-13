function initws(matchDataUrl) {
    const turnFrameDataArray = [];
    const universeSize = 50;
    const bytesPerTurn = universeSize * universeSize * 4;
    const preCanvas = document.getElementById("preCanvas");
    const gameCanvas = document.getElementById("gameCanvas");
    const preCtx = preCanvas.getContext("2d");
    const gameCtx = gameCanvas.getContext("2d");
    const currentTurnLabel = document.getElementById("currentTurn");
    const canvasHeight = gameCanvas.height;
    const canvasWidth = gameCanvas.width;
    let totalTurnCount = 0;
    let currentTurn = 0;
    let isPlaying = 0;
    let playInterval;
    let delay = 50;
    let speed = 1;

    preCanvas.height = universeSize;
    preCanvas.width = universeSize;
    gameCtx.scale(canvasWidth / universeSize, canvasHeight / universeSize);
    gameCtx.imageSmoothingEnabled = false;
    gameCtx.mozImageSmoothingEnabled = false;
    gameCtx.webkitImageSmoothingEnabled = false;

    drawCurrentTurn = function () {
        const imageData = new ImageData(turnFrameDataArray[currentTurn], universeSize, universeSize);
        preCtx.putImageData(imageData, 0, 0);
        gameCtx.clearRect(0, 0, canvasWidth, canvasHeight);
        gameCtx.drawImage(preCanvas, 0, 0);

        currentTurnLabel.innerHTML = '' + (currentTurn + 1) + ' / ' + totalTurnCount + ' (' + speed + 'x)';
    };

    drawNextTurn = function () {
        if (currentTurn < (totalTurnCount - 1)) {
            currentTurn++;
            drawCurrentTurn();
        }
    };

    goToStart = function () {
        stopPlay();
        currentTurn = 0;
        drawCurrentTurn();
    };

    goToEnd = function () {
        stopPlay();
        currentTurn = totalTurnCount - 1;
        drawCurrentTurn();
    };

    goToPreviousFrame = function () {
        stopPlay();
        if (currentTurn > 0) {
            currentTurn--;
            drawCurrentTurn();
        }
    };

    goToNextFrame = function () {
        stopPlay();
        drawNextTurn();
    };

    togglePlay = function () {
        if (!isPlaying) {
            playInterval = setInterval(drawNextTurn, delay);
        } else {
            clearInterval(playInterval);
            playInterval = null;
        }
        isPlaying = !isPlaying;
    };

    startPlay = function () {
        if (!isPlaying) {
            togglePlay();
        }
    };

    stopPlay = function () {
        if (isPlaying) {
            togglePlay();
        }
    };

    increaseSpeed = function () {
        speed = speed * 1.5;
        delay = Math.round(delay / speed);
        stopPlay();
        startPlay();
    };

    decreaseSpeed = function () {
        speed = speed / 1.5;
        delay = Math.round(delay * speed);
        stopPlay();
        startPlay();
    };

    loadData = function (matchDataUrl) {
        const xhr = new XMLHttpRequest();
        xhr.open('GET', matchDataUrl, true);
        xhr.responseType = 'arraybuffer';

        xhr.onload = function () {
            const data = this.response;
            const dataByteSize = data.byteLength;
            console.log('Received data size: ' + dataByteSize);

            totalTurnCount = Math.floor(dataByteSize / bytesPerTurn);
            console.log('Calculated turn count from data: ' + totalTurnCount);

            for (let turn = 0; turn < totalTurnCount; turn++) {
                turnFrameDataArray[turn] =
                    new Uint8ClampedArray(data.slice(bytesPerTurn * turn, bytesPerTurn * (turn + 1)));
            }
            console.log('Frame array length: ' + turnFrameDataArray.length);

            goToStart();
        };

        xhr.send(null);
    };

    loadData(matchDataUrl);
}
