package com.stratagile.qlink.ui.activity.topup

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.pawegio.kandroid.runDelayedOnUiThread
import com.pawegio.kandroid.runOnUiThread
import com.pawegio.kandroid.toast
import com.socks.library.KLog
import com.stratagile.qlink.BuildConfig
import com.stratagile.qlink.R
import com.stratagile.qlink.application.AppConfig
import com.stratagile.qlink.base.BaseFragment
import com.stratagile.qlink.constant.ConstantValue
import com.stratagile.qlink.entity.*
import com.stratagile.qlink.entity.eventbus.Logout
import com.stratagile.qlink.entity.eventbus.ShowMiningAct
import com.stratagile.qlink.entity.reward.Dict
import com.stratagile.qlink.entity.topup.CountryList
import com.stratagile.qlink.entity.topup.PayToken
import com.stratagile.qlink.entity.topup.TopupGroupKindList
import com.stratagile.qlink.entity.topup.TopupProduct
import com.stratagile.qlink.ui.activity.finance.InviteNowActivity
import com.stratagile.qlink.ui.activity.main.MainViewModel
import com.stratagile.qlink.ui.activity.main.WebViewActivity
import com.stratagile.qlink.ui.activity.my.AccountActivity
import com.stratagile.qlink.ui.activity.my.EpidemicInviteNowActivity
import com.stratagile.qlink.ui.activity.my.EpidemicWebViewActivity
import com.stratagile.qlink.ui.activity.place.PlaceListActivity
import com.stratagile.qlink.ui.activity.place.PlaceVisitActivity
import com.stratagile.qlink.ui.activity.recommend.TopupProductDetailActivity
import com.stratagile.qlink.ui.activity.topup.component.DaggerTopUpComponent
import com.stratagile.qlink.ui.activity.topup.contract.TopUpContract
import com.stratagile.qlink.ui.activity.topup.module.TopUpModule
import com.stratagile.qlink.ui.activity.topup.presenter.TopUpPresenter
import com.stratagile.qlink.ui.adapter.finance.InvitedAdapter
import com.stratagile.qlink.ui.adapter.topup.CountryListAdapter
import com.stratagile.qlink.ui.adapter.topup.ImagesPagerAdapter
import com.stratagile.qlink.ui.adapter.topup.TopupShowProductAdapter
import com.stratagile.qlink.utils.*
import com.stratagile.qlink.view.ScaleCircleNavigator
import kotlinx.android.synthetic.main.fragment_topup.*
import net.lucode.hackware.magicindicator.FragmentContainerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import javax.inject.Inject

/**
 * @author hzp
 * @Package com.stratagile.qlink.ui.activity.topup
 * @Description: $description
 * @date 2019/09/23 15:54:17
 */

class TopUpFragment : BaseFragment(), TopUpContract.View {
    override fun setQlcPrice(tokenPrice: TokenPrice) {
    }

    override fun setOneFriendReward(dict: Dict) {
        queryProxyActivity()
        oneFirendClaimQgas = dict.data.value.toFloat()
    }

