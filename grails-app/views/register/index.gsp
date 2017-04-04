<html>
<head>
    <meta name='layout' content='main'/>
    <title><g:message code='spring.security.ui.register.title'/></title>
</head>

<body>
<div class='row'><h2 class="text-center"><g:message code='spring.security.ui.register.description'/></h2></div>
<g:form action='register' name='registerForm' class='form form-horizontal col-md-6 col-md-offset-3'>
    <g:if test='${emailSent}'>
        <div class="form-group">
            <div class="alert alert-success"><g:message code='spring.security.ui.register.sent'/></div>
        </div>
    </g:if>
    <g:else>
        <div class="form-group">
            <label for='username' class="control-label col-sm-4"><g:message code="user.username.label"/>:</label>
            <div class="col-sm-8">
                <g:field type='text' class='form-control' name='username' id='username' value="${command.username}" bean="${command}" />
                <g:hasErrors bean="${command}" field="username">
                    <div class="errors text-danger">
                        <g:renderErrors bean="${command}" field="username" as="list" />
                    </div>
                </g:hasErrors>
            </div>
        </div>
        <div class="form-group">
            <label for='email' class="control-label col-sm-4"><g:message code="user.email.label"/>:</label>
            <div class="col-sm-8 ">
                <g:field type='text' class='form-control' name='email' id='email' value="${command.email}" bean="${command}" />
                <g:hasErrors bean="${command}" field="email">
                    <div class="errors text-danger"><g:renderErrors bean="${command}" field="email" as="list" /></div>
                </g:hasErrors>
            </div>
        </div>
        <div class="form-group">
            <label for='password' class="control-label col-sm-4"><g:message code="user.password.label"/>:</label>
            <div class="col-sm-8 ">
                <g:field type='password' class='form-control' name='password' id='password' value="${command.password}" bean="${command}" />
                <g:hasErrors bean="${command}" field="password">
                    <div class="errors text-danger"><g:renderErrors bean="${command}" field="password" as="list" /></div>
                </g:hasErrors>
            </div>
        </div>
        <div class="form-group">
            <label for='password2' class="control-label col-sm-4"><g:message code="user.password2.label"/>:</label>
            <div class="col-sm-8 ">
                <g:field type='password' class='form-control' name='password2' id='password2' value="${command.password2}" bean="${command}" />
                <g:hasErrors bean="${command}" field="password2">
                    <div class="errors text-danger"><g:renderErrors bean="${command}" field="password2" as="list" /></div>
                </g:hasErrors>
            </div>
        </div>
        <div class="form-group">
            <label for='masterPass' class="control-label col-sm-4"><g:message code="user.masterPass.label"/>:</label>
            <div class="col-sm-8 ">
                <g:field type='password' class='form-control' name='masterPass' id='masterPass' value="${command.masterPass}" bean="${command}" />
                <g:hasErrors bean="${command}" field="masterPass">
                    <div class="errors text-danger"><g:renderErrors bean="${command}" field="masterPass" as="list" /></div>
                </g:hasErrors>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-8">
                <input type='submit' class="btn btn-primary pull-right" id="create" value='${message(code: "spring.security.ui.register.submit")}'/>
            </div>
        </div>
    </g:else>
</g:form>
<script>
    $(document).ready(function() {
        $('#username').focus();
    });
</script>

</body>
</html>
