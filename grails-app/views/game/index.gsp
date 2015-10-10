<%--
  Created by IntelliJ IDEA.
  User: glastram
  Date: 10/04/13
  Time: 16:12
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>GridWars - Scoreboard</title>
    <meta name="layout" content="main"/>
</head>

<body>
<table class="table table-condensed table-hover">
    <tr><th>Username</th><th>Agent</th><th>Wins</th><th>Draws</th><th>Losses</th></tr>
    <g:each in="${agents}" var="agent">
        <tr class="${ agent.team.id == currentLoggedInUserId ? "info" : "" }">
            <td>${agent.team.username}</td>
            <td>${agent.fqClassName}</td>
            <td>${ service.wins(agent) }</td>
            <td>${ service.draws(agent) }</td>
            <td>${ service.losses(agent) }</td>
        </tr>
    </g:each>
</table>
</body>
</html>
