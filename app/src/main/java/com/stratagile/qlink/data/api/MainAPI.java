package com.stratagile.qlink.data.api;import com.kenai.jffi.Main;/** * Created by zl on 2018/06/13. */public interface MainAPI {    //http://dapp-t.qlink.mobi/api/    String MainBASE_URL = "http://dapp-t.qlink.mobi/";    //http://dapp-t.qlink.mobi/    //http://192.168.1.111:8080/dapp/    /**     * 查询ssid是否注册     * @see API#url_get_ssids     */    @Deprecated    String url_get_ssid = MainBASE_URL + "api/neo/ssId/query.json";    /**     * 查询ssid是否注册     */    String url_get_ssids = MainBASE_URL + "api/neo/ssId/querys.json";    /**     * 注册wifi资产     * registerWifiByFee     */    String url_save_ssid = MainBASE_URL + "api/neo/ssId/registerWifiByFee.json";    /**     * 注册wifi资产     * registerWifiByFee     * 第二版本     */    String url_save_ssid2 = MainBASE_URL + "api/neo/ssId/registerWifiByFeeV4.json";    /**     * 连接WiFi后调用，在断开WiFi的时候还需要调用另外一个接口     */    String url_record_save = MainBASE_URL + "api/neo/record/save.json";    /**     * 获取使用历史记录     * @see API#url_record_querys     */    @Deprecated    String url_record_query = MainBASE_URL + "api/neo/record/query.json";    /**     * 获取使用历史记录     */    String url_record_querys = MainBASE_URL + "api/neo/record/querys.json";    /**     * 创建钱包     */    String url_createWallet = MainBASE_URL + "api/neo/createWalletV2.json";    /**     * 导入钱包     */    String url_importWallet = MainBASE_URL + "api/neo/exportKey.json";    /**     * 批量导入钱包     */    String url_batchImportWallet = MainBASE_URL + "api/neo/batchExportKey.json";    /**     * 获取资产     */    String url_getBalance = MainBASE_URL + "api/neo/getTokenBalance.json";    /**     * 打赏     */    String url_reward = MainBASE_URL + "api/neo/wif/ds.json";    /**     * c层需要的数据，通过这个接口拿的     */    String url_c_nodes = "https://nodes.tox.chat/json";    /**     * 获取汇率     */    String url_get_raw = MainBASE_URL + "api/neo/raw.json";    /**     * neo兑换为qlc     */    String url_neo_exchange_qlc = MainBASE_URL + "api/neo/neoExchangeQlcV2.json";    /**     * 交易接口     */    String url_transaction = MainBASE_URL + "api/neo/transfer.json";    /**     * 注册vpn资产     */    String url_vpn_save = MainBASE_URL + "api/neo/ssId/registerVpnByFee.json";    /**     * 注册vpn资产     * (第二版本)     */    String url_vpn_save2 = MainBASE_URL + "api/neo/ssId/registerVpnByFeeV6.json";    /**     * 根据国家获取vpn资产列表接口     */    String url_vpn_query = MainBASE_URL + "api/neo/vpn/queryVpnV2.json";    String url_vpn_query_v3 = MainBASE_URL + "api/neo/vpn/queryVpnV3.json";    String latlngParseCountry = "https://maps.google.com/maps/api/geocode/json";    /**     * 验证vpn名字是否存在     */    String vertify_vpn_name = MainBASE_URL + "api/neo/validateAssetIsexist.json";    /**     * 保存vpn记录接口     */    String vpn_record_save = MainBASE_URL + "api/neo/vpn/saveCVpnRecord.json";    /**     * 更新用户的头像     */    String user_update_avatar = MainBASE_URL + "fapi/user/uploadHeadView.json";    /**     * 心跳     */    String heart_beat = MainBASE_URL + "api/neo/heartbeatV3.json";    /**     * 更新vpn基本信息接口     */    String update_vpn_info = MainBASE_URL + "api/neo/ssId/updateVpnInfoV4.json";    /**     * 更新wifi基本信息接口     */    String update_wifi_info = MainBASE_URL + "api/neo/ssId/updateWifiInfoV3.json";    /**     * 获取自己的头像的接口     */    String user_headView = MainBASE_URL + "fapi/user/getHeadView.json";    /**     * 获取unspent     */    String getUnspentAsset = MainBASE_URL + "api/neo/allUnpspentAsset.json";    /**     * 转账的的直接调用接口，app中不直接调用。     */    String sendRow = MainBASE_URL + "api/neo/sendrawtransaction.json";    /**     * 连接vpn和打赏的接口     */    String url_transaction_v2 = MainBASE_URL + "api/neo/ssId/transTypeOperate.json";    /**     * 获取主兑换地址，包括neo兑换为qlc的地址和bnb兑换为qlc的地址     */    String url_main_address = MainBASE_URL + "api/neo/getMainAddressV2.json";    /**     * bnb兑换qlc的接口     */    String url_bnb_2_qlc = MainBASE_URL + "api/neo/bnbExchangeQlc.json";    /**     * 查询以太坊钱包余额     */    String url_eth_wallet_value = "https://api.ethplorer.io/getAddressInfo/{address}";    /**     * 获取服务器时间     */    String url_get_server_time = MainBASE_URL + "api/wc/getServerTime.json";    /**     * 获取赛程列表接口     */    String url_race_times = MainBASE_URL + "api/wc/raceTimes.json";    /**     * 用户投注列表接口     */    String url_findbet = MainBASE_URL + "api/wc/findBets.json";    /**     * 投注接口     */    String url_bet = MainBASE_URL + "api/wc/bet.json";    /**     * 赠送免费次数接     */    String url_zs_free_num = MainBASE_URL + "api/neo/zsFreeNum.json";    /**     * 免费连接vpn接口     */    String url_freeConnection = MainBASE_URL + "api/neo/vpn/freeConnection.json";    /**     * 查找免费使用记录接口     */    String url_queryFreeRecords = MainBASE_URL + "api/neo/queryFreeRecords.json";    /**     * 上报vpn信息接口     */    String reportVpnInfo = MainBASE_URL + "api/neo/reportVpnInfo.json";    /**     * 获取活动的接口     */    String act_get =  MainBASE_URL + "api/act/queryActs.json";    /**     * 获取活动的资产     */    String act_asset = MainBASE_URL + "api/act/queryVpnRankings.json";    /**     * 获取是否展示活动的配置     */    String act_show = MainBASE_URL + "api/act/isShowRanking.json";    /**     * eth钱包的token     */    String get_eth_wallet_info = MainBASE_URL + "api/eth/address_info.json";    /**     * neo钱包的token     */    String get_neo_wallet_info = MainBASE_URL + "api/neo/address_info.json";    /**     * 获取token的价值     */    String url_token_price = MainBASE_URL + "api/token/price.json";    /**     * 获取24小时涨跌     */    String url_bina_tpcs = MainBASE_URL + "api/bina/tpcs.json";    /**     * eth钱包某个token的交易记录     */    String url_eth_address_history = MainBASE_URL + "api/eth/address_history.json";    /**     * neo钱包交易记录     */    String url_neo_address_history = MainBASE_URL + "api/neo/getAllTransferByAddress.json";    String url_main_net_unspent = MainBASE_URL + "api/neo/mainNet/allUnpspentAsset_v2.json";    /**     * neo代币转账     */    String url_neo_token_transacation = MainBASE_URL + "api/neo/mainNet/nep5Transfer.json";    /**     * 获取k线     */    String url_kline = MainBASE_URL + "api/bina/klines.json";    /**     * 这个接口只拿eth的转账记录     */    String url_eth_history = MainBASE_URL + "api/eth/address_transactions.json";    /**     * 上报创建钱包地址接口     */    String url_report_wallet_create = MainBASE_URL + "api/wallet/report_wallet_create_v2.json";    /**     * 上报钱包转账接口     */    String url_wallet_transaction_report = MainBASE_URL + "api/wallet/report_wallet_transfer.json";    /**     * 查询winqgas     */    String url_query_winq_gas = MainBASE_URL + "api/neo/queryWGas.json";    /**     * 领取winqgas     */    String url_got_winq_gas = MainBASE_URL + "api/neo/gotWGas.json";    String url_neo_gas_claim = MainBASE_URL + "api/neo/getClaims.json";    /**     * 查询eos账户基本信息     */    String url_eos_account_info = MainBASE_URL + "api/eos/get_account_info.json";    String url_eos_token_list = MainBASE_URL + "api/eos/get_token_list.json";    String url_get_account_resource = MainBASE_URL + "api/eos/get_account_resource_info.json";    /**     * 查询eos链上的某个代币的转账记录     */    String url_eos_account_transaction_info = MainBASE_URL + "api/eos/get_account_related_trx_info.json";    /**     * 查询币安支持查询24小时涨跌的币     */    String url_bina_gettokens = MainBASE_URL +  "api/bina/getTokens.json";    /**     * 创建eos账户     */    String url_create_eos_account = MainBASE_URL + "api/eos/new_account.json";    /**     * 注册eos需要的eth和转账eth地址     */    String url_create_eos_need_info = MainBASE_URL + "api/eth/eth_for_activate_eos_wallet.json";    //    String url_key_account = "https://history.cryptolions.io/v1/history/get_key_accounts/{public_key}";    String url_key_account = "https://history.cryptolions.io/v2/state/get_key_accounts";    String url_eos_resource_price = MainBASE_URL + "api/eos/eos_resource_price.json";    //新版本winq  api。。。    /**     * 获取注册验证码     */    String url_vcode_signup_code = MainBASE_URL + "api/vcode/signup_code.json";    /**     * 注册     */    String ulr_user_sign_up = MainBASE_URL + "api/user/sign_up.json";    /**     * 登录     */    String url_user_sign_in = MainBASE_URL + "api/user/sign_in.json";    /**     * 邀请好友     */    String url_user_invite = MainBASE_URL + "api/user/invite.json";    /**     * 邀请排名     */    String url_user_invite_ranking = MainBASE_URL + "api/user/invite_rankings.json";    /**     * 富豪榜     */    String url_user_rich_list = MainBASE_URL + "api/user/rich_list.json";    /**     * 产品列表     */    String url_financial_product_list = MainBASE_URL + "api/financial/product_list.json";    /**     * 产品详情     */    String url_financial_product_info = MainBASE_URL + "api/financial/product_info.json";    /**     * 购买产品     */    String url_financial_product_order = MainBASE_URL + "api/financial/order.json";    /**     * 订单列表     */    String url_financial_order_list = MainBASE_URL + "api/financial/order_list.json";    /**     * 订单赎回     */    String url_financial_redeem = MainBASE_URL + "api/financial/redeem.json";    /**     * 历史操作记录     */    String url_finance_history_record = MainBASE_URL + "api/financial/history_record.json";    /**     * 获取登录验证码     */    String url_vcode_sign_in_code = MainBASE_URL + "api/vcode/signin_code.json";    /**     * 验证码登录     */    String url_user_signin_code = MainBASE_URL + "api/user/signin_code.json";    /**     * 获取修改密码验证码     */    String url_vcode_change_password_code = MainBASE_URL + "api/vcode/change_password_code.json";    /**     * 获取更换手机验证码     */    String url_vcode_change_phone_code = MainBASE_URL + "api/vcode/change_phone_code.json";    /**     * 修改密码     */    String url_user_change_password = MainBASE_URL + "api/user/change_password.json";    /**     * 更换手机号码     */    String url_user_change_phone = MainBASE_URL + "api/user/change_phone.json";    /**     * 更换邮箱     */    String url_user_change_email = MainBASE_URL + "api/user/change_email.json";    /**     * 修改昵称     */    String url_user_change_nickname = MainBASE_URL + "api/user/change_nickname.json";    /**     * 添加委托订单     */    String url_entrust_order = MainBASE_URL + "api/entrust/order.json";    /**     * 委托订单列表     */    String url_entrust_order_list = MainBASE_URL + "api/entrust/order_list.json";    /**     * 委托订单列表     */    String url_entrust_order_info = MainBASE_URL + "api/entrust/order_info.json";    /**     * 取消委托订单     */    String url_entrust_cancel_order = MainBASE_URL + "api/entrust/cancel_order.json";    /**     * 生成买单     */    String url_trade_buy_order = MainBASE_URL + "api/trade/buy_order.json";    /**     * 买家确认已转出USDT     */    String url_trade_buyer_confirm = MainBASE_URL + "api/trade/buyer_confirm.json";    /**     * 生成卖单     */    String url_trade_sell_order = MainBASE_URL + "api/trade/sell_order.json";    /**     * 卖家确认已收到USDT     */    String url_trade_seller_confirm = MainBASE_URL + "api/trade/seller_confirm.json";    /**     * 买卖订单列表     */    String url_trade_order_list = MainBASE_URL + "api/trade/order_list.json";    /**     * 买卖订单详情     */    String url_trade_order_info = MainBASE_URL + "api/trade/order_info.json";    /**     * 买家取消订单接口     */    String url_trade_cancel = MainBASE_URL + "api/trade/cancel.json";    /**     * 申诉     */    String url_trade_appeal = MainBASE_URL + "fapi/user/appeal.json";    /**     * 上传用户头像     */    String url_user_upload_headview = MainBASE_URL + "fapi/user/upload_headview.json";    String url_uploadIdCard = MainBASE_URL + "fapi/user/upload_id_card.json";    String url_user_userinfo = MainBASE_URL + "api/user/user_info.json";    /**     * 获取版本信息     */    String url_app_version_info = MainBASE_URL + "api/sys/version_info.json";    /**     * 获取交易对     */    String url_pairs = MainBASE_URL + "api/pairs/pairs.json";    String url_contact_unlock = MainBASE_URL + "api/sys/contract_unlock.json";    /**     * 充值产品列表     */    String url_topup_productlist = MainBASE_URL + "api/topup/product_list.json";    /**     * 充值订单     */    String url_topup_order = MainBASE_URL + "api/topup/order.json";    /**     * 充值订单列表     */    String url_topup_order_list = MainBASE_URL + "api/topup/order_list.json";    /**     * 取消订单     */    String url_topup_cancel_order = MainBASE_URL + "api/topup/cancel_order.json";    /**     * 保存错误日志     */    String url_log_save = MainBASE_URL + "api/log/save.json";    /**     * 绑定qlcchain钱包地址     */    String url_bind_qlcchain_wallet = MainBASE_URL + "api/user/bind.json";    /**     * 奖励列表     */    String url_reward_list = MainBASE_URL + "api/reward/reward_list.json";    /**     * 领取qgas奖励     */    String url_reward_claims = MainBASE_URL + "api/reward/claim_bind.json";    /**     * 领取邀请用户获得的奖励     */    String url_reward_claim_invite = MainBASE_URL + "api/reward/claim_invite.json";    /**     * qgas奖励总数     */    String url_reward_tatal = MainBASE_URL + "api/reward/reward_total.json";    /**     * 邀请人数     */    String url_inivte_amount = MainBASE_URL + "api/user/invite_amount.json";    /**     * 字典查询     */    String url_sys_dict = MainBASE_URL + "api/sys/dict.json";}