    lateinit var mIndexInterface: IndexInterface
    override fun setIndexInterface(indexInterface: IndexInterface) {
        var indexInterfaceStr = GsonUtils.objToJson(indexInterface)
        KLog.i(indexInterfaceStr)
        FileUtil.savaData("/Qwallet/indexInterfaceStr.txt", indexInterfaceStr)
        this.mIndexInterface = indexInterface
        viewModel!!.indexInterfaceMutableLiveData.value = indexInterface
        this.oneFirendClaimQgas = mIndexInterface.dictList.winq_invite_reward_amount.toFloat()
        //处理轮播图
        handlerBanner()
        //设置topup支持的国家
        setCountry()
        selectPayToken = mIndexInterface.payTokenList[0]
        if ("".equals(mIndexInterface.payTokenList[0].logo_png)) {
            Glide.with(activity!!)
                    .load(resources.getIdentifier(mIndexInterface.payTokenList[0].symbol.toLowerCase(), "mipmap", activity!!.packageName))
                    .apply(AppConfig.instance.optionsNormal)
                    .into(ivDeduction)
        } else {
            Glide.with(activity!!)
                    .load(mIndexInterface.payTokenList[0].logo_png)
                    .apply(AppConfig.instance.optionsNormal)
                    .into(ivDeduction)
        }


        topupShowProductAdapter.currentTime = mIndexInterface.currentTimeMillis
        topupShowProductAdapter.dictListBean = mIndexInterface.dictList
        var mustDiscount = 1.toDouble()
        mIndexInterface.groupKindList.forEach {
            if (mustDiscount > it.discount) {
                mustDiscount = it.discount
            }
        }
        mIndexInterface.groupKindList.forEach {
            if (mustDiscount == it.discount) {
                topupShowProductAdapter.mustGroupKind = it
            }
        }
        topupShowProductAdapter.payToken = mIndexInterface.payTokenList[0]

        var map = mutableMapOf<String, String>()
        map.put("page", "1")
        map.put("size", "20")
        map.put("globalRoaming", if (this::selectedCountry.isInitialized) {
            selectedCountry.globalRoaming
        } else {
            ""
        })
        map.put("deductionTokenId", if (this::selectPayToken.isInitialized) {
            selectPayToken.id
        } else {
            ""
        })
        mPresenter.getProductList(map, true, true)
        if (mIndexInterface.dictList.show19.equals("1")) {
            rlXingcheng.visibility = View.VISIBLE
            tv_title.visibility = View.INVISIBLE
        } else {
            rlXingcheng.visibility = View.GONE
            tv_title.visibility = View.VISIBLE
        }
    }

    fun handlerBanner() {
        isStop = true
        viewList.clear()
        //分享
        viewList.add(R.layout.layout_finance_share)

        //赚取qgas
        if (mIndexInterface.tradeMiningList.size > 0) {
            ConstantValue.miningQLC = mIndexInterface.tradeMiningList[0].totalRewardAmount.toBigDecimal().stripTrailingZeros().toPlainString() + " QLC!"
            viewList.add(R.layout.layout_finance_earn_rank)
        }

        //回购
        if (TimeUtil.timeStamp(mIndexInterface.dictList.burnQgasVoteStartDate) < mIndexInterface.currentTimeMillis && (TimeUtil.timeStamp(mIndexInterface.dictList.burnQgasVoteEndDate) > mIndexInterface.currentTimeMillis)) {
            viewList.add(R.layout.layout_banner_buyback)
        }

        //代理
        if (TimeUtil.timeStamp(mIndexInterface.dictList.topupGroupStartDate) < mIndexInterface.currentTimeMillis && (TimeUtil.timeStamp(mIndexInterface.dictList.topopGroupEndDate) > mIndexInterface.currentTimeMillis)) {
            viewList.add(R.layout.layout_banner_proxy_youxiang)
        }

        val viewAdapter = ImagesPagerAdapter(viewList, viewPager, activity!!)
        viewPager.adapter = viewAdapter
        val scaleCircleNavigator = ScaleCircleNavigator(activity)
        scaleCircleNavigator.setCircleCount(viewList.size)
        scaleCircleNavigator.setNormalCircleColor(Color.LTGRAY)
        scaleCircleNavigator.setSelectedCircleColor(activity!!.resources.getColor(R.color.mainColor))
        scaleCircleNavigator.setCircleClickListener { index -> viewPager.currentItem = index }
        indicator.navigator = scaleCircleNavigator
        ViewPagerHelper.bind(indicator, viewPager, viewList.size)

        if (viewList.size > 1) {
            autoPlayView()
        }
    }

    fun setCountry() {
        var countryListBean = IndexInterface.CountryListBean()
        countryListBean.name = "全部"
        countryListBean.nameEn = "All"
        countryListBean.globalRoaming = ""
        countryListBean.imgPath = ""
        selectedCountry = countryListBean
        mIndexInterface.countryList.add(0, countryListBean)
        val commonNavigator = CommonNavigator(activity!!)
        commonNavigator.isAdjustMode = true
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return mIndexInterface.countryList.size
            }

