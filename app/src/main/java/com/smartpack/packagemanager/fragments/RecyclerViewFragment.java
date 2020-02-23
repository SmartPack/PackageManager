package com.smartpack.packagemanager.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.utils.ViewUtils;
import com.smartpack.packagemanager.views.recyclerview.RecyclerViewAdapter;
import com.smartpack.packagemanager.views.recyclerview.RecyclerViewItem;
import com.smartpack.packagemanager.viewpagerindicator.CirclePageIndicator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public abstract class RecyclerViewFragment extends BaseFragment {

    private Handler mHandler;
    private ScheduledThreadPoolExecutor mPoolExecutor;

    private List<RecyclerViewItem> mItems = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private Scroller mScroller;

    private View mProgress;

    private List<Fragment> mViewPagerFragments;
    private ViewPagerAdapter mViewPagerAdapter;
    private View mViewPagerParent;
    private ViewPager mViewPager;
    private CirclePageIndicator mCirclePageIndicator;

    private FloatingActionButton mBottomFab;

    private AsyncTask<Void, Void, List<RecyclerViewItem>> mLoader;

    private ValueAnimator mForegroundAnimator;
    private View mForegroundParent;
    private float mForegroundHeight;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        mHandler = new Handler();

        mRecyclerView = mRootView.findViewById(R.id.recyclerview);

        if (mViewPagerFragments != null) {
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            for (Fragment fragment : mViewPagerFragments) {
                fragmentTransaction.remove(fragment);
            }
            fragmentTransaction.commitAllowingStateLoss();
            mViewPagerFragments.clear();
        } else {
            mViewPagerFragments = new ArrayList<>();
        }
        mViewPagerParent = mRootView.findViewById(R.id.viewpagerparent);
        mViewPager = mRootView.findViewById(R.id.viewpager);
        mViewPager.setVisibility(View.INVISIBLE);
        mCirclePageIndicator = mRootView.findViewById(R.id.indicator);
        mViewPagerParent.setVisibility(View.INVISIBLE);
        ViewUtils.dismissDialog(getChildFragmentManager());

        mProgress = mRootView.findViewById(R.id.progress);

        mBottomFab = mRootView.findViewById(R.id.bottom_fab);

        mRecyclerView.clearOnScrollListeners();
        if (showViewPager()) {
            mScroller = new Scroller();
            mRecyclerView.addOnScrollListener(mScroller);
        }
        mRecyclerView.setAdapter(mRecyclerViewAdapter == null ? mRecyclerViewAdapter
                = new RecyclerViewAdapter(mItems, new RecyclerViewAdapter.OnViewChangedListener() {
            @Override
            public void viewChanged() {
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isAdded() && getActivity() != null) {
                            adjustScrollPosition();
                        }
                    }
                }, 250);
            }
        }) : mRecyclerViewAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager = getLayoutManager());
        mRecyclerView.setHasFixedSize(true);

        mBottomFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBottomFabClick();
            }
        });
        {
            Drawable drawable = getBottomFabDrawable();
            if (drawable != null) {
                mBottomFab.setImageDrawable(drawable);
            }
        }

        BaseFragment foregroundFragment = getForegroundFragment();
        if (foregroundFragment != null) {
            mForegroundParent = mRootView.findViewById(R.id.foreground_parent);
            TextView mForegroundText = mRootView.findViewById(R.id.foreground_text);
            mForegroundText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissForeground();
                }
            });
            getChildFragmentManager().beginTransaction().replace(R.id.foreground_content,
                    foregroundFragment).commit();
            mForegroundHeight = getResources().getDisplayMetrics().heightPixels;
        }

        if (itemsSize() == 0) {
            mLoader = new UILoader(this, savedInstanceState);
            mLoader.execute();
        } else {
            showProgress();
            init();
            hideProgress();
            postInit();
            adjustScrollPosition();

            mViewPager.setVisibility(View.VISIBLE);
        }

        return mRootView;
    }

    private static class UILoader extends AsyncTask<Void, Void, List<RecyclerViewItem>> {

        private WeakReference<RecyclerViewFragment> mRefFragment;
        private Bundle mSavedInstanceState;

        private UILoader(RecyclerViewFragment fragment, Bundle savedInstanceState) {
            mRefFragment = new WeakReference<>(fragment);
            mSavedInstanceState = savedInstanceState;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            RecyclerViewFragment fragment = mRefFragment.get();

            fragment.showProgress();
            fragment.init();
        }

        @Override
        protected List<RecyclerViewItem> doInBackground(Void... params) {
            RecyclerViewFragment fragment = mRefFragment.get();

            if (fragment.isAdded() && fragment.getActivity() != null) {
                List<RecyclerViewItem> items = new ArrayList<>();
                fragment.addItems(items);
                return items;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecyclerViewItem> recyclerViewItems) {
            super.onPostExecute(recyclerViewItems);
            if (isCancelled() || recyclerViewItems == null) return;

            final RecyclerViewFragment fragment = mRefFragment.get();

            for (RecyclerViewItem item : recyclerViewItems) {
                fragment.addItem(item);
            }
            fragment.hideProgress();
            fragment.postInit();
            if (mSavedInstanceState == null) {
                fragment.mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        Activity activity = fragment.getActivity();
                        if (fragment.isAdded() && activity != null) {
                            fragment.mRecyclerView.startAnimation(AnimationUtils.loadAnimation(
                                    activity, R.anim.slide_in_bottom));

                            int cx = fragment.mViewPager.getWidth();

                            SupportAnimator animator = ViewAnimationUtils.createCircularReveal(
                                    fragment.mViewPager, cx / 2, 0, 0, cx);
                            animator.addListener(new SupportAnimator.SimpleAnimatorListener() {
                                @Override
                                public void onAnimationStart() {
                                    super.onAnimationStart();
                                    fragment.mViewPager.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAnimationEnd() {
                                    super.onAnimationEnd();
                                }
                            });
                            animator.setDuration(400);
                            animator.start();
                        }
                    }
                });
            } else {
                fragment.mViewPager.setVisibility(View.VISIBLE);
            }
            fragment.mLoader = null;
        }
    }

    @Override
    public void onViewFinished() {
        super.onViewFinished();
        if (showViewPager()) {
            mViewPager.setAdapter(mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(),
                    mViewPagerFragments));
            mCirclePageIndicator.setViewPager(mViewPager);

            adjustScrollPosition();
        } else {
            mRecyclerView.setClipToPadding(true);
            ViewGroup.LayoutParams layoutParams = mViewPagerParent.getLayoutParams();
            layoutParams.height = 0;
            mViewPagerParent.requestLayout();
        }
    }

    protected void init() {
    }

    protected void postInit() {
        if (getActivity() != null && isAdded()) {
            for (RecyclerViewItem item : mItems) {
                item.onRecyclerViewCreate(getActivity());
            }
        }
    }

    protected void adjustScrollPosition() {
        if (mScroller != null) {
            mScroller.onScrolled(mRecyclerView, 0, 0);
        }
    }

    protected abstract void addItems(List<RecyclerViewItem> items);

    protected void addItem(RecyclerViewItem recyclerViewItem) {
        mItems.add(recyclerViewItem);
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.notifyItemInserted(mItems.size() - 1);
        }
        if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            ((StaggeredGridLayoutManager) mLayoutManager).setSpanCount(getSpanCount());
        }
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        return new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
    }

    protected void clearItems() {
        mItems.clear();
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.notifyDataSetChanged();
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager = getLayoutManager());
            adjustScrollPosition();
        }
    }

    public int getSpanCount() {
        Activity activity;
        if ((activity = getActivity()) != null) {
            int span = Utils.isTablet(activity) ? Utils.getOrientation(activity) ==
                    Configuration.ORIENTATION_LANDSCAPE ? 3 : 2 : Utils.getOrientation(activity) ==
                    Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
            if (itemsSize() != 0 && span > itemsSize()) {
                span = itemsSize();
            }
            return span;
        }
        return 1;
    }

    public int itemsSize() {
        return mItems.size();
    }

    protected void addViewPagerFragment(BaseFragment fragment) {
        mViewPagerFragments.add(fragment);
        if (mViewPagerAdapter != null) {
            mViewPagerAdapter.notifyDataSetChanged();
        }
    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragments;

        public ViewPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
            super(fragmentManager);
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments == null ? 0 : mFragments.size();
        }
    }

    private class Scroller extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            View firstItem = mRecyclerView.getChildAt(0);
            if (firstItem == null) {
                if (mRecyclerViewAdapter != null) {
                    firstItem = mRecyclerViewAdapter.getFirstItem();
                }
                if (firstItem == null) {
                    return;
                }
            }

            int mScrollDistance = -firstItem.getTop() + mRecyclerView.getPaddingTop();

            mViewPagerParent.setTranslationY(-mScrollDistance);
            if (showBottomFab() && autoHideBottomFab()) {
                if (dy <= 0) {
                    if (mBottomFab.getVisibility() != View.VISIBLE) {
                        mBottomFab.show();
                    }
                } else if (mBottomFab.getVisibility() == View.VISIBLE) {
                    mBottomFab.hide();
                }
            }
        }
    }

    protected void showProgress() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAdded()) {
                        mProgress.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.INVISIBLE);
                        if (mBottomFab != null && showBottomFab()) {
                            mBottomFab.hide();
                        }
                    }
                }
            });
        }
    }

    protected void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mViewPagerParent.setVisibility(View.VISIBLE);
        if (mBottomFab != null && showBottomFab()) {
            mBottomFab.show();
        }
        adjustScrollPosition();
    }

    private BaseFragment getForegroundFragment() {
        return null;
    }

    private void dismissForeground() {
        float translation = mForegroundParent.getTranslationY();
        mForegroundAnimator = ValueAnimator.ofFloat(translation, mForegroundHeight);
        mForegroundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mForegroundParent.setTranslationY((float) animation.getAnimatedValue());
            }
        });
        mForegroundAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mForegroundParent.setVisibility(View.GONE);
                mForegroundAnimator = null;
            }
        });
        mForegroundAnimator.start();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (showViewPager()) {
            menu.add(0, 0, Menu.NONE, R.string.options)
                    .setIcon(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_launcher_preview))
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        if (showBottomFab()) {
            menu.add(0, 1, Menu.NONE, R.string.more)
                    .setIcon(getBottomFabDrawable())
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (showBottomFab()) {
            onBottomFabClick();
        }
        return false;
    }

    private boolean showViewPager() {
        return true;
    }

    protected boolean showBottomFab() {
        return false;
    }

    protected Drawable getBottomFabDrawable() {
        return null;
    }

    protected void onBottomFabClick() {
    }

    protected boolean autoHideBottomFab() {
        return true;
    }

    protected FloatingActionButton getBottomFab() {
        return mBottomFab;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPoolExecutor == null) {
            mPoolExecutor = new ScheduledThreadPoolExecutor(1);
        }
        for (RecyclerViewItem item : mItems) {
            item.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPoolExecutor != null) {
            mPoolExecutor.shutdown();
            mPoolExecutor = null;
        }
        for (RecyclerViewItem item : mItems) {
            item.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mItems.clear();
        mRecyclerViewAdapter = null;
        if (mLoader != null) {
            mLoader.cancel(true);
            mLoader = null;
        }
        for (RecyclerViewItem item : mItems) {
            item.onDestroy();
        }
    }

    protected Handler getHandler() {
        return mHandler;
    }

}
