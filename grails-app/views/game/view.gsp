<%--
  Created by IntelliJ IDEA.
  User: Gerardo
  Date: 13/04/13
  Time: 16:34
  To change this template use File | Settings | File Templates.
--%>

<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; cern.ais.gridwars.GameConstants;" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>GridWars - Game Viewer</title>
    <script>
         function initws() {
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
             xhr.open('GET', '${ g.createLink(action: "data", id: game.id)}', true);
             xhr.responseType = 'arraybuffer';
             xhr.onload = function() {
                 var data = this.response;
                 data = pako.inflate(data);
                 var HEADER_SIZE = 0; // size of header in bytes (current: 2 bytes turns for num).
                 var turnsLoaded = ${ game.turns };

                 var N = universeSize * universeSize * 4;
                 for (var turn = 0; turn < turnsLoaded; turn++) {
                     frameArray[currentTurn++] = data.slice(HEADER_SIZE + N * turn, HEADER_SIZE + N * (turn + 1))
                 }

                 if (currentTurn >= ${ game.turns })
                 {
                     document.getElementById("gameView").hidden = "";
                     drawFrame(0);
                 }
                 $("#loadedTurns").parent().toggle(false)
             };
             xhr.addEventListener("progress", function(oEvent) {
                 var loaded = parseInt(oEvent.loaded / 1024);
                 var total = ${ game.fileSize / 1024 as int };
                 var percentage = parseInt(loaded / total * 100);
                 $("#loadedTurns").width(percentage + "%");
                 $('#loadedData').html(percentage + "% (" + loaded + " / " + total + " KB)");
             });
             xhr.send(null);
        }
    </script>
</head>

<body>
<div onload="initws()">
    Players:
    <g:set var="i" value="${0}" />
    <g:each in="[game.player1, game.player2]">
        <span style="color: ${GameConstants.getRGB(i++)}">${ it.team.username }<g:if test="${ it.team.id == currentLoggedInUserId || SpringSecurityUtils.ifAllGranted('ROLE_ADMIN') }"> (<g:link action="playerOutput" params="${ [bot: it.id, game: game.id] }">View log</g:link>) </g:if></span>
    </g:each>
    <br/>
    Winner: ${ game.winner?.team?.username ?: "None" }<br/>
    Turns to complete: ${ game.turns }<br/>
    <div class="row">
        <div id="loadedData" class="col-md-2"></div>
        <div class="progress col-md-10" style="padding: 0">
            <div id="loadedTurns" class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100"><span class="sr-only"></span></div>
        </div>
    </div>
</div>

<div id="gameView" hidden="hidden">
    <canvas id="gameCanvas" style="border: 1px solid black;" width="600" height="600"></canvas>

    <div>
        <button onclick="goToStart();">|&lt;</button>
        <button onclick="drawFrame(--currentRenderedFrame);">&lt;</button>
        <button onclick="togglePlay();">Play / pause</button>
        <button onclick="drawFrame(++currentRenderedFrame);">&gt;</button>
        <button onclick="decreaseSpeed();">Decrease speed</button>
        <button onclick="increaseSpeed();">Increase speed</button>
        Current displayed turn: <span id="currentTurn"></span>
    </div>
</div>

<canvas id="preCanvas" hidden="hidden"></canvas>
<script>$(initws)</script>
</body>
</html>
