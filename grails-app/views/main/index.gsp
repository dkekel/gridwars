<html>
<head>
    <title>GridWars - Homepage</title>
</head>

<body>
<g:if test="${session?.user}">
    <div>${session.user.username} | <g:link controller="user" action="logout">Logout</g:link> |
        <g:link controller="game" action="index">View active bot scoreboard</g:link> |
        <g:link controller="game" action="list">List games</g:link> |
        <g:link controller="agentUpload" action="index">Upload a new bot</g:link> |
        <a href="/api/doc">API Documentation</a> |
        <a href="/api/api.jar">API Download</a> |
        <a href="/api/examples">Examples</a>
    </div>
</g:if>
<g:else>
    <g:form url="[controller: 'user', action: 'login']">
        <fieldset>
            <legend>Login</legend>

            <p class="info">
                Please login with your username and password. <br/>
            </p>

            <p>
                <label for="username">Username</label>
                <g:textField name="username"/>
            </p>

            <p>
                <label for="password">Password</label>
                <g:passwordField name="password"/>
            </p>

            <p>
                <g:submitButton class="button" name="submitButton" value="Login"/>
            </p>
        </fieldset>
    </g:form>
</g:else>
</body>
</html>