package com.superdan.app.aileplayer.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;

/**
 * Created by Administrator on 2016/4/9.
 * Generic reusable methods to handle resources.
 * 可以广泛重用的方法，用来处理资源
 */
public class ResourceHelper {
        /**
         * Get a color value from a theme attribute.
         *从主题属性获取颜色值
         * @param context used for getting the color.
         * @param attribute theme attribute.
         * @param defaultColor default to use.
         * @return color value
         */
    public static  int getThemeColor(Context context,int attribute,int defaultColor){
        int themeColor=0;
        String packageName=context.getPackageName();

        try {
            Context packageContext=context.createPackageContext(packageName,0);

            ApplicationInfo applicationInfo=context.getPackageManager().getApplicationInfo(packageName,0);
            packageContext.setTheme(applicationInfo.theme);
            Resources.Theme theme=packageContext.getTheme();
            TypedArray ta=theme.obtainStyledAttributes(new int[]{attribute});
            themeColor=ta.getColor(0,defaultColor);
            ta.recycle();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return themeColor;


    }


}
