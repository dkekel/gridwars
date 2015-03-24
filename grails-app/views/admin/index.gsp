<%--
  Created by IntelliJ IDEA.
  User: seagull
  Date: 24/03/15
  Time: 11:50
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Admin</title>
</head>

<body>
<g:form name="updateForm" action="update">
    <g:textArea name="config" rows="50">${config}</g:textArea>
    <g:submitButton name="update"/>
</g:form>
<div>
    <ul>
    <g:each in="${ grailsApplication.controllerClasses }">
        <li><g:link controller="${it.name}">${ it.fullName }</g:link></li>
    </g:each>
    </ul>
</div>
</body>
</html>
