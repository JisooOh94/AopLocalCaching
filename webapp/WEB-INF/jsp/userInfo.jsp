<%--
  Created by IntelliJ IDEA.
  User: USER
  Date: 2020-12-16
  Time: 오전 12:25
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
  <body>
  <c:forEach var="userInfo" items="${userInfoList}">
    <p>userName : ${userInfo.getUserName()}</p>
  </c:forEach>
  </body>
</html>