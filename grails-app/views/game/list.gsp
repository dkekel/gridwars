<%--
  Created by IntelliJ IDEA.
  User: Gerardo
  Date: 10/04/13
  Time: 21:05
  To change this template use File | Settings | File Templates.
--%>

<%@ page import="cern.ais.gridwars.Match" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>GridWars - Game list</title>
    <meta name="layout" content="main"/>
</head>

<% // I know, how terrible it is, to have complex logic here.
    def getColumnClass = { Match game, id -> 
    if (game.player1.team.id != id && game.player2.team.id != id) return ""
    return !game.winner ? "warning" : game.winner.team.id == id ? "success" : "danger"
} %>

<body>
<table class="table table-condensed table-hover">
    <tr><th></th><th>Winner</th><th>Players</th><th>Start date</th><th>File Size</th></tr>
    <g:each in="${games}" var="game">
        <tr class="${ getColumnClass(game, currentLoggedInUserId) }">
            <td><g:link controller="game" action="view" params="[id: game.id]">View</g:link></td>
            <td>${ game.winner ? game.winner.team.username : "Draw" }</td>
            <td>${ game.player1.team.username } vs ${ game.player2.team.username }</td>
            <td>${ game.startDate.format("yyyy-MM-dd HH:mm:ss") }</td>
            <td>${ (game.fileSize / 1024) as int } KB</td>
        </tr>
    </g:each>
</table>
</body>
</html>
