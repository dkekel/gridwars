<%--
  Created by IntelliJ IDEA.
  User: glastram
  Date: 05/04/13
  Time: 15:49
  To change this template use File | Settings | File Templates.
--%>

<%@ page import="cern.ais.gridwars.Agent" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>GridWars - Agent Upload</title>
    <style>
    .btn-file {
        position: relative;
        overflow: hidden;
    }
    .btn-file input[type=file] {
        position: absolute;
        top: 0;
        right: 0;
        min-width: 100%;
        min-height: 100%;
        font-size: 100px;
        text-align: right;
        filter: alpha(opacity=0);
        opacity: 0;
        outline: none;
        background: white;
        cursor: inherit;
        display: block;
    }</style>
</head>

<body>
    <g:if test="${ error }">
        <div class="alert alert-danger">${ error }</div>
    </g:if>
    <g:form controller="agentUpload" method="post" action="upload" enctype="multipart/form-data" class="form">
        <div class="alert alert-warning" role="alert">
            Please <b>do not</b> use Java 8. It will not be uploaded!<br/>
            Maximal file size is <b>10MB</b><br/>
        </div>
        <div class="input-group">
            <input name="fqcn" type="text" class="form-control" placeholder="Fully Qualified name(i.e. com.superuser.MegaBot).">
            <span class="input-group-btn">
                <span class="btn btn-default btn-file">
                    Browse <input name="file" type="file" />
                </span>
                <input class="btn btn-primary" type="submit"/>
            </span>
        </div>
    </g:form>
    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <table class="table">
            <tr>
                <th>#</th>
                <th>Path</th>
                <th>isActive</th>
                <th>ClassName</th>
                <th>Upload date</th>
                <th>Version</th>
                <th>Team</th>
            </tr>
            <g:each in="${agents}">
                <tr>
                    <td>${ it.id }</td>
                    <td>${ it.jarPath }</td>
                    <td>${ it.active }</td>
                    <td>${ it.fqClassName }</td>
                    <td>${ it.uploadDate }</td>
                    <td>${ it.version }</td>
                    <td>${ it.team.username }</td>
                </tr>
            </g:each>
        </table>
    </sec:ifAnyGranted>
</body>
</html>
