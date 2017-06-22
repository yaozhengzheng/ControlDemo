/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package liubin.com.myapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.mylibrary.base.ApiResponse;
import com.example.mylibrary.base.BaseFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import liubin.com.myapplication.api.CustomerApi;
import liubin.com.myapplication.bean.Result;
import timber.log.Timber;

public class CheeseListFragment extends BaseFragment
    implements BaseQuickAdapter.RequestLoadMoreListener {

  @BindView(R.id.recyclerview) RecyclerView mRecyclerView;
  @BindView(R.id.swip) SwipeRefreshLayout mSwipeRefreshLayout;
  Unbinder mUnBinder;

  private StringAdapter mAdapter;
  private final List<Result> mData = new ArrayList<>();

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    onLoadMoreRequested();
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_cheese_list, container, false);
    mUnBinder = ButterKnife.bind(this, view);

    mSwipeRefreshLayout.setColorSchemeResources(//
        android.R.color.holo_blue_bright,//
        android.R.color.holo_green_light,//
        android.R.color.holo_orange_light,//
        android.R.color.holo_red_light);

    mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

      @Override public void onRefresh() {
        if (mAdapter.isLoading()) {
          mSwipeRefreshLayout.setRefreshing(false);
          return;
        }
        mAdapter.setEnableLoadMore(false);
        onLoadMoreRequested();
      }
    });

    setupRecyclerView(mRecyclerView);
    return view;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    mSwipeRefreshLayout.removeAllViews();
    mAdapter = null;
    mUnBinder.unbind();
  }

  private void setupRecyclerView(RecyclerView recyclerView) {
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    mAdapter = new StringAdapter(mData, this);
    recyclerView.setAdapter(mAdapter);
    mAdapter.setOnLoadMoreListener(this, mRecyclerView);
    mAdapter.setEmptyView(R.layout.default_progress_layout);
  }

  @Override public void onLoadMoreRequested() {
    CustomerApi.queryData(20)//
        .subscribeOn(Schedulers.io())// 指定之前的subscribe在io线程执行
        .doOnSubscribe(new Consumer<Disposable>() {
          @Override public void accept(Disposable disposable) throws Exception {
            mDisposables.add(disposable);
          }
        })//开始执行之前的准备工作
        .subscribeOn(AndroidSchedulers.mainThread())//指定 前面的doOnSubscribe 在主线程执行
        .observeOn(AndroidSchedulers.mainThread())//指定 后面的subscribe在io线程执行
        .subscribe(new Consumer<ApiResponse<List<Result>>>() {
          @Override public void accept(ApiResponse<List<Result>> data) throws Exception {
            boolean isRefresh = !mAdapter.isLoading();
            if (!mIsViewCreated) {
              List<Result> results = data.getData();
              if (results == null) results = new ArrayList<>();
              if (isRefresh) mData.clear();
              mData.addAll(results);
              return;
            }
            if (!data.isSuccess()) {//如果服务调用失败
              if (isRefresh) {//刷新失败
                mAdapter.setEnableLoadMore(true);
              } else {//加载更多失败
                mAdapter.loadMoreFail();
              }
              mSwipeRefreshLayout.setRefreshing(false);
              if (mData.size() == 0 || mAdapter.getData().size() == 0) {
                mAdapter.setEmptyView(R.layout.default_empty_layout);
              }
              Toast.makeText(getContext(), data.getMessage(), Toast.LENGTH_LONG).show();
              return;
            }

            // 更新列表数据
            List<Result> results = data.getData();
            if (results == null) results = new ArrayList<>();
            if (isRefresh) {
              mData.clear();
              mData.addAll(results);
              mAdapter.notifyDataSetChanged();
            } else {
              mAdapter.addData(results);
            }

            mSwipeRefreshLayout.setRefreshing(false);
            if (mAdapter.getData().size() == 0) {
              mAdapter.setEmptyView(R.layout.default_empty_layout);
            }

            if (results.size() == 20) {
              mAdapter.loadMoreComplete();
            } else {
              mAdapter.loadMoreEnd();
            }
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable throwable) throws Exception {
            boolean isRefresh = !mAdapter.isLoading();
            if (isRefresh) {
              mAdapter.setEnableLoadMore(true);
            } else {
              mAdapter.loadMoreFail();
            }
            if (mAdapter.getData().size() == 0) {
              mAdapter.setEmptyView(R.layout.default_network_error_layout);
            }
            if (isRefresh) {
              mSwipeRefreshLayout.setRefreshing(false);
            }
            Timber.e(throwable);
            Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
          }
        });
  }

  public static class StringAdapter extends BaseQuickAdapter<Result, BaseViewHolder> {
    private final Fragment mFragment;

    public StringAdapter(@Nullable List<Result> data, Fragment fragment) {
      super(R.layout.list_item, data);
      this.mFragment = fragment;
    }

    @Override protected void convert(BaseViewHolder helper, Result item) {
      helper.setText(android.R.id.text1, item.getName());
      helper.itemView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
        }
      });
      Glide.with(mFragment)
          .load(item.getIcon())
          .bitmapTransform(new CropCircleTransformation(mContext))
          .into((ImageView) helper.getView(R.id.avatar));
    }
  }
}
