package com.ccqiuqiu.flist.todo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ccqiuqiu.flist.MainActivity;
import com.ccqiuqiu.flist.App;
import com.ccqiuqiu.flist.R;
import com.ccqiuqiu.flist.view.BaseFragment;

/**
 * Created by cc on 2015/11/22.
 */
public class TodoFragment extends BaseFragment {

    public TabLayout mTabLayout;
    public ViewPager mViewPager;

    public TodoFragment() {
        super();
    }
    //Fragment创建时执行
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).changeTipsView();
        //填充布局文件文件就一个RecyclerView
        return inflater.inflate(R.layout.fragment_todo_viewpager, container, false);
    }
    //Fragment所在的view创建完成后执行
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mTabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
        mViewPager = (ViewPager) getActivity().findViewById(R.id.pager);

        //设置ViewPager适配器
        mViewPager.setAdapter(new MyPagerAdapter((getActivity()).getSupportFragmentManager()));

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setSelectedTabIndicatorColor(App.colorAccent);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ((MainActivity) getActivity()).changeFab(position);

                ((MainActivity) getActivity()).changeTipsView();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

}