            override fun getTitleView(context: Context, i: Int): IPagerTitleView {
                val simplePagerTitleView = SimplePagerTitleView(context)
                var isCn = true
                isCn = SpUtil.getInt(activity!!, ConstantValue.Language, -1) == 1
                if (isCn) {
                    simplePagerTitleView.setText(mIndexInterface.countryList.get(i).name)
                } else {
                    simplePagerTitleView.setText(mIndexInterface.countryList.get(i).nameEn)
                }
//                simplePagerTitleView.isSingleLine = false
                simplePagerTitleView.normalColor = resources.getColor(R.color.color_505050)
                simplePagerTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                simplePagerTitleView.selectedColor = resources.getColor(R.color.color_F50B6E)
                simplePagerTitleView.setOnClickListener {
                    mFragmentContainerHelper.handlePageSelected(i)
                    selectedCountry = mIndexInterface.countryList[i]
                    reChangeTaoCan(mIndexInterface.countryList[i])
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_WRAP_CONTENT
                indicator.lineHeight = resources.getDimension(R.dimen.x3)
                indicator.setColors(resources.getColor(R.color.transparent))
                return indicator
            }
        }
        indicatorPlan.navigator = commonNavigator
        commonNavigator.titleContainer.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        mFragmentContainerHelper.attachMagicIndicator(indicatorPlan)
    }


    lateinit var proxyAcitivtyDict: Dict
    override fun setProxyActivityBanner(dict: Dict) {
    }

    override fun setInviteRank(inviteList: InviteList) {
//        mPresenter.getCountryList(hashMapOf())
        val invitedAdapter = InvitedAdapter(inviteList.top5, oneFirendClaimQgas)
        recyclerViewInvite.adapter = invitedAdapter
    }

    override fun setProductList(topupProduct: TopupProduct, next: Boolean, saveToLocal : Boolean) {
        if (saveToLocal) {
            var productListStr = GsonUtils.objToJson(topupProduct)
            KLog.i(productListStr)
            FileUtil.savaData("/Qwallet/productListStr.txt", productListStr)
        }

        topupShowProductAdapter.setNewData(topupProduct.productList)
        if (next) {
            getInviteRank()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun logOut(logout: Logout) {
        llReferralCode.visibility = View.GONE
    }

    private var isStop: Boolean = true
    var autoPlayThread: Thread? = null

    /**
     * 第五步: 设置自动播放,每隔PAGER_TIOME秒换一张图片
     */
    private fun autoPlayView() {
        isStop = false
        //自动播放图片
        if (autoPlayThread == null) {
            autoPlayThread = Thread(Runnable {
                while (true) {
                    if (!isStop && viewList.size > 1) {
                        runOnUiThread { viewPager.currentItem = viewPager.currentItem + 1 }
                        SystemClock.sleep(5000)
                    }
                    SystemClock.sleep(10)
                }
            })
            autoPlayThread!!.start()
        }
    }

    lateinit var showMiningAct: ShowMiningAct
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun setMiningAct(showMiningAct: ShowMiningAct) {
    }

    lateinit var burnQgasAct1: BurnQgasAct
    override fun setBurnQgasAct(burnQgasAct: BurnQgasAct) {
    }

    @Inject
    lateinit internal var mPresenter: TopUpPresenter
    lateinit var countryAdapter: CountryListAdapter
    private var oneFirendClaimQgas = 0f
    private var viewModel: MainViewModel? = null
    internal var viewList: MutableList<Int> = ArrayList()
    lateinit var topupShowProductAdapter: TopupShowProductAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_topup, null)
        ButterKnife.bind(this, view)
        viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        EventBus.getDefault().register(this)
        val mBundle = arguments
        return view
    }

    /**
     * 第四步：设置刚打开app时显示的图片和文字
     */
    private fun setFirstLocation() {
        val m = Integer.MAX_VALUE / 2 % viewList.size
        val currentPosition = Integer.MAX_VALUE / 2 - m
        viewPager.currentItem = currentPosition
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewList.add(R.layout.layout_finance_share)
        var viewAdapter = ImagesPagerAdapter(viewList, viewPager, activity!!)

        val llp = LinearLayout.LayoutParams(UIUtils.getDisplayWidth(activity!!), UIUtils.getStatusBarHeight(activity!!))
        status_bar.setLayoutParams(llp)

        viewPager.adapter = viewAdapter
        val scaleCircleNavigator = ScaleCircleNavigator(activity)
        scaleCircleNavigator.setCircleCount(viewList.size)
        scaleCircleNavigator.setNormalCircleColor(Color.LTGRAY)
        scaleCircleNavigator.setSelectedCircleColor(activity!!.resources.getColor(R.color.mainColor))
        scaleCircleNavigator.setCircleClickListener { index -> viewPager.currentItem = index }
        indicator.navigator = scaleCircleNavigator
        ViewPagerHelper.bind(indicator, viewPager!!, viewList.size)
        rlWallet.setOnClickListener {
            FireBaseUtils.logEvent(activity!!, FireBaseUtils.Topup_Home_ChooseToken)
            val intent1 = Intent(activity!!, TopupSelectDeductionTokenActivity::class.java)
            startActivityForResult(intent1, 11)
        }

        rl1.setOnClickListener {
            FireBaseUtils.logEvent(activity!!, FireBaseUtils.Topup_Home_MyOrders)
            val intent1 = Intent(activity!!, TopupOrderListActivity::class.java)
            startActivity(intent1)
        }
        tvPlaceQuery.setOnClickListener {
            mPresenter.getLocation(hashMapOf<String, String>())
//            val intent1 = Intent(activity!!, PlaceListActivity::class.java)
//            startActivity(intent1)
        }

        viewModel?.currentUserAccount?.observe(this, Observer {
            if (it != null) {
                tvIniviteCode.text = ConstantValue.currentUser.inviteCode
                llReferralCode.visibility = View.VISIBLE
                tvInivteNow.setOnClickListener {
                    FireBaseUtils.logEvent(activity!!, FireBaseUtils.Topup_Home_MyReferralCode_ReferNOW)
                    if ("1".equals(viewModel!!.indexInterfaceMutableLiveData.value!!.dictList.show19)) {
                        startActivity(Intent(activity, EpidemicInviteNowActivity::class.java))
                    } else {
                        startActivity(Intent(activity, InviteNowActivity::class.java))
                    }
                }
                llCopy.setOnClickListener {
                    FireBaseUtils.logEvent(activity!!, FireBaseUtils.Topup_Home_MyReferralCode_Copy)
                    //获取剪贴板管理器：
                    var cm = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    // 创建普通字符型ClipData
                    val mClipData = ClipData.newPlainText("Label", tvIniviteCode.text.toString().trim { it <= ' ' })
                    // 将ClipData内容放到系统剪贴板里。
                    cm!!.setPrimaryClip(mClipData)
                    ToastUtil.displayShortToast(getString(R.string.copy_success))
                }
                if (!it.startApp) {
                    if (!this::selectPayToken.isInitialized) {
                        mPresenter.getPayToken()
                    } else {
                        getOneFriendReward()
                    }
                }
            } else {
                llReferralCode.visibility = View.GONE
            }
        })
        topupShowProductAdapter = TopupShowProductAdapter(arrayListOf())
        recyclerView.adapter = topupShowProductAdapter
        refreshLayout.setColorSchemeColors(resources.getColor(R.color.mainColor))
//        recyclerView.addItemDecoration(BottomMarginItemDecoration(UIUtils.dip2px(15f, activity!!)))
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false

        recyclerViewInvite.setHasFixedSize(true)
        recyclerViewInvite.isNestedScrollingEnabled = false

        getLocalData()

        mPresenter.indexInterface()
        topupShowProductAdapter.setOnItemClickListener { adapter, view, position ->
            FireBaseUtils.logEvent(activity!!, FireBaseUtils.Topup_Choose_a_plan)
            if (ConstantValue.currentUser == null) {
                startActivity(Intent(activity!!, AccountActivity::class.java))
                return@setOnItemClickListener
            }
            if (topupShowProductAdapter.data[position].stock != 0) {
                if ("FIAT".equals(topupShowProductAdapter.data[position].payWay)) {
                    var qurryIntent = Intent(activity!!, QurryMobileActivity::class.java)
                    qurryIntent.putExtra("country", selectedCountry.nameEn)
                    qurryIntent.putExtra("area", selectedCountry.globalRoaming)
                    qurryIntent.putExtra("isp", topupShowProductAdapter.data[position].ispEn.trim())
                    startActivity(qurryIntent)
                } else {
                    getGroupKindList(position)
//                    if (this::proxyAcitivtyDict.isInitialized && TimeUtil.timeStamp(proxyAcitivtyDict.data.topupGroupStartDate) < proxyAcitivtyDict.currentTimeMillis && (TimeUtil.timeStamp(proxyAcitivtyDict.data.topopGroupEndDate) > proxyAcitivtyDict.currentTimeMillis)) {
//
//                    } else {
//                        var qurryIntent = Intent(activity!!, QurryMobileActivity::class.java)
//                        qurryIntent.putExtra("area", selectedCountry.nameEn)
//                        qurryIntent.putExtra("country", selectedCountry.globalRoaming)
//                        qurryIntent.putExtra("isp", topupShowProductAdapter.data[position].ispEn.trim())
//                        startActivity(qurryIntent)
//                    }
                }
            } else {
                toast(getString(R.string.the_product_is_sold_out))
            }
        }
//        rlXingcheng.setOnClickListener {
//
//            var xingchengIntent = Intent(activity!!, PlaceVisitActivity::class.java)
//
//            startActivity(xingchengIntent)
//        }
        refreshLayout.setOnRefreshListener {
            refreshLayout.isRefreshing = false
            mPresenter.indexInterface()
//            if (!this::selectPayToken.isInitialized) {
//                mPresenter.getPayToken()
//            } else {
                getOneFriendReward()
//            }
        }

        var countryListBean = IndexInterface.CountryListBean()
        countryListBean.name = "全部"
        countryListBean.nameEn = "All"
        countryListBean.globalRoaming = ""
        countryListBean.imgPath = ""
        selectedCountry = countryListBean
//        if (BuildConfig.isGooglePlay) {
//            rlXingcheng.visibility = View.GONE
//            rl1.background = resources.getDrawable(R.drawable.main_bg_shape)
//            status_bar.background = resources.getDrawable(R.drawable.main_bg_shape)
//            tv_title.visibility = View.VISIBLE
//        } else {
//
//        }

    }

    override fun setLocation(location: Location) {
        FireBaseUtils.logEvent(activity!!, FireBaseUtils.Campaign_Covid19_more_details)
        if ("domestic".equals(location.location)) {
            val intent = Intent(activity, EpidemicWebViewActivity::class.java)
            intent.putExtra("url", ConstantValue.guoneiEpidemic)
            intent.putExtra("title", "COVID-19 Live Updates")
            startActivity(intent)
        } else {
            val intent = Intent(activity, EpidemicWebViewActivity::class.java)
            intent.putExtra("url", ConstantValue.haiwaiEpidemic)
            intent.putExtra("title", "COVID-19 Live Updates")
            startActivity(intent)
        }
    }

    fun getLocalData() {
        runDelayedOnUiThread(5) {
            KLog.i("获取本地数据")
            try {
                var interfaceListStr = FileUtil.readData("/Qwallet/indexInterfaceStr.txt")
                KLog.i(interfaceListStr)
                if (!"".equals(interfaceListStr)) {
                    var indexInterface = GsonUtils.jsonToObj(interfaceListStr, IndexInterface::class.java)
                    setIndexInterface(indexInterface)
                }
                var productListStr = FileUtil.readData("/Qwallet/productListStr.txt")
                KLog.i(productListStr)
                if (!"".equals(productListStr)) {
                    var topuproduct = GsonUtils.jsonToObj(productListStr, TopupProduct::class.java)
                    setProductList(topuproduct, false, false)
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 11 && resultCode == Activity.RESULT_OK) {
            var returnPayToken = data!!.getParcelableExtra<PayToken.PayTokenListBean>("selectPayToken")
            selectPayToken = returnPayToken
//            selectPayToken.decimal = returnPayToken.decimal
//            selectPayToken.hash = returnPayToken.hash
//            selectPayToken.id = returnPayToken.id
//            selectPayToken.logo_png = returnPayToken.logo_png
//            selectPayToken.logo_webp = returnPayToken.logo_webp
//            selectPayToken.price = returnPayToken.price
//            selectPayToken.symbol = returnPayToken.symbol
//            selectPayToken.usdPrice = returnPayToken.usdPrice

            topupShowProductAdapter.payToken = selectPayToken
            topupShowProductAdapter.notifyDataSetChanged()
            if ("".equals(selectPayToken.logo_png)) {
                Glide.with(activity!!)
                        .load(activity!!.resources.getIdentifier(selectPayToken.symbol.toLowerCase(), "mipmap", activity!!.packageName))
                        .apply(AppConfig.instance.optionsTopup)
                        .into(ivDeduction)
            } else {
                Glide.with(activity!!)
                        .load(selectPayToken.logo_png)
                        .apply(AppConfig.instance.optionsTopup)
                        .into(ivDeduction)
            }
        }
    }

    override fun setGroupDate(dict: Dict, position: Int) {
        this.proxyAcitivtyDict = dict
        if (TimeUtil.timeStamp(dict.data.topupGroupStartDate) < dict.currentTimeMillis && (TimeUtil.timeStamp(dict.data.topopGroupEndDate) > dict.currentTimeMillis)) {
            var productIntent = Intent(activity!!, TopupProductDetailActivity::class.java)
            productIntent.putExtra("productBean", topupShowProductAdapter!!.data[position])
            productIntent.putExtra("globalRoaming", selectedCountry.globalRoaming)
            productIntent.putExtra("phoneNumber", "")
            productIntent.putExtra("selectedPayToken", selectPayToken)
            startActivity(productIntent)
        } else {
            var qurryIntent = Intent(activity!!, QurryMobileActivity::class.java)
            qurryIntent.putExtra("country", selectedCountry.nameEn)
            qurryIntent.putExtra("area", selectedCountry.globalRoaming)
            qurryIntent.putExtra("isp", topupShowProductAdapter.data[position].ispEn.trim())
            qurryIntent.putExtra("selectedPayToken", selectPayToken)
            startActivity(qurryIntent)
//            var deductionTokenPrice = 0.toDouble()
//            if ("CNY".equals(topupShowProductAdapter!!.data[position].payFiat)) {
//                deductionTokenPrice = selectPayToken.price
//            } else if ("USD".equals(topupShowProductAdapter!!.data[position].payFiat)){
//                deductionTokenPrice = selectPayToken.usdPrice
//            }
//
//            var dikoubijine = topupShowProductAdapter!!.data[position].payFiatAmount.toBigDecimal().multiply(topupShowProductAdapter!!.data[position].qgasDiscount.toBigDecimal())
//            var dikoubishuliang = dikoubijine.divide(deductionTokenPrice.toBigDecimal(), 3, BigDecimal.ROUND_HALF_UP)
//            var zhifufabijine = topupShowProductAdapter!!.data[position].payFiatAmount.toBigDecimal().multiply(topupShowProductAdapter!!.data[position].discount.toBigDecimal())
//            var zhifudaibijine = zhifufabijine - dikoubijine
//            var zhifubishuliang = zhifudaibijine.divide(if ("CNY".equals(topupShowProductAdapter!!.data[position].payFiat)){topupShowProductAdapter!!.data[position].payTokenCnyPrice.toBigDecimal()} else {topupAbleAdapter!!.data[position].payTokenUsdPrice.toBigDecimal()}, 3, BigDecimal.ROUND_HALF_UP)
//
//            activity!!.alert(getString(R.string.a_cahrge_of_will_cost_paytoken_and_deduction_token, topupShowProductAdapter!!.data[position].amountOfMoney.toString(), zhifubishuliang.stripTrailingZeros().toPlainString(), topupAbleAdapter!!.data[position].payTokenSymbol, dikoubishuliang.stripTrailingZeros().toPlainString(), selectedPayToken.symbol, topupAbleAdapter!!.data[position].localFiat)) {
//                negativeButton(getString(R.string.cancel)) { dismiss() }
//                positiveButton(getString(R.string.buy_topup)) {
//                    if (AppConfig.instance.daoSession.qlcAccountDao.loadAll().size != 0) {
//                        generateTopupOrder(topupShowProductAdapter!!.data[position])
//                    } else {
//                        activity!!.alert(getString(R.string.you_do_not_have_qlcwallet_create_immediately, "QLC Chain")) {
//                            negativeButton(getString(R.string.cancel)) { dismiss() }
//                            positiveButton(getString(R.string.create)) { startActivity(Intent(activity!!, SelectWalletTypeActivity::class.java)) }
//                        }.show()
//                    }
//                }
//            }.show()
        }
    }

    fun getGroupKindList(posiion: Int) {
        val infoMap: MutableMap<String, String> = HashMap()
        infoMap["dictType"] = "app_dict"
        mPresenter.qurryDict(infoMap, posiion)
    }

    fun queryProxyActivity() {
        val infoMap: MutableMap<String, String> = HashMap()
        infoMap["dictType"] = "app_dict"
        mPresenter.queryDict(infoMap)
    }

    lateinit var selectPayToken: PayToken.PayTokenListBean
    override fun setPayToken(payToken: PayToken) {
//        selectPayToken = payToken.payTokenList[0]
//        topupShowProductAdapter.payToken = selectPayToken
//        if ("".equals(payToken.payTokenList[0].logo_png)) {
//            Glide.with(activity!!)
//                    .load(resources.getIdentifier(payToken.payTokenList[0].symbol.toLowerCase(), "mipmap", activity!!.packageName))
//                    .apply(AppConfig.instance.optionsTopup)
//                    .into(ivDeduction)
//        } else {
//            Glide.with(activity!!)
//                    .load(payToken.payTokenList[0].logo_png)
//                    .apply(AppConfig.instance.optionsTopup)
//                    .into(ivDeduction)
//        }
//        getOneFriendReward()
    }

    override fun setChartData(data: KLine) {

    }

    private val mFragmentContainerHelper = FragmentContainerHelper()
    lateinit var selectedCountry: IndexInterface.CountryListBean
    override fun setCountryList(countryList: CountryList) {
    }

    fun reChangeTaoCan(bean: IndexInterface.CountryListBean) {
        if ("China".equals(bean.nameEn)) {
            FireBaseUtils.logEvent(activity!!, FireBaseUtils.Topup_China)
        } else if ("Singapore".equals(bean.nameEn)) {
            FireBaseUtils.logEvent(activity!!, FireBaseUtils.Topup_Singapore)
        } else if ("Indonesia".equals(bean.nameEn)) {
            FireBaseUtils.logEvent(activity!!, FireBaseUtils.Topup_Indonesia)
        }
        var map = mutableMapOf<String, String>()
        map.put("page", "1")
        map.put("globalRoaming", bean.globalRoaming)
        map.put("deductionTokenId", if (this::selectPayToken.isInitialized) {
            selectPayToken.id
        } else {
            ""
        })
        map.put("size", "20")
        mPresenter.getProductList(map, false, false)
    }

    lateinit var mustGroupKind: TopupGroupKindList.GroupKindListBean
    override fun setGroupKindList(topupGroupList: TopupGroupKindList) {
//        var mustDiscount = 1.toDouble()
//        topupGroupList.groupKindList.forEach {
//            if (mustDiscount > it.discount) {
//                mustDiscount = it.discount
//            }
//        }
//        topupGroupList.groupKindList.forEach {
//            if (mustDiscount == it.discount) {
//                topupShowProductAdapter.mustGroupKind = it
//            }
//        }

    }


    override fun onDestroy() {
        isStop = true
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun setupFragmentComponent() {
        DaggerTopUpComponent
                .builder()
                .appComponent((activity!!.application as AppConfig).applicationComponent)
                .topUpModule(TopUpModule(this))
                .build()
                .inject(this)
    }

    fun getInviteRank() {
        if (ConstantValue.currentUser != null) {
            val infoMap = HashMap<String, String>()
            infoMap["account"] = ConstantValue.currentUser.account
            infoMap["token"] = AccountUtil.getUserToken()
            mPresenter.getInivteRank(infoMap)
        } else {
//            mPresenter.getCountryList(hashMapOf())
        }
    }

    /**
     * 获取邀请到一个好友的奖励数
     */
    private fun getOneFriendReward() {
        val infoMap = HashMap<String, String>()
        infoMap["dictType"] = "winq_invite_reward_amount"
        mPresenter.getOneFriendReward(infoMap)
    }

    override fun setPresenter(presenter: TopUpContract.TopUpContractPresenter) {
        mPresenter = presenter as TopUpPresenter
    }

    override fun initDataFromLocal() {

    }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }
}