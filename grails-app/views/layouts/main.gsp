<%--
  Created by IntelliJ IDEA.
  User: seagull
  Date: 31/03/15
  Time: 19:15
--%>

<%@ page import="cern.ais.gridwars.security.UserRole; cern.ais.gridwars.security.Role" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="x-ua-compatible" content="IE=9">
    <title><g:layoutTitle default="Grid Wars" /></title>
    <r:require module="jquery"/>
    <g:layoutHead/>
    <g:layoutHead />
    <r:layoutResources />
    <g:javascript library="jquery" />
    <link rel="stylesheet"  type="text/css" href="${resource(dir: 'css', file: 'bootstrap.min.css')}" />
    <script src="${resource(dir: 'js', file: 'bootstrap.min.js')}"></script>
    <script src="${resource(dir: 'js', file: 'pako_inflate.min.js')}"></script>
    <style>
        body { 
            padding-top: 70px;
            padding-bottom: 30px;
        }
        .footer {
            position: fixed;
            bottom: 0;
            width: 100%;
            height: 20px;
            background-color: #f5f5f5;
        }
    </style>
</head>

<body>
<div id="navigationBar" class="navbar navbar-default navbar-fixed-top">
    <div class="navbar-inner">
        <ul class="nav navbar-nav">
            <li><g:link controller="teamMember" action="index"><i class="glyphicon glyphicon-user"></i> Team</g:link></li>
            <li><g:link controller="game" action="index">View active bot scoreboard</g:link></li>
            <li><g:link controller="game" action="list">List games</g:link></li>
            <li><g:link controller="agentUpload" action="index">Upload a new bot</g:link></li>
            <li><a href="/api/docs">API Documentation</a></li>
            <li><a href="/api/emulator-1.0-SNAPSHOT.zip">API Download</a></li>
            <sec:ifAllGranted roles="ROLE_ADMIN">
                <li><g:link controller="admin">Admin</g:link></li>
                <li><g:link controller="admin" action="queue">Status</g:link></li>
            </sec:ifAllGranted>
        </ul>
        <sec:ifLoggedIn>
        <ul class="nav navbar-right">
            <li>
                <g:form class="navbar-form" name='logoutForm' controller="logout" action="">
                    <div class="form-group">
                        <div>Welcome <b><sec:username/>!</b></div>
                    </div>
                    <sec:ifAllGranted roles="ROLE_ADMIN">
                        <g:select
                                name="account"
                                from="${ UserRole.findAllByRole(Role.findByAuthority("ROLE_ADMIN"))*.user*.username.unique() }"
                                onchange="this.form.action='${ createLink(controller: "logout", action: "switchUser")}'; this.form.submit()"
                        />
                    </sec:ifAllGranted>
                    <g:submitButton class="btn btn-link" name="Logout"/>
                </g:form>
            </li>
        </ul>
        </sec:ifLoggedIn>
    </div>
</div>
<div id="body" class="container">
    <div class="row">
        <div class="col-md-12">
            <r:layoutResources />
            <g:layoutBody />
        </div>
    </div>
</div>
<footer class="footer"><span class="text-muted pull-right" style="padding-right: 1em">Copyright CERN, GS-AIS 2015</span></footer>
</body>
</html>
