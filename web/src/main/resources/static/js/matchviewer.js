function initws(matchDataUrl, turnCount) {
    var frameArray = [];
    var universeSize = 50;
    var currentTurn = 0;
    var currentRenderedFrame = 0;

    var isPlaying = 0;
    var playInterval;
    var delay = 50;

    drawFrame = function (frameNumber)
    {
        // Canvas
        var canvasHeight = document.getElementById("gameCanvas").height;
        var canvasWidth = document.getElementById("gameCanvas").width;
        var c = document.getElementById("preCanvas");
        var ctx = c.getContext("2d");
        var extractedData = frameArray[frameNumber];
        var imageData = ctx.createImageData(universeSize, universeSize);
        for (var i = 0; i < extractedData.length; i++)
        {
            imageData.data[i] = extractedData[i];
        }
        ctx.putImageData(imageData, 0, 0);

        var realCtx = document.getElementById("gameCanvas").getContext("2d");

        realCtx.imageSmoothingEnabled = false;
        realCtx.mozImageSmoothingEnabled = false;
        realCtx.webkitImageSmoothingEnabled = false;
        realCtx.clearRect(0, 0, canvasWidth, canvasHeight);
        realCtx.drawImage(c, 0, 0);

        currentRenderedFrame = frameNumber;
        document.getElementById("currentTurn").innerHTML = currentRenderedFrame;
    };

    goToStart = function ()
    {
        if (isPlaying) togglePlay();

        drawFrame(0);
    };

    drawNextFrame = function ()
    {
        if (currentRenderedFrame >= frameArray.length - 1)
        {
            if (isPlaying) togglePlay();
        }
        else
        {
            drawFrame(currentRenderedFrame + 1);
        }
    };

    togglePlay = function ()
    {
        if (!isPlaying)
        {
            playInterval = setInterval(drawNextFrame, delay);
        }
        else
        {
            clearInterval(playInterval);
        }
        isPlaying = !isPlaying;
    };

    increaseSpeed = function ()
    {
        delay = Math.round((2 * delay) / 3);
        if (isPlaying)
        {
            togglePlay();
            togglePlay();
        }
    };

    decreaseSpeed = function ()
    {
        delay = Math.round(delay * 1.5)
        if (isPlaying)
        {
            togglePlay();
            togglePlay();
        }
    };

    var canvasHeight = document.getElementById("gameCanvas").height;
    var canvasWidth = document.getElementById("gameCanvas").width;
    currentTime = new Date();
    document.getElementById("preCanvas").height = universeSize;
    document.getElementById("preCanvas").width = universeSize;
    document.getElementById("gameCanvas").getContext("2d").scale(canvasWidth / universeSize, canvasHeight / universeSize);

    console.log("Loading game");

    var xhr = new XMLHttpRequest();
    xhr.open('GET', matchDataUrl, true);
    xhr.responseType = 'arraybuffer';

    xhr.onload = function() {
        var data = this.response;

        var N = universeSize * universeSize * 4;
        for (var turn = 0; turn < turnCount; turn++) {
            frameArray[currentTurn++] = data.slice(N * turn, N * (turn + 1));
        }

        if (currentTurn >= turnCount)
        {
            document.getElementById("gameView").removeAttribute("hidden");
            drawFrame(0);
        }
        // $("#loadedTurns").parent().toggle(false)
    };
    // xhr.addEventListener("progress", function(oEvent) {
    //     var loaded = parseInt(oEvent.loaded / 1024);
    //     var total = ${ game.fileSize / 1024 as int };
    //     var percentage = parseInt(loaded / total * 100);
    //     $("#loadedTurns").width(percentage + "%");
    //     $('#loadedData').html(percentage + "% (" + loaded + " / " + total + " KB)");
    // });
    xhr.send(null);
}
