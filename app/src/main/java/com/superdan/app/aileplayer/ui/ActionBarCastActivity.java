package com.superdan.app.aileplayer.ui;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.superdan.app.aileplayer.utils.LogHelper;

/**
 * Created by Administrator on 2016/4/8.
 * 抽象类并带有工作栏，导航抽屉和投射支持的Activity,
 * 需要被任何想要作为顶层活动展示的活动继承，
 *要求子类调用{@link #initializeToolbar()}在onCreate中setContentView() 调用之后,
 * 有三个强制性布局元素：
 * {@link android.support.v7.widget.Toolbar}带有 id的toolbar
 * {@link android.support.v4.widget.DrawerLayout}带有id的drawerLayout
 *{@link android.widget.ListView}带有id的drawerList
 */
public abstract class ActionBarCastActivity extends AppCompatActivity {
        private  static final String TAG= LogHelper.makeLogTag(ActionBarCastActivity.class);
        //延迟时间
        private static final int DELAY_MILLS=1000;

        private VideoCastManager mCastManager;

        private MenuItem mMediaRouteMenuItem;

        private Toolbar mToolbar;

        private ActionBarDrawerToggle mDrawerToggle;
        private DrawerLayout mDrawerLayout;

        private boolean mToolbarInitilized;

        private int mItemToOpenWhenDrawerCloses=-1;

        //TODO???
        private final VideoCastConsumerImpl mCastConsumer=new VideoCastConsumerImpl(){
                @Override
                public void onFailed(int resourceId, int statusCode) {
                        super.onFailed(resourceId, statusCode);
                }
        };
}
