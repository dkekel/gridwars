"use strict";

window.matchViewer = (function() {
    const turnDataArray = [];
    const universeSize = 50;
    const bytesPerTurn = universeSize * universeSize * 4;
    const baseDelayMillis = 50;
    const speedSteps = [0.33, 0.66, 1, 2, 3, 4, 8, 16];

    let totalTurnCount = 0;
    let currentTurn = 0;
    let isPlaying = 0;
    let speedStepIndex = 2;
    let playIntervalCallback;
    let errorLabel;
    let gameView;
    let preCanvas;
    let gameCanvas;
    let preCtx;
    let gameCtx;
    let currentTurnLabel;
    let speedLabel;
    let canvasHeight;
    let canvasWidth;

    function init(matchDataUrl) {
        errorLabel = document.getElementById('errorLabel');
        gameView = document.getElementById('gameView');
        preCanvas = document.getElementById("preCanvas");
        gameCanvas = document.getElementById("gameCanvas");
        preCtx = preCanvas.getContext("2d");
        gameCtx = gameCanvas.getContext("2d");
        currentTurnLabel = document.getElementById("currentTurn");
        speedLabel = document.getElementById("speed");
        canvasHeight = gameCanvas.height;
        canvasWidth = gameCanvas.width;

        preCanvas.height = universeSize;
        preCanvas.width = universeSize;
        gameCtx.scale(canvasWidth / universeSize, canvasHeight / universeSize);
        gameCtx.imageSmoothingEnabled = false;
        gameCtx.mozImageSmoothingEnabled = false;
        gameCtx.webkitImageSmoothingEnabled = false;

        updateSpeedLabel();

        loadData(matchDataUrl);
    }

    function loadData(matchDataUrl) {
        const xhr = new XMLHttpRequest();
        xhr.open('GET', matchDataUrl, true);
        xhr.responseType = 'arraybuffer';

        xhr.onload = function () {
            if (this.status !== 200) {
                showLoadingError();
                return;
            }

            const data = this.response;
            const dataByteSize = data.byteLength;
            console.log('Received data size: ' + dataByteSize);

            totalTurnCount = Math.floor(dataByteSize / bytesPerTurn);
            console.log('Turn count: ' + totalTurnCount);

            for (let turn = 0; turn < totalTurnCount; turn++) {
                turnDataArray[turn] = new Uint8ClampedArray(data.slice(bytesPerTurn * turn, bytesPerTurn * (turn + 1)));
            }

            showElement(gameView);
            goToStart();
        };

        xhr.onerror = function() {
            showLoadingError();
        };

        xhr.send();
    }

    function showLoadingError() {
        showElement(errorLabel);
    }

    function showElement(element) {
        element.removeAttribute('hidden');
    }

    function drawCurrentTurn() {
        const imageData = new ImageData(turnDataArray[currentTurn], universeSize, universeSize);
        preCtx.putImageData(imageData, 0, 0);
        gameCtx.clearRect(0, 0, canvasWidth, canvasHeight);
        gameCtx.drawImage(preCanvas, 0, 0);
        updateCurrentTurnLabel();
    }

    function updateCurrentTurnLabel() {
        currentTurnLabel.innerHTML = '' + (currentTurn + 1) + ' / ' + totalTurnCount;
    }

    function drawNextTurn() {
        if (currentTurn < (totalTurnCount - 1)) {
            currentTurn++;
            drawCurrentTurn();
        } else {
            stopPlay();
        }
    }

    function goToStart() {
        stopPlay();
        currentTurn = 0;
        drawCurrentTurn();
    }

    function goToEnd() {
        stopPlay();
        currentTurn = totalTurnCount - 1;
        drawCurrentTurn();
    }

    function goToPreviousTurn() {
        stopPlay();
        if (currentTurn > 0) {
            currentTurn--;
            drawCurrentTurn();
        }
    }

    function goToNextTurn() {
        stopPlay();
        drawNextTurn();
    }

    function startPlay() {
        if (!isPlaying) {
            isPlaying = true;
            const delayMillis = baseDelayMillis / speedSteps[speedStepIndex];
            playIntervalCallback = setInterval(drawNextTurn, delayMillis);
        }
    }

    function stopPlay() {
        if (isPlaying) {
            isPlaying = false;
            clearInterval(playIntervalCallback);
            playIntervalCallback = null;
            console.log("Stopped");
        }
    }

    function togglePlay() {
        if (isPlaying) {
            stopPlay();
        } else {
            startPlay();
        }
    }

    function restartPlay() {
        stopPlay();
        startPlay();
    }

    function increaseSpeed() {
        speedStepIndex = Math.min(speedSteps.length - 1, speedStepIndex  + 1);
        updateSpeedLabel();

        if (isPlaying) {
            restartPlay();
        }
    }

    function decreaseSpeed() {
        speedStepIndex = Math.max(0, speedStepIndex  - 1);
        updateSpeedLabel();

        if (isPlaying) {
            restartPlay();
        }
    }

    function updateSpeedLabel() {
        speedLabel.innerHTML = ''+  speedSteps[speedStepIndex];
    }

    return {
        init: init,
        goToStart: goToStart,
        goToEnd: goToEnd,
        goToPreviousTurn: goToPreviousTurn,
        goToNextTurn: goToNextTurn,
        togglePlay: togglePlay,
        increaseSpeed: increaseSpeed,
        decreaseSpeed: decreaseSpeed
    }
})();
