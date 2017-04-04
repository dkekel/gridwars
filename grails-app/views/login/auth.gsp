<html>
<head>
    <meta name='layout' content='main'/>
    <title><g:message code="springSecurity.login.title"/></title>
    <style>
        #loginForm {
            margin-top: 3em;
        }
    </style>
</head>

<body>
<div class='row'><h2 class="text-center"><g:message code="springSecurity.login.header"/></h2></div>

<form action='${postUrl}' method='POST' id='loginForm' class='form form-horizontal col-md-4 col-md-offset-4' autocomplete='off'>
    <div class="form-group">
        <label for='username' class="control-label col-sm-2"><g:message code="springSecurity.login.username.label"/>:</label>
        <div class="col-sm-10 ">
            <input type='text' class='form-control' name='j_username' id='username'/>
        </div>
    </div>
    <div class="form-group">
        <label for='password' class="control-label col-sm-2"><g:message code="springSecurity.login.password.label"/>:</label>
        <div class="col-sm-10 ">
            <input type='password' class='form-control' name='j_password' id='password'/>
        </div>
    </div>
    <div id="remember_me_holder" class="checkbox">
        <label class="control-label">
            <input type='checkbox' name='${rememberMeParameter}' id='remember_me' <g:if test='${hasCookie}'>checked='checked'</g:if>/>
            <g:message code="springSecurity.login.remember.me.label"/>
        </label>
    </div>
    <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
            <input type='submit' class="btn btn-primary pull-right" id="submit" value='${message(code: "springSecurity.login.button")}'/>
        </div>
    </div>
    <g:if test='${flash.message}'>
        <div class="form-group">
            <div class='alert alert-danger'>${flash.message}</div>
        </div>
    </g:if>
    <div class="form-group"><hr/></div>
    <div class="form-group"><g:message code="auth.register.prompt.label" /> <g:link controller="register"><g:message code="auth.register.prompt.link" /></g:link></div>
</form>

<script type='text/javascript'>
    <!--
    (function() {
        document.forms['loginForm'].elements['j_username'].focus();
    })();
    // -->
</script>
</body>
</html>
