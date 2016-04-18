package com.superdan.app.aileplayer.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import android.support.design.widget.NavigationView;

import android.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.widgets.IntroductoryOverlay;
import com.superdan.app.aileplayer.R;
import com.superdan.app.aileplayer.utils.LogHelper;

/**
 * Created by Administrator on 2016/4/8.
 * 抽象类并带有工作栏，导航抽屉和投射支持的Activity,
 * 需要被任何想要作为顶层活动展示的活动继承，
 * 要求子类调用{@link #initializeToolbar()}在onCreate中setContentView() 调用之后,
 * 有三个强制性布局元素：
 * {@link android.support.v7.widget.Toolbar}带有 id的toolbar
 * {@link android.support.v4.widget.DrawerLayout}带有id的drawerLayout
 * {@link android.widget.ListView}带有id的drawerList
 */
public abstract class ActionBarCastActivity extends AppCompatActivity {
    private static final String TAG = LogHelper.makeLogTag(ActionBarCastActivity.class);
    //延迟时间
    private static final int DELAY_MILLIS = 1000;

    private VideoCastManager mCastManager;

    private MenuItem mMediaRouteMenuItem;

    protected Toolbar mToolbar;

    /**
     * This class provides a handy way to tie together the functionality of
     * {@link android.support.v4.widget.DrawerLayout} and the framework <code>ActionBar</code> to
     * implement the recommended design for navigation drawers.
     * 此类提供一个便捷的方式去联系DrawLayout的功能和ActionBar框架去实现导航抽屉推荐的设计
     */

    protected ActionBarDrawerToggle mDrawerToggle;
    protected DrawerLayout mDrawerLayout;

    private boolean mToolbarInitilized;

    private int mItemToOpenWhenDrawerCloses = -1;

