package com.pcl.mvvm.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.aleyn.mvvm.base.BaseFragment
import com.aleyn.mvvm.extend.flowLaunch
import com.aleyn.mvvm.extend.launch
import com.pcl.mvvm.R
import com.pcl.mvvm.databinding.HomeFragmentBinding
import com.pcl.mvvm.network.entity.ArticlesBean
import com.pcl.mvvm.network.entity.BannerBean
import com.pcl.mvvm.ui.detail.DetailActivity
import com.pcl.mvvm.utils.GlideImageLoader
import com.youth.banner.Banner

/**
 *   @author : Aleyn
 *   time   : 2019/11/02
 */
class HomeFragment : BaseFragment<HomeFragmentBinding>() {

    /**
     * 推荐官方获取 ViewModel 方式
     */
    private val viewModel by viewModels<HomeViewModel>()

    private val mAdapter by lazy { HomeListAdapter() }
    private var page: Int = 0
    private lateinit var banner: Banner<BannerBean, GlideImageLoader>


    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun initView(savedInstanceState: Bundle?) {
        with(mBinding.rvHome) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
            //banner
            banner = Banner(context)
            banner.minimumWidth = MATCH_PARENT
            banner.layoutParams =
                ViewGroup.LayoutParams(MATCH_PARENT, resources.getDimension(R.dimen.dp_120).toInt())
            banner.setAdapter(GlideImageLoader())
        }
        mAdapter.apply {
            addHeaderView(banner)
            loadMoreModule.setOnLoadMoreListener(this@HomeFragment::loadMore)
            setOnItemClickListener { adapter, _, position ->
                val item = adapter.data[position] as ArticlesBean
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("url", item.link)
                startActivity(intent)
            }
        }
        mBinding.refreshHome.setOnRefreshListener {
            dropDownRefresh()
        }
        registerDefUIChange(viewModel)//绑定默认UI 事件
    }

    override fun initObserve() {
        launch {
            viewModel.refreshState
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect {
                    if (mBinding.refreshHome.isRefreshing) mBinding.refreshHome.finishRefresh()
                }
        }

        // 这种写法等同于上边 flowWithLifecycle 写法
        flowLaunch {
            viewModel.mBanners.collect {
                banner.setDatas(it)
            }
        }

        flowLaunch {
            viewModel.projectData.collect {
                if (it.curPage == 1) mAdapter.setList(it.datas)
                else mAdapter.addData(it.datas)
                if (it.curPage == it.pageCount) mAdapter.loadMoreModule.loadMoreEnd()
                else mAdapter.loadMoreModule.loadMoreComplete()
                page = it.curPage
            }
        }
    }

    override fun lazyLoadData() {
        viewModel.getBanner()
        viewModel.getHomeList(page)
    }

    /**
     * 下拉刷新
     */
    private fun dropDownRefresh() {
        page = 0
        viewModel.getHomeList(page, true)
        viewModel.getBanner(true)
    }

    /**
     * 加载更多
     */
    private fun loadMore() {
        viewModel.getHomeList(page + 1)
    }


    override fun onResume() {
        super.onResume()
    }

}
