<%--
  Created by IntelliJ IDEA.
  User: glastram
  Date: 10/04/13
  Time: 16:12
  To change this template use File | Settings | File Templates.
--%>

<%@ page import="cern.ais.gridwars.Match;" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>GridWars - Scoreboard</title>
    <meta name="layout" content="main"/>
    <style type="text/css">
    td {
        text-align: center;
        padding: 10px;
    }
    </style>
</head>

<body>
<table>
    <tr><th>Username</th><th>Agent</th><th>Wins</th><th>Draws</th><th>Losses</th></tr>
    <g:each in="${agents}" var="agent">
        <tr>
            <td>
                ${agent.team.username}
            </td>
            <td>
                ${agent.fqClassName}
            </td>
            <td>
                ${ Match.countByWinner(agent) }
            </td>
            <td>
                ${ Match.findAllByPlayer1OrPlayer2(agent, agent).grep { it.winner = null }.size() }
            </td>
            <td>
                ${ Match.findAllByPlayer1OrPlayer2(agent, agent).grep { it.winner != agent }.size() }
            </td>
        </tr>
    </g:each>
</table>
</body>
</html>