    //TODO???
    private final VideoCastConsumerImpl mCastConsumer = new VideoCastConsumerImpl() {
        @Override
        public void onFailed(int resourceId, int statusCode) {
            LogHelper.d(TAG,"onFiled",resourceId," status ",statusCode);
        }

        @Override
        public void onConnectionSuspended(int cause) {
            LogHelper.d(TAG,"onConnectionSuspended() was called with cause:",cause);
        }

        @Override
        public void onConnectivityRecovered() {

        }

        @Override
        public void onCastAvailabilityChanged(boolean castPresent) {
                if(castPresent){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(mMediaRouteMenuItem.isVisible()){
                                LogHelper.d(TAG,"Cast Icon is visible");
                                showFtu();
                            }
                        }
                    },DELAY_MILLIS);
                }
        }
    };


    //Listener for monitoring events about drawers
    //监听有关抽屉时间的监听器
    private final DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {


        /**
         *Called when a drawer's position changes.
         * 当一个抽屉的位置改变 时调用
         */
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            if (mDrawerToggle != null) mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            if (mDrawerToggle != null) mDrawerToggle.onDrawerOpened(drawerView);
            /**
             * Set the action bar's title. This will only be displayed if
             * {@link #DISPLAY_SHOW_TITLE} is set
             * 设置操作栏的标题
             */

            if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.app_name);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            /**
             * callback method. If you do not use your
             * ActionBarDrawerToggle instance directly as your DrawerLayout's listener, you should call
             * through to this method from your own listener object.
             * 回调方法。如果你不直接使用您的ActionBarDrawerToggle 实例作为DrawerLayout监听器，应该
             * 从你自己的监听器中调用此方法
             */
            if (mDrawerToggle != null) mDrawerToggle.onDrawerClosed(drawerView);
            //显示所选择要开启的activity
            if (mItemToOpenWhenDrawerCloses >= 0) {

                /**
                 * Create an ActivityOptions specifying a custom animation to run when
                 * the activity is displayed.
                 * 创建一个ActivityOptions指定自定义动画当该activity被显示时才会运行动画
                 * @param context Who is defining this.  This is the application that the
                 * animation resources will be loaded from.
                 *是哪个context定义的，动画资源将被加载出
                 * @param enterResId A resource ID of the animation resource to use for
                 * the incoming activity.  Use 0 for no animation.
                 *                   动画资源 进入activity的动画 使用0表示没有动画
                 * @param exitResId A resource ID of the animation resource to use for
                 * the outgoing activity.  Use 0 for no animation.
                 *                  动画资源，离开activity的资源，使用0表示无资源
                 * @return Returns a new ActivityOptions object that you can use to
                 * supply these options as the options Bundle when starting an activity.
                 * 返回一个新的ActivityOptions对象，可以使用它来在开始活动时提供这些选项
                 */
                Bundle extras = ActivityOptions.makeCustomAnimation(
                        ActionBarCastActivity.this, R.anim.fade_in, R.anim.fade_out
                ).toBundle();
                Class activityClass = null;
                switch (mItemToOpenWhenDrawerCloses) {
                    case R.id.navigayion_allmusic:
                        activityClass = MusicPlayerActivity.class;
                        break;
                    case R.id.navigation_playlists:
                        activityClass = PlaceholderActivity.class;
                        break;

                }
                if (activityClass != null) {
                    startActivity(new Intent(ActionBarCastActivity.this, activityClass), extras);
                    finish();
                }

            }
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            if (mDrawerToggle != null) mDrawerToggle.onDrawerStateChanged(newState);

        }

    };
    //监听返回栈的改变
    private final FragmentManager.OnBackStackChangedListener mBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            updateDrawerToggle();
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogHelper.d(TAG, "Activity onCreate");

        //Ensure that Google Play Service is available.
        //确信谷歌Paly服务是否可用。
        VideoCastManager.checkGooglePlayServices(this);
        mCastManager = VideoCastManager.getInstance();

        /**
         * This method tries to automatically re-establish re-establish connection to a session if
         * 此方法尝试自动重新建立连接会话，如果用户没有在最近做手动断开，该用户所连接的设备一直运行在相同的会话条件下
         * 尽最大的努力继续尝试使用相同的会话。
         * <ul>
         * <li>User had not done a manual disconnect in the last session
         * <li>Device that user had connected to previously is still running the same session
         * </ul>
         * Under these conditions, a best-effort attempt will be made to continue with the same
         * session. This attempt will go on for {@code SESSION_RECOVERY_TIMEOUT} seconds.
         */
        mCastManager.reconnectSessionIfPossible();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mToolbarInitilized) {
            throw new IllegalStateException("你必须调用super.initializeToobar 在完成你onCreate（）后");
        }
    }

    /**
     * Called when activity start-up is complete (after onStart and onRestoreInstanceState have been called).
     * Applications will generally not implement this method; it is intended for system classes to do final
     * initialization after application code has run.
     * Derived classes must call through to the super class's implementation of this method.
     * If they do not, an exception will be thrown.
     * <p/>
     * 当调用activity启动完成（onStart and onRestoreInstanceState已经调用之后）。应用程序一般不会实现此方法
     * 它是用于应用程序代码运行后系统类做的最后初始化。
     * 派生类必须通过调用 super类实现的此方法
     * 如果不这么做将有异常抛出
     */

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
        /**
         * Synchronize the state of the drawer indicator/affordance with the linked DrawerLayout.
         This should be called from your Activity's onPostCreate method to synchronize after
         the DrawerLayout's instance state has been restored
         同步与DrawerLayout连接的抽屉指示器的状态
         这应该被来自Activity的onPostCreate方法调用去同步当DrawerLayout的实例状态已经被恢复之后
         */

            mDrawerToggle.syncState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCastManager.addVideoCastConsumer(mCastConsumer);
        mCastManager.incrementUiCounter();
        // Whenever the fragment back stack changes, we may need to update the
        // action bar toggle: only top level screens show the hamburger-like icon, inner
        // screens - either Activities or fragments - show the "Up" icon instead
        getFragmentManager().addOnBackStackChangedListener(mBackStackChangedListener);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(mDrawerToggle!=null){
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        mCastManager.decrementUiCounter();
        getFragmentManager().removeOnBackStackChangedListener(mBackStackChangedListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main,menu);
        mMediaRouteMenuItem=mCastManager.addMediaRouterButton(menu,R.id.media_route_menu_item);
        return  true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle!=null&&mDrawerToggle.onOptionsItemSelected(item)){
            return  true;
        }
        // If not handled by drawerToggle, home needs to be handled by returning to previous
        if(item!=null&&item.getItemId()==android.R.id.home){
            onBackPressed();
            return  true;
        }
        return  super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout!=null&&mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawers();
            return;
        }
        //否则将返回一个之前的Fragment堆
        FragmentManager fragmentManager=getFragmentManager();
        if(fragmentManager.getBackStackEntryCount()>0){
            fragmentManager.popBackStack();
        }else {
            //这将依赖系统的表现
            super.onBackPressed();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mToolbar.setTitle(title);
    }


    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        mToolbar.setTitle(titleId);
    }

    protected void initializeToolbar(){

        mToolbar=(Toolbar)findViewById(R.id.toobar);
        if (mToolbar==null){
             throw  new IllegalStateException("Layout is required to include a Toolbar with id " +
                     "'toolbar'");
        }
        mToolbar.inflateMenu(R.menu.main);
        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        if(mDrawerLayout!=null){
            NavigationView navigationView=(NavigationView)findViewById(R.id.nav_view);
            if(navigationView!=null){
                throw new IllegalStateException("Layout requires a NavigationView " +
                        "with id 'nav_view'");
            }
            mDrawerToggle=new ActionBarDrawerToggle(this,mDrawerLayout,mToolbar
            ,R.string.open_content_drawer,R.string.close_content_drawer
            );
            mDrawerLayout.setDrawerListener(mDrawerListener);
            populateDrawerItems(navigationView);
            setSupportActionBar(mToolbar);
            updateDrawerToggle();
        }else {
            setSupportActionBar(mToolbar);
        }
        mToolbarInitilized=true;
    }


    //?TODO
    private void populateDrawerItems(NavigationView navigationView){


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                item.setChecked(true);
                mItemToOpenWhenDrawerCloses=item.getItemId();
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        if(MusicPlayerActivity.class.isAssignableFrom(getClass())){
            navigationView.setCheckedItem(R.id.navigayion_allmusic);
        }else if ((PlaceholderActivity.class.isAssignableFrom(getClass()))){
            navigationView.setCheckedItem(R.id.navigation_playlists);
        }


    }


    // TODO: 16/4/17 ?
    protected  void updateDrawerToggle(){
        if (mDrawerToggle==null)
            return;
        boolean isRoot=getFragmentManager().getBackStackEntryCount()==0;
        mDrawerToggle.setDrawerIndicatorEnabled(isRoot);
        if(getSupportActionBar()!=null){

            getSupportActionBar().setDisplayHomeAsUpEnabled(!isRoot);
            getSupportActionBar().setDisplayShowHomeEnabled(!isRoot);
            getSupportActionBar().setHomeButtonEnabled(!isRoot);
        }
        if(isRoot){
            mDrawerToggle.syncState();
        }



    }


    /**
     *Shows the Cast First Time User experience to the user (an overlay that explains what is
     * the Cast icon)
     * 显示投射 首次用户体验给用户（解释什么是投射图标）
     */
    private void showFtu(){
        Menu menu=mToolbar.getMenu();
        View view=menu.findItem(R.id.media_route_menu_item).getActionView();
        if(view!=null&&view instanceof MediaRouteButton){
            IntroductoryOverlay overlay=new IntroductoryOverlay.Builder(this)
                    .setMenuItem(mMediaRouteMenuItem)
                    .setTitleText(R.string.touch_to_cast)
                    .setSingleTime()
                    .build();
            overlay.show();


        }


    }





}
