package com.superdan.app.aileplayer;

/**
 * Created by Administrator on 2016/4/25.
 */

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Process;
import android.util.Base64;
import android.widget.ArrayAdapter;

import com.superdan.app.aileplayer.utils.LogHelper;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Validates that the calling package is authorized to browse a
 * {@link android.service.media.MediaBrowserService}.
 *
 * The list of allowed signing certificates and their corresponding package names is defined in
 * res/xml/allowed_media_browser_callers.xml.
 *
 * If you add a new valid caller to allowed_media_browser_callers.xml and you don't know
 * its signature, this class will print to logcat (INFO level) a message with the proper base64
 * version of the caller certificate that has not been validated. You can copy from logcat and
 * paste into allowed_media_browser_callers.xml. Spaces and newlines are ignored.
 */
public class PackageValidator {
    private static final String TAG= LogHelper.makeLogTag(PackageValidator.class);

    /**
     * Map allowed callers' certificate keys to the expected caller information.
     *
     */
    private final Map<String,ArrayList<CallerInfo>> mValidCertificates;

    public PackageValidator(Context ctx){
        mValidCertificates=readValidCertificates(ctx.getResources().getXml(R.xml.allowed_media_browser_callers));
    }

    private  Map<String,ArrayList<CallerInfo>> readValidCertificates(XmlResourceParser parser){
        HashMap<String,ArrayList<CallerInfo>>validCertificates=new HashMap<>();

        try{
            int eventType=parser.next();
            while (eventType!=XmlResourceParser.END_DOCUMENT){
                if(eventType==XmlResourceParser.START_DOCUMENT&&parser.getName().equals("signing_certificate")){

                    String name=parser.getAttributeValue(null,"name");
                    String packageName=parser.getAttributeValue(null,"package");
                    boolean isRelease=parser.getAttributeBooleanValue(null,"realse",false);
                    String certificate=parser.nextText().replaceAll("\\s|\\n","");
                    CallerInfo info=new CallerInfo(name,packageName,isRelease);
                    ArrayList<CallerInfo>infos=validCertificates.get(certificate);
                    if(info==null){
                        infos=new ArrayList<>();
                        validCertificates.put(certificate,infos);
                    }

                    LogHelper.v(TAG,"Adding allowed caller:",info.name,"package=",info.packageName,
                            " realse=",info.release,"certificate=",certificate);

                    infos.add(info);



                }//end if
                eventType=parser.next();

            }



        }catch (XmlPullParserException|IOException e){
            LogHelper.e(TAG,e,"could not read allowed callowed callers from xml");
        }
        return validCertificates;

    }



    public boolean isCallerAllowed(Context context,String callingPackage,int callingUid){
        // Always allow calls from the framework, self app or development environment.
        if (Process.SYSTEM_UID==callingUid||Process.myPid()==callingUid){
            return  true;
        }

        PackageManager packageManager=context.getPackageManager();
        PackageInfo packageInfo;
        try{
            packageInfo=packageManager.getPackageInfo(callingPackage,PackageManager.GET_SIGNATURES);

        }catch (PackageManager.NameNotFoundException e){
            LogHelper.w(TAG ,e,"package manager can't find package: ",callingPackage);
            return  false;
        }
        if (packageInfo.signatures.length!=1){
            LogHelper.w(TAG,"Caller has more than one signature certificate!");
            return  false;
        }
        String signature = Base64.encodeToString(packageInfo.signatures[0].toByteArray(),Base64.NO_WRAP);
        //Test for known signatures:
        ArrayList<CallerInfo>validCallers=mValidCertificates.get(signature);
        if(validCallers==null){
            LogHelper.v(TAG,"Signature for caller",callingPackage,"is not valid \n",signature);
            if(mValidCertificates.isEmpty()){

                LogHelper.w(TAG,"The list of valid certificates is empty,Either your file",
                        "res/xml/allow_media_browser_callers.xml is empty or there was an error",
                        "while reading it,Check previous log messages.");
            }
            return false;
        }

        //Check if the package name is valid for the certificate

        StringBuffer expectedPackages=new StringBuffer();
        for (CallerInfo info:validCallers){
            if(callingPackage.equals(info.packageName)){
                LogHelper.v(TAG,"Valid caller:",info.name," package= ",info.packageName,"release= ",info.release);
                return  true;
            }
            expectedPackages.append(info.packageName).append(' ');
        }

        LogHelper.i(TAG,"Caller has a valid certificate,but its package doesn't match any ",
               "expected package for the given certificate,Caller's package is",callingPackage,".Expected packages has defined in " +
                        "res/xml/allowed_media_browser_callers.xml are (",expectedPackages,"). the caller's certificate is:\n"
        ,signature);
        return  false;

    }





    private final static class CallerInfo{
        final String name;
        final String packageName;
        final  boolean release;
        public CallerInfo(String name,String packageName,boolean release){

            this.name=name;
            this.packageName=packageName;
            this.release=release;
        }


    }
}
