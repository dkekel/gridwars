<%--
  Created by IntelliJ IDEA.
  User: seagull
  Date: 31/03/15
  Time: 19:15
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="x-ua-compatible" content="IE=9">
    <title><g:layoutTitle default="Grid Wars" /></title>
    <r:require module="jquery"/>
    <g:layoutHead />
    <r:layoutResources />
</head>

<body>
<nav id="navigationBar" class="navbar navbar-default navbar-fixed-top">
    <div class="navbar-inner container-fluid">
        <ul class="nav navbar-nav">
            <li><g:link controller="game" action="index">View active bot scoreboard</g:link></li>
            <li><g:link controller="game" action="list">List games</g:link></li>
            <li><g:link controller="agentUpload" action="index">Upload a new bot</g:link></li>
            <li><a href="/api/doc">API Documentation</a></li>
            <li><a href="/api/api.jar">API Download</a></li>
            <li><a href="/api/examples">Examples</a></li>
            <sec:ifAllGranted roles="ROLE_ADMIN">
                <li><g:link controller="admin">Admin</g:link></li>
            </sec:ifAllGranted>
            <li><sec:ifLoggedIn>Welcome <b><sec:username/>!</b><g:form name='logoutForm' controller="logout" action=""><g:submitButton name="Logout"/></g:form></sec:ifLoggedIn></li>
        </ul>
    </div>
</nav>
<div id="body">
    <r:layoutResources />
    <g:layoutBody />
</div>
</body>
</html>
