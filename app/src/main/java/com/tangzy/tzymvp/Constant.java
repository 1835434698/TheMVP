package com.tangzy.tzymvp;

import android.content.Context;
import android.os.Environment;

public class Constant {
    public static Context app;
    public static final String url = "http://120.27.23.38:80/api/";
    public static String MESSSAGENET = "无网络";
    public static String MESSSAGE = "网络错误";
    public static String MESSSAGEJSON = "数据解析错误";
    public static int ERRORCODE = -1;
    public static String path = Environment.getExternalStorageDirectory().toString()+"/DemoApp";

    public static final String Api_login = "login";
}
