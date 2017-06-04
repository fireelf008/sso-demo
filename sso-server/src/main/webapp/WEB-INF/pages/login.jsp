<%--
  Created by IntelliJ IDEA.
  User: fireelf
  Date: 2017/6/4
  Time: 21:12
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>登录</title>
</head>
<body>
    <form method="post" action="/subLogin">
        用户名：<input type="text" id="username" name="username" /><br>
        密&nbsp;&nbsp;码：<input type="password" id="password" name="password" /><br>
        <input type="hidden" id="s" name="s" value="<%=request.getAttribute("s")%>" />
        <input type="hidden" id="r" name="r" value="<%=request.getAttribute("r")%>" />
        <input type="submit" value="提交" />
    </form>
</body>
</html>
