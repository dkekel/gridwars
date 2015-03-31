<%--
  Created by IntelliJ IDEA.
  User: Gerardo
  Date: 10/04/13
  Time: 21:05
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>GridWars - Game list</title>
    <style type="text/css">
    td {
        text-align: center;
        padding: 10px;
    }
    </style>
</head>

<body>
<table>
    <tr><th>Start date</th><th>Players</th><th>Winner</th></tr>
    <g:each in="${games}" var="game">
        <tr>
            <td>
                ${game.startDate.format("yyyy-MM-dd HH:mm:ss")}
            </td>
            <td>
                ${ game.player1.team.username } vs ${ game.player2.team.username }
            </td>
            <td>
                ${ game.winner ? game.winner.team.username : "Draw" }
            </td>
            <td>
                <g:link controller="game" action="view" params="[id: game.id]">View</g:link>
            </td>
        </tr>
    </g:each>
</table>
</body>
</html>
