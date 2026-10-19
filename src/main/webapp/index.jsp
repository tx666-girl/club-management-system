<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
    <title>登录</title>
  </head>
  <body>
    <input type="text" id="username">
    <input type="text" id="password">
    <input type="button" value="登录" id="login">
  </body>
  <script type="text/javascript" src="js/jquery-3.6.0.js"></script>
  <script type="text/javascript">
    $(function () {
      $("#login").click(function () {
        $.ajax({
          url: 'login',
          type: 'post',
          contentType: 'application/x-www-form-urlencoded;charset=UTF-8',
          data: {
            username: $("#username").val(),
            password: $("#password").val()
          },
          success: function (resp) {
            if(resp === "1"){
              window.location.href = "main.jsp";
            } else if(resp === "-1"){
              alert("账号不存在");
            } else {
              alert("账号或密码错误");
            }
          }
        })
      })
    })
  </script>
</html>
