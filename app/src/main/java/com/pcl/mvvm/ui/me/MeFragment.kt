package com.pcl.mvvm.ui.me

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.aleyn.mvvm.base.BaseVMFragment
import com.aleyn.mvvm.extend.flowLaunch
import com.pcl.mvvm.databinding.MeFragmentBinding
import com.pcl.mvvm.ui.detail.DetailActivity

class MeFragment : BaseVMFragment<MeViewModel, MeFragmentBinding>() {

    private val mAdapter by lazy { MeWebAdapter() }

    companion object {
        fun newInstance() = MeFragment()
    }

    override fun initView(savedInstanceState: Bundle?) {
        with(mBinding.rvMeUesdWeb) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        mAdapter.setOnItemClickListener { _, _, position ->
            val intent = Intent().apply {
                setClass(requireContext(), DetailActivity::class.java)
                putExtra("url", (mAdapter.data[position]).link)
            }
            startActivity(intent)
        }


    }

    override fun initObserve() {
        flowLaunch {
            viewModel.popularWeb.collect {
                mAdapter.setList(it)
            }
        }
    }


    override fun lazyLoadData() {
        viewModel.getPopularWeb()
    }
}
