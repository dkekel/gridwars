<%--
  Created by IntelliJ IDEA.
  User: seagull
  Date: 31/03/15
  Time: 20:37
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>PendingMatches</title>
</head>
<body>
<table>
    <thead>
    <tr>
        <td>id</td>
        <td>player1</td>
        <td>player2</td>
        <g:if test="${ showStatus }">
            <td>status</td>
        </g:if>
    </tr>
    </thead>
    <g:each in="${ matches }">
        <tr>
            <td>${ it.id }</td>
            <td>${ it.player1.team.username }: ${ it.player1.fqClassName }</td>
            <td>${ it.player2.team.username }: ${ it.player2.fqClassName }</td>
            <g:if test="${ showStatus }">
                <td>${ it.status }</td>
            </g:if>
        </tr>
    </g:each>
</table>
</body>
</html>
