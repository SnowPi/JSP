package com;

/**
 * Created by chenyufeng on 17/4/16.
 */

import com.oracle.tools.packager.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ShowGrade extends HttpServlet {
    private int TIME_OUT = 5; //超时时间设置
    private String cookie = null;//保存cookie
    private String picturesrc;//保存图片地址
    private String username;//用户名
    private String password;//密码
    private String verificationCode;//验证码
    private HttpSession session;
    private HttpServletRequest req;
    private HttpServletResponse resp;


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        this.req = req;
        this.resp = resp;
        //设传来的参数为用户名密码和验证码结果
        session = req.getSession();
        username = req.getParameter("username");
        password = req.getParameter("password");
        verificationCode = req.getParameter("verificationCode");


//        String login_fail = "fail.jsp";
//        resp.sendRedirect(login_fail);

    }


    private void getCookieAndPictureSrc() {//获取cookie和图片地址
        new Thread(new Runnable() {
            //            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                            .build();

                    Request request = new Request.Builder()
                            .url("http://202.118.31.197")
                            .build();

                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    String cookieString = response.header("Set-Cookie");
                    //获取set-cookie段的所有值，保存在cookieString中

                    Log.info("用okhttp：\n" + responseData);
                    Log.info(cookieString);

                    String[] cookieList = cookieString.split(";");
                    //用分号分割

                    cookie = cookieList[0];
                    //第一个就是cookie
                    Log.info("cookie=" + cookie);

                    picturesrc = responseData.substring(responseData.indexOf("ACTIONVALIDATERANDOMPICTURE"), responseData.indexOf("ACTIONVALIDATERANDOMPICTURE") + 64);
                    picturesrc = picturesrc.substring(0, picturesrc.indexOf("\""));
                    //从整个html的代码中获取到图片的地址

                    Log.info("图片地址:" + picturesrc);

                    getPicture();
                    //获取验证码方法

                } catch (Exception e) {

//                    ToastShow("无法连接到教务处");
                    //超时处理
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private void getPicture() {//利用现有的cookie去获取图片
        new Thread(new Runnable() {
            //            @Override
            public void run() {
                try {

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                            .build();

                    Request request = new Request.Builder()
                            .url("http://202.118.31.197" + "/" + picturesrc)
                            .addHeader("Cookie", cookie)
                            .build();

                    ResponseBody body = client.newCall(request).execute().body();
                    InputStream in = body.byteStream();
//                    bitmap = BitmapFactory.decodeStream(in);
//                    showPicture(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
//                    ToastShow("无法连接到教务处");
                }
            }
        }).start();
    }

    private void judgeLoginInfomation() {
        new Thread(new Runnable() {
            //            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
//                    Agnomen 30
//                    Password cyf512518
//                    WebUserNO 20154863
//                    applicant ACTIONQUERYSTUDENTSCORE
//                    submit7 µÇÂ¼
                    RequestBody requestBody = new FormBody.Builder()
                            .add("WebUserNO", username)
                            .add("Password", password)
                            .add("Agnomen", verificationCode)
                            .add("submit7", "%B5%C7%C2%BC")
                            .build();

                    Request request = new Request.Builder()
                            .url("http://202.118.31.197/ACTIONLOGON.APPPROCESS?mode=")
                            .addHeader("Cookie", cookie)
                            .addHeader("Referer", "http://202.118.31.197")
                            .post(requestBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.info("data1=\n" + responseData);

                    if (responseData.indexOf("请输入正确的附加码") != -1) {
                        session.setAttribute("warning", "请输入正确的附加码");
                        resp.sendRedirect(req.getContextPath() + "/ShowGrade.jsp");
                    } else if (responseData.indexOf("您的密码错误了1次，共3次错误机会！") != -1) {
                        session.setAttribute("warning", "您的密码错误了1次，共3次错误机会！");
                        resp.sendRedirect(req.getContextPath() + "/ShowGrade.jsp");
                    } else if (responseData.indexOf("您的密码错误了2次，共3次错误机会！") != -1) {
                        session.setAttribute("warning", "您的密码错误了2次，共3次错误机会！");
                        resp.sendRedirect(req.getContextPath() + "/ShowGrade.jsp");
                    } else if (responseData.indexOf("您的密码错误了3次，共3次错误机会！") != -1) {
                        session.setAttribute("warning", "您的密码错误了3次，共3次错误机会！");
                        resp.sendRedirect(req.getContextPath() + "/ShowGrade.jsp");
                    } else if (responseData.indexOf("密码错误次数超限，锁定登录5分钟！") != -1) {
                        session.setAttribute("warning", "密码错误次数超限，锁定登录5分钟！");
                        resp.sendRedirect(req.getContextPath() + "/ShowGrade.jsp");
                    } else {
//                        Intent intent = new Intent(LoginIn.this, ShowGrade.class);
//                        intent.putExtra("cookie", cookie);
//                        startActivity(intent);
                        //把验证成功的cookie传给下一个活动。
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
