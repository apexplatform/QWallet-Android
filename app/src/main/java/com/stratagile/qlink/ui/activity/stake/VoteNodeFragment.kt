package com.stratagile.qlink.ui.activity.stake

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.annotation.Nullable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import com.stratagile.qlink.application.AppConfig
import com.stratagile.qlink.base.BaseFragment
import com.stratagile.qlink.ui.activity.stake.component.DaggerVoteNodeComponent
import com.stratagile.qlink.ui.activity.stake.contract.VoteNodeContract
import com.stratagile.qlink.ui.activity.stake.module.VoteNodeModule
import com.stratagile.qlink.ui.activity.stake.presenter.VoteNodePresenter

import javax.inject.Inject;

import butterknife.ButterKnife;
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pawegio.kandroid.alert
import com.pawegio.kandroid.runOnUiThread
import com.pawegio.kandroid.toast
import com.socks.library.KLog
import com.stratagile.qlc.QLCAPI
import com.stratagile.qlc.entity.BaseResult
import com.stratagile.qlink.Account
import com.stratagile.qlink.R
import com.stratagile.qlink.db.QLCAccount
import com.stratagile.qlink.db.Wallet
import com.stratagile.qlink.db.WalletDao
import com.stratagile.qlink.entity.AllWallet
import com.stratagile.qlink.entity.MyAsset
import com.stratagile.qlink.entity.NeoWalletInfo
import com.stratagile.qlink.entity.eventbus.ChangeCurrency
import com.stratagile.qlink.entity.eventbus.ChangeWallet
import com.stratagile.qlink.entity.stake.*
import com.stratagile.qlink.ui.activity.otc.OtcChooseWalletActivity
import com.stratagile.qlink.utils.LocalWalletUtil
import com.stratagile.qlink.utils.OtcUtils
import com.stratagile.qlink.utils.QlcReceiveUtils
import com.stratagile.qlink.utils.getLine
import com.stratagile.qlink.view.SweetAlertDialog
import kotlinx.android.synthetic.main.activity_sell_qgas.*
import kotlinx.android.synthetic.main.fragment_vote_node.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringEscapeUtils
import org.greenrobot.eventbus.EventBus
import org.spongycastle.crypto.tls.ContentType.alert
import qlc.bean.StateBlock
import qlc.mng.AccountMng
import qlc.mng.BlockMng
import qlc.mng.WalletMng
import qlc.network.QlcClient
import qlc.network.QlcException
import qlc.utils.Constants
import qlc.utils.Helper
import qlc.utils.WorkUtil
import java.math.BigDecimal
import java.net.URLEncoder
import java.util.*
import kotlin.concurrent.thread

/**
 * @author hzp
 * @Package com.stratagile.qlink.ui.activity.stake
 * @Description: $description
 * @date 2019/08/08 16:37:41
 */

class VoteNodeFragment : BaseFragment(), VoteNodeContract.View {

    @Inject
    lateinit internal var mPresenter: VoteNodePresenter

    var neoWallet: Wallet? = null
    var qlcWallet: QLCAccount? = null
    var neoWalletInfo: NeoWalletInfo? = null
    var neoQlcTokenInfo: NeoWalletInfo.DataBean.BalanceBean? = null
    lateinit var stakeViewModel: NewStakeViewModel

