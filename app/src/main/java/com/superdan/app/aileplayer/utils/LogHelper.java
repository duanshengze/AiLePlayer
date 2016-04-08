package com.superdan.app.aileplayer.utils;

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

}
