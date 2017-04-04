<%--
  Created by IntelliJ IDEA.
  User: seagull
  Date: 03/11/15
  Time: 15:20
--%>

<%@ page import="cern.ais.gridwars.TeamMember" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Teams</title>
</head>
<body>
<div class="container">
    <table class="table row">
        <g:each in="${ users }">
            <tr>
                <td><g:link action="index" id="$it.id">${ it.id }</g:link></td>
                <td>${ it.username }</td>
                <td>${ TeamMember.countByTeam(it) }</td>
                <td>${ TeamMember.findAllByTeam(it).groupBy { it.tShirtSize }.collect { "$it.key(${ it.value.size() })" }.join(', ') }</td>
            </tr>
        </g:each>
    </table>
</div>
</body>
</html>
