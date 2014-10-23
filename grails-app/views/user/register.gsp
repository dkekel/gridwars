<%--
  Created by IntelliJ IDEA.
  User: glastram
  Date: 05/04/13
  Time: 16:16
  To change this template use File | Settings | File Templates.
--%>

<html>
<head>
    <title>GridWars - User Registration</title>
</head>

<body>
<g:form url="[controller: 'user', action: 'register']">
    <fieldset>
        <legend>User Registration</legend>

        <p class="info">
            Complete the form below to create an account!
        </p>
        <g:hasErrors bean="${user}">
            <div class="errors">
                <g:renderErrors bean="${user}"/>
            </div>
        </g:hasErrors>
        <p>
            <label for="username">Username</label>
            <g:textField name="username" value="${user?.username}"
                         class="${hasErrors(bean: user, field: 'username', 'errors')}"/>
        </p>

        <p>
            <label for="password">Password</label>
            <g:passwordField name="password"
                             class="${hasErrors(bean: user, field: 'password', 'errors')}"/>
        </p>

        <p>
            <label for="passwordConfirm">Confirm Password</label>
            <g:passwordField name="passwordConfirm"
                             class="${hasErrors(bean: user, field: 'password', 'errors')}"/>
        </p>

        <p>
            <label for="email">Contact e-mail</label>
            <g:textField name="email" value=""/>
        </p>

        <p class="button">
            <label>&nbsp;</label>
            <g:submitButton class="button" name="submitButton" value="Create Account"/>
        </p>
    </fieldset>
</g:form>
</body>
</html>