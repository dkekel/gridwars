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
    <meta name="layout" content="main"/>
    <title>GridWars - Agent Upload</title>
</head>

<body>
<div>
<g:form controller="agentUpload" method="post" action="upload"
        enctype="multipart/form-data">
    <label for="file">.JAR File (max 10 MB):</label> <input id="file" type="file" name="file"/>
    <label for="fqcn">FQ Class Name (e.g. my.package.MyAgent):</label> <input id="fqcn" type="text" name="fqcn"/>
    <input type="submit"/>
</g:form>
</body>
</html>
