<%@ page import="cern.ais.gridwars.TeamMember; cern.ais.gridwars.TeamMember.Gender; cern.ais.gridwars.TeamMember.TShirtSize" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Team Members</title>
</head>

<body>
<div class="container">
<table class="table row">
    <g:each in="${ team }">
        <tr>
            <td>${ it.name }</td>
            <td>${ it.lastName }</td>
            <td>${ it.gender }</td>
            <td>${ it.tShirtSize }</td>
            <td><g:link action="delete" id="${ it.id }"><i class="text-danger glyphicon glyphicon-remove"></i></g:link></td>
        </tr>
    </g:each> 
</table>
</div>
<hr />
<%
     def getStyle = { 
         member == null ? "glyphicon-asterisk text-info" : hasErrors(bean: member, field: it, "!") ? "glyphicon-remove text-danger" : "glyphicon-ok text-success"
     }
%>
<g:if test="${ member == null }">
    <div class="alert alert-info row">
        Please register <strong>all</strong> team members using form below.
        We will use information provided for giving prizes.
    </div>
</g:if>
<g:hasErrors bean="${member}">
    <div class="alert alert-danger row">
        <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
        <span class="sr-only">Error:</span>Please correct errors highlighted below.
    </div>
</g:hasErrors>
<div class="container row">
<g:form action="add" class="form-horizontal">
    <div class="form-group">
        <div class="col-sm-6">
            <div class="input-group">
                <g:field name="name" type="text" class="form-control" placeholder="Name" value="${ fieldValue(bean: member, field: "name") }" />
                <span class="input-group-addon"><i class="glyphicon ${ getStyle("name") }"></i></span>
            </div>
        </div>
        <div class="col-sm-6">
            <div class="input-group">
                <g:field name="lastName" type="text" class="form-control" placeholder="Last Name" value="${ fieldValue(bean: member, field: "lastName") }" />
                <span class="input-group-addon"><i class="glyphicon ${ getStyle("lastName") }"></i></span>
            </div>
        </div>
    </div>
    <div class="form-group">
        <div>
            <label for="gender" class="col-sm-6">Gender</label>
            <label for="tShirtSize" class="col-sm-6">T-Shirt Size</label>
        </div>
            <div class="col-sm-6">
                <g:select name="gender" class="form-control" from="${ Gender.values() }" />
            </div>
            <div class="col-sm-6">
                <g:select name="tShirtSize" class="form-control" from="${ TShirtSize.values() }" />
            </div>
        </div>
        <input class="btn btn-primary pull-right" type="submit"/>
    </div>
    </div>
</g:form>
</div>
</body>
</html>
