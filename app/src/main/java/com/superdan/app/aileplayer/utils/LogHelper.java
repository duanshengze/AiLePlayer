package com.superdan.app.aileplayer.utils;

import android.util.Log;

import com.superdan.app.aileplayer.BuildConfig;

/**
 * Created by Administrator on 2016/4/7.
 */
public class LogHelper {
    //前缀
    private  static final String LOG_PREFIX="aile_";
    private  static final  int LOG_PREFIX_LENGTH=LOG_PREFIX.length();
    private static final  int MAX_LOG_TAG_LENGTH=23;

    //生成日志标签
    public static String makeLogTag(String str){

        if (str.length()>MAX_LOG_TAG_LENGTH-LOG_PREFIX_LENGTH){
            return LOG_PREFIX+str.substring(0,MAX_LOG_TAG_LENGTH-LOG_PREFIX_LENGTH-1);
        }
            return LOG_PREFIX+str;
    }

    /**
     *不要使用此方法当混淆这个类名时
     */
    public static String makeLogTag(Class cls){
        return  makeLogTag(cls.getSimpleName());
    }

    public static void d(String tag,Object...meaasges){

        if(BuildConfig.DEBUG){
            log(tag,Log.DEBUG,null,meaasges);
        }
    }


    public static void e(String tag,Object...messages){
        log(tag,Log.ERROR,null,messages);
    }
    public  static void e(String tag,Throwable t,Object...messages){
        log(tag,Log.ERROR,t,messages);
    }


    public  static  void w(String tag,Object...messages){
        log(tag,Log.WARN,null,messages);
    }

    public static  void w(String tag,Throwable t,Object...messages){
        log(tag,Log.WARN,t,messages);
    }



    public static void log(String tag,int level,Throwable t,Object...messages){
        if(Log.isLoggable(tag,level)){
            String message;
            if(t==null&&messages!=null&&messages.length==1){
               // handle this common case without the extra cost of creating a stringbuffer
                //处理这种常见的情况，无需创建一个StringBuffer的额外费用
                message=messages[0].toString();
            }else {
                StringBuilder sb=new StringBuilder();//同步的  与StringBuffer不同
                if(messages!=null){
                    for(Object m:messages){
                        sb.append(m);
                    }
                }
                if(t!=null){
                    sb.append("\n").append(Log.getStackTraceString(t));
                }
                message=sb.toString();
            }
            Log.println(level,tag,message);
        }

    }


}
