package com.stratagile.qlink.ui.activity.topup.presenter
import android.support.annotation.NonNull
import com.socks.library.KLog
import com.stratagile.qlink.api.HttpObserver
import com.stratagile.qlink.application.AppConfig
import com.stratagile.qlink.constant.ConstantValue
import com.stratagile.qlink.data.api.HttpAPIWrapper
import com.stratagile.qlink.db.EntrustTodoDao
import com.stratagile.qlink.db.TopupTodoList
import com.stratagile.qlink.db.TopupTodoListDao
import com.stratagile.qlink.entity.BaseBack
import com.stratagile.qlink.entity.topup.TopupOrder
import com.stratagile.qlink.ui.activity.topup.contract.TopupQlcPayContract
import com.stratagile.qlink.ui.activity.topup.TopupQlcPayActivity
import com.stratagile.qlink.utils.AccountUtil
import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.util.HashMap

/**
 * @author hzp
 * @Package com.stratagile.qlink.ui.activity.topup
 * @Description: presenter of TopupQlcPayActivity
 * @date 2019/09/26 10:08:40
 */
class TopupQlcPayPresenter @Inject
constructor(internal var httpAPIWrapper: HttpAPIWrapper, private val mView: TopupQlcPayContract.View) : TopupQlcPayContract.TopupQlcPayContractPresenter {

    private val mCompositeDisposable: CompositeDisposable

    init {
        mCompositeDisposable = CompositeDisposable()
    }

    override fun subscribe() {

    }

    fun getMainAddress() {
        mCompositeDisposable.add(httpAPIWrapper.getMainAddress(HashMap<String, String>()).subscribe({
            KLog.i("onSuccesse")
            ConstantValue.mainAddress = it.data.neo.address
            ConstantValue.ethMainAddress = it.data.eth.address
            ConstantValue.mainAddressData = it.data
            mView.setMainAddress()
        }, {

        }, {

        }))
    }

    override fun unsubscribe() {
        if (!mCompositeDisposable.isDisposed) {
            mCompositeDisposable.dispose()
        }
    }

    fun createTopupOrder(map: MutableMap<String, String>) {
        mCompositeDisposable.add(httpAPIWrapper.topupCreateOrder(map).subscribe({
            mView.createTopupOrderSuccess(it)
        }, {
            mView.createTopupOrderError()
            TopupTodoList.createTodoList(map)
            sysbackUp(map["txid"]!!, "TOPUP", "", "", "")
        }, {
            mView.createTopupOrderError()
            TopupTodoList.createTodoList(map)
            sysbackUp(map["txid"]!!, "TOPUP", "", "", "")
        }))
    }

    fun sysbackUp(txid: String, type: String, chain: String, tokenName: String, amount: String) {
        val infoMap = java.util.HashMap<String, Any>()
        infoMap["account"] = ConstantValue.currentUser.account
        infoMap["token"] = AccountUtil.getUserToken()
        infoMap["type"] = type
        infoMap["chain"] = chain
        infoMap["tokenName"] = tokenName
        infoMap["amount"] = amount
        infoMap["platform"] = "Android"
        infoMap["txid"] = txid
        httpAPIWrapper.sysBackUp(infoMap).subscribe(object : HttpObserver<BaseBack<*>>() {
            override fun onNext(baseBack: BaseBack<*>) {
                onComplete()
                var list = AppConfig.instance.daoSession.topupTodoListDao.queryBuilder().where(TopupTodoListDao.Properties.Txid.eq(txid)).list()
                if (list.size > 0) {
                    AppConfig.instance.daoSession.topupTodoListDao.delete(list[0])
                }
            }
        })
    }



    fun topupOrderConfirm(map: MutableMap<String, String>) {
        mCompositeDisposable.add(httpAPIWrapper.topupOrderConfirm(map).subscribe({
            mView.topupOrderStatus(it)
        }, {
            var topupOrder = TopupOrder()
            var orderBean = TopupOrder.OrderBean()
            topupOrder.order = orderBean
            topupOrder.order.id = map["orderId"]
            topupOrder.order.status = "NEW"
            mView.topupOrderStatus(topupOrder)
        }, {
            var topupOrder = TopupOrder()
            var orderBean = TopupOrder.OrderBean()
            topupOrder.order = orderBean
            topupOrder.order.id = map["orderId"]
            topupOrder.order.status = "NEW"
            mView.topupOrderStatus(topupOrder)
        }))
    }
}