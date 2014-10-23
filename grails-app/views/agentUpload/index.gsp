<%--
  Created by IntelliJ IDEA.
  User: glastram
  Date: 05/04/13
  Time: 15:49
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>GridWars - Agent Upload</title>
</head>

<body>
<div>${session.user.username} | <g:link controller="user" action="logout">Logout</g:link> |
<g:link controller="game" action="index">View active bot scoreboard</g:link> |
<g:link controller="game" action="list">List games</g:link> |
<g:link controller="agentUpload" action="index">Upload a new bot</g:link> |
    <a href="/api/doc">API Documentation</a> |
    <a href="/api/api.jar">API Download</a> |
    <a href="/api/examples">Examples</a>
</div>
<g:form controller="agentUpload" method="post" action="upload"
        enctype="multipart/form-data">
    <label for="file">.JAR File (max 10 MB):</label> <input id="file" type="file" name="file"/>
    <label for="fqcn">FQ Class Name (e.g. my.package.MyAgent):</label> <input id="fqcn" type="text" name="fqcn"/>
    <input type="submit"/>
</g:form>
</body>
</html>