    override fun setNeoDetail(neoWalletInfo: NeoWalletInfo) {
        closeProgressDialog()
        this.neoWalletInfo = neoWalletInfo
        neoWalletInfo.data.balance.forEach {
            if (it.asset_symbol.equals("QLC")) {
                neoQlcTokenInfo = it
                tvQlcBalance.text = getString(R.string.balance) + ": ${it.amount} QLC"
            }
//            if (it.asset_symbol.equals("GAS")) {
//                gasTokenInfo = it
//            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_vote_node, null)
        ButterKnife.bind(this, view)
        val mBundle = arguments
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvQlcBalance.text = getString(R.string.balance) + ": -/- QLC"
        stakeViewModel = ViewModelProviders.of(activity!!).get<NewStakeViewModel>(NewStakeViewModel::class.java)
        stakeViewModel.lockResult.observe(this, Observer<LockResult> {
            if (it!!.stakeType.type == 0) {
                getNep5Txid(it)
            }
        })
        stakeViewModel.txidMutableLiveData.observe(this, Observer {
            showEnterTxIdDialog()
        })
        ivVote.setOnClickListener {
            showEnterTxIdDialog()
        }
        invoke.setOnClickListener {
            if (neoWallet == null) {
                return@setOnClickListener
            }
            if (qlcWallet == null) {
                return@setOnClickListener
            }
            if (neoQlcTokenInfo == null) {
                val infoMap = HashMap<String, String>()
                infoMap["address"] = neoWallet!!.address
                mPresenter.getNeoWalletDetail(infoMap, neoWallet!!.address)
                toast(getString(R.string.pleasewait))
                return@setOnClickListener
            }
            if ("".equals(etStakeQlcAmount.text.toString().trim())) {
                return@setOnClickListener
            }
            if ("".equals(etStakeDays.text.toString().trim())) {
                return@setOnClickListener
            }
            if (etStakeQlcAmount.text.toString().trim().toInt() > neoQlcTokenInfo!!.amount) {
                toast(getString(R.string.no_enough) + " QLC")
                return@setOnClickListener
            }
            showStakingPup()
            var stakeQLcAmount = etStakeQlcAmount.text.toString().trim()
            var stakeQlcDays = etStakeDays.text.toString().trim()
            var priKey = neoWallet!!.privateKey
            var fromAddress = neoWallet!!.address
            var qlcchainAddress = qlcWallet!!.address
            var neoPubKey = neoWallet!!.publicKey
            var stakeType = StakeType(0)
            stakeType.fromNeoAddress = fromAddress
            stakeType.neoPriKey = priKey
            stakeType.neoPubKey = neoPubKey
            stakeType.qlcchainAddress = qlcchainAddress
            stakeType.stakeQLcAmount = stakeQLcAmount
            stakeType.stakeQlcDays = stakeQlcDays
            EventBus.getDefault().post(stakeType)
        }

        llSelectNeoWallet.setOnClickListener {
            var intent1 = Intent(activity, OtcChooseWalletActivity::class.java)
            intent1.putExtra("walletType", AllWallet.WalletType.NeoWallet.ordinal)
            intent1.putExtra("select", true)
            startActivityForResult(intent1, AllWallet.WalletType.NeoWallet.ordinal)
            activity!!.overridePendingTransition(R.anim.activity_translate_in, R.anim.activity_translate_out)
        }

        llSelectQlcWallet.setOnClickListener {
            var intent1 = Intent(activity, OtcChooseWalletActivity::class.java)
            intent1.putExtra("walletType", AllWallet.WalletType.QlcWallet.ordinal)
            intent1.putExtra("select", true)
            startActivityForResult(intent1, AllWallet.WalletType.QlcWallet.ordinal)
            activity!!.overridePendingTransition(R.anim.activity_translate_in, R.anim.activity_translate_out)
        }
        etStakeDays.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if ("".equals(p0!!.toString())) {
                    return
                }
                if (p0!!.toString().toInt() >= 10) {
                    if (etStakeQlcAmount.text.toString().trim().toInt() >= 1 && neoWallet != null && qlcWallet != null) {
                        invoke.background = resources.getDrawable(R.drawable.main_color_bt_bg)
                        invoke.isEnabled = true
                    } else {
                        invoke.background = resources.getDrawable(R.drawable.unable_bt_bg)
                        invoke.isEnabled = false
                    }
                } else {
                    invoke.background = resources.getDrawable(R.drawable.unable_bt_bg)
                    invoke.isEnabled = false
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    var sweetAlertDialog: SweetAlertDialog? = null

    fun showStakingPup() {
        val view = layoutInflater.inflate(R.layout.alert_staking, null, false)
        sweetAlertDialog = SweetAlertDialog(activity)
        sweetAlertDialog!!.setView(view)
        sweetAlertDialog!!.show()
    }

    fun getNep5Txid(lockResult: LockResult) {
        thread {
            val dataJson = jsonObject(
                    "jsonrpc" to "2.0",
                    "method" to "getnep5transferbytxid",
                    "params" to jsonArray(lockResult.txid),
                    "id" to 3
            )
//            var request = "https://api.nel.group/api/mainnet".httpPost().body(dataJson.toString())
            var request = "https://api.neoscan.io/api/main_net/v1/get_transaction/${lockResult.txid}".httpGet()
            request.headers["Content-Type"] = "application/json"
            request.responseString { _, _, result ->
                val (data, error) = result
                if (error == null) {
                    val gson = Gson()
                    KLog.i(data!!)
                    val nodeResponse = gson.fromJson<NeoTransactionInfo>(data, object : TypeToken<NeoTransactionInfo>() {}.type)
                    KLog.i(nodeResponse.toString())
                    if (nodeResponse.errors == null && nodeResponse.block_hash != null) {
                        prePareBenefitPledge(lockResult = lockResult)
                    }  else {
                        launch {
                            delay(3000)
                            getNep5Txid(lockResult)
                        }
                    }
                } else {
                    launch {
                        delay(3000)
                        getNep5Txid(lockResult)
                    }
                    KLog.i(error.localizedMessage)
                }
            }
        }
    }

    fun showEnterTxIdDialog() {
        //
        val view = View.inflate(activity!!, R.layout.dialog_input_txid_layout, null)
        val etContent = view.findViewById<View>(R.id.etContent) as EditText//输入内容
        val tvCancel = view.findViewById<TextView>(R.id.tvCancel)//取消按钮
        val tvOk = view.findViewById<TextView>(R.id.tvOk)
        //取消或确定按钮监听事件处l
        val sweetAlertDialog = SweetAlertDialog(activity!!)
        val window = sweetAlertDialog.window
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        sweetAlertDialog.setView(view)
        sweetAlertDialog.show()
        tvCancel.setOnClickListener {
            sweetAlertDialog.cancel()
        }
        tvOk.setOnClickListener {
            //            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//            cm.primaryClip = ClipData.newPlainText("", etContent.text.toString().trim())
            sweetAlertDialog.cancel()
            try {
                checkTxid(etContent.text.toString().trim())
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }


    fun checkTxid(txid :String) {
        showProgressDialog()
        thread {
            try {
                val client = QlcClient("https://nep5.qlcchain.online")
                val params = JSONArray()
                params.add(txid)
                var result = client.call("ledger_pledgeInfoByTransactionID", params)
                KLog.i(result.toJSONString())
                var pledgeInfo = Gson().fromJson(result.toJSONString(), PledgeInfo::class.java)
                if (pledgeInfo.result == null) {
                    recoverStake(txid)
                } else {
                    runOnUiThread {
                        closeProgressDialog()
                        toast("this txid is pledged")
                    }
                }
            } catch (e : Exception) {
                e.printStackTrace()
                runOnUiThread {
                    closeProgressDialog()
                    toast("get txid info error")
                }
            }
        }
    }

    fun recoverStake(txid : String) {
        thread {
            try {
                val client = QlcClient("https://nep5.qlcchain.online")
                val params = JSONArray()
                params.add(txid)
                var result = client.call("nep5_getLockInfo", params)
                KLog.i(result.toJSONString())
                var lockInfo = Gson().fromJson(result.toJSONString(), LockInfo::class.java)

                if (lockInfo.result.isState) {
                    var lockResult = LockResult()
                    var stakeType = StakeType(0)
                    lockResult.stakeType = stakeType
                    lockResult.stakeType.fromNeoAddress = lockInfo.result.neoAddress
                    lockResult.stakeType.toAddress = lockInfo.result.multiSigAddress
                    lockResult.stakeType.qlcchainAddress = lockInfo.result.qlcAddress
                    lockResult.txid = txid
                    var oneDay = 60*60*24
                    var lockDays = (lockInfo.result.unLockTimestamp - lockInfo.result.lockTimestamp) / oneDay

                    lockResult.stakeType.stakeQLcAmount = lockInfo.result.amount.toBigDecimal().divide(10.toBigDecimal().pow(8), 8,  BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
                    var neoList = AppConfig.instance.daoSession.walletDao.loadAll()
                    neoList.forEach {
                        if (it.address.equals(lockResult.stakeType.fromNeoAddress)) {
                            neoWallet = it
                            if (neoWallet!!.privateKey.toLowerCase().equals(neoWallet!!.publicKey.toLowerCase())) {

                                Account.fromWIF(neoWallet!!.wif)
                                neoWallet!!.publicKey = Account.byteArray2String(Account.getWallet()!!.publicKey).toLowerCase()
                                AppConfig.instance.daoSession.walletDao.update(neoWallet)
                            }
                        }
                    }

                    if (neoWallet == null) {
                        runOnUiThread {
                            closeProgressDialog()
                            toast("neo wallet not found")
                        }
                    } else {
                        lockResult.stakeType.neoPriKey = neoWallet!!.privateKey
                        lockResult.stakeType.neoPubKey = neoWallet!!.publicKey
                        prePareBenefitPledge(lockResult = lockResult)
                        runOnUiThread {
                            closeProgressDialog()
                            showStakingPup()
                        }
                    }

                } else {
                    runOnUiThread {
                        closeProgressDialog()
                        toast("txid is unlock")
                    }
                }
            } catch (e : Exception) {
                e.printStackTrace()
                runOnUiThread {
                    closeProgressDialog()
                    toast("get lockInfo error")
                }
            }
        }
    }

    fun prePareBenefitPledge(lockResult: LockResult) {
        val client = QlcClient("https://nep5.qlcchain.online")
        val params = JSONArray()
        val paramOne = JSONObject()
        paramOne["beneficial"] = lockResult.stakeType.qlcchainAddress
        paramOne["amount"] = lockResult.stakeType.stakeQLcAmount.toBigDecimal().multiply(10.toBigDecimal().pow(8)).stripTrailingZeros().toPlainString()
        paramOne["pType"] = "vote"
        params.add(paramOne)
        val paramTwo = JSONObject()
        paramTwo["multiSigAddress"] = lockResult.stakeType.toAddress
        paramTwo["publicKey"] = lockResult.stakeType.neoPubKey
        paramTwo["lockTxId"] = lockResult.txid
        params.add(paramTwo)
        val result = client.call("nep5_prePareBenefitPledge", params)
        KLog.i(result)
        benefitPledge(lockResult = lockResult)
    }

    fun benefitPledge(lockResult: LockResult) {
        val client = QlcClient("https://nep5.qlcchain.online")
        val params = JSONArray()
        params.add(lockResult.txid)
        var result = client.call("nep5_benefitPledge", params)
        KLog.i(result.toString())
        var stateBlock = Gson().fromJson(result.getJSONObject("result").toString(), StateBlock::class.java)

        var root = BlockMng.getRoot(stateBlock)
//        var root = AccountMng.addressToPublicKey(stateBlock.address)
        var params1 = mutableListOf<Pair<String, String>>()
        params1.add(Pair("root", root))
        var request = "http://pow1.qlcchain.org/work".httpGet(params1)
        request.responseString { _, _, result ->
            KLog.i("远程work返回、、")
            val (data, error) = result
            try {
                if (error == null) {
                    stateBlock.work = data
                    var hash = BlockMng.getHash(stateBlock)
                    var signature = WalletMng.sign(hash, Helper.hexStringToBytes(QlcReceiveUtils.drivePrivateKey(lockResult.stakeType.qlcchainAddress).substring(0, 64)))
                    val signCheck = WalletMng.verify(signature, hash, Helper.hexStringToBytes(QlcReceiveUtils.drivePublicKey(lockResult.stakeType.qlcchainAddress)))
                    if (!signCheck) {
                        KLog.i("签名验证失败")
                        return@responseString
                    }
                    stateBlock.setSignature(Helper.byteToHexString(signature))
                    lockResult.stateBlock = stateBlock
                    process(lockResult)
                } else {

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun process(lockResult: LockResult) {
        val client = QlcClient("https://nep5.qlcchain.online")
        val params = JSONArray()
        params.add(JSONObject.parse(Gson().toJson(lockResult.stateBlock)))
        params.add(lockResult.txid)

        println(params.toJSONString())

        val result = client.call("ledger_process", params)
        println(result)
        if (result.getString("result") == null) {
            AppConfig.instance.saveLog("stake", "voteNode process" + getLine(), URLEncoder.encode(result.toJSONString() + Gson().toJson(lockResult.stateBlock), "UTF-8"))
        }
        runOnUiThread {
            sweetAlertDialog?.dismissWithAnimation()
            launch {
                delay(300)
                activity!!.setResult(Activity.RESULT_OK)
                activity!!.finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AllWallet.WalletType.NeoWallet.ordinal -> {
                    neoWallet = data!!.getParcelableExtra("wallet")
                    if (neoWallet!!.privateKey.toLowerCase().equals(neoWallet!!.publicKey.toLowerCase())) {
                        var list = AppConfig.instance.daoSession.walletDao.loadAll()
                        var hasSelectNeo = false
                        list.forEach {
                            if (it.isCurrent) {
                                hasSelectNeo = true
                                it.isCurrent = false
                                AppConfig.instance.daoSession.walletDao.update(it)
                            }
                        }
                        Account.fromWIF(neoWallet!!.wif)
                        neoWallet!!.publicKey = Account.byteArray2String(Account.getWallet()!!.publicKey).toLowerCase()
                        if (hasSelectNeo) {
                            neoWallet!!.isCurrent = true
                            EventBus.getDefault().post(ChangeWallet())
                        }
                        AppConfig.instance.daoSession.walletDao.update(neoWallet)
                    }


                    tvNeoWalletName.text = neoWallet!!.name
                    tvNeoWalletAddess.text = neoWallet!!.address
                    val infoMap = HashMap<String, String>()
                    infoMap["address"] = neoWallet!!.address
                    mPresenter.getNeoWalletDetail(infoMap, neoWallet!!.address)
                }
                AllWallet.WalletType.QlcWallet.ordinal -> {
                    qlcWallet = data!!.getParcelableExtra("wallet")
                    tvQLCWalletName.text = qlcWallet!!.accountName
                    tvQLCWalletAddess.text = qlcWallet!!.address
                }
            }
        }
    }


    override fun setupFragmentComponent() {
        DaggerVoteNodeComponent
                .builder()
                .appComponent((activity!!.application as AppConfig).applicationComponent)
                .voteNodeModule(VoteNodeModule(this))
                .build()
                .inject(this)
    }

    override fun setPresenter(presenter: VoteNodeContract.VoteNodeContractPresenter) {
        mPresenter = presenter as VoteNodePresenter
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