package com.stratagile.qlink.ui.activity.my;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.socks.library.KLog;
import com.stratagile.qlink.R;
import com.stratagile.qlink.application.AppConfig;
import com.stratagile.qlink.base.BaseFragment;
import com.stratagile.qlink.constant.ConstantValue;
import com.stratagile.qlink.data.api.MainAPI;
import com.stratagile.qlink.db.UserAccount;
import com.stratagile.qlink.entity.UserInfo;
import com.stratagile.qlink.entity.eventbus.ChangeViewpager;
import com.stratagile.qlink.entity.eventbus.LoginSuccess;
import com.stratagile.qlink.entity.eventbus.UpdateAvatar;
import com.stratagile.qlink.entity.reward.InviteTotal;
import com.stratagile.qlink.entity.reward.RewardTotal;
import com.stratagile.qlink.ui.activity.reward.MyClaimActivity;
import com.stratagile.qlink.ui.activity.finance.InviteActivity;
import com.stratagile.qlink.ui.activity.finance.JoinCommunityActivity;
import com.stratagile.qlink.ui.activity.main.MainViewModel;
import com.stratagile.qlink.ui.activity.main.TestActivity;
import com.stratagile.qlink.ui.activity.my.component.DaggerMyComponent;
import com.stratagile.qlink.ui.activity.my.contract.MyContract;
import com.stratagile.qlink.ui.activity.my.module.MyModule;
import com.stratagile.qlink.ui.activity.my.presenter.MyPresenter;
import com.stratagile.qlink.ui.activity.setting.SettingsActivity;
import com.stratagile.qlink.utils.AccountUtil;
import com.stratagile.qlink.view.MyItemView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author hzp
 * @Package com.stratagile.qlink.ui.activity.my
 * @Description: $description
 * @date 2019/04/09 10:02:03
 */

public class MyFragment extends BaseFragment implements MyContract.View {

    @Inject
    MyPresenter mPresenter;
    @BindView(R.id.userAvatar)
    ImageView userAvatar;
    @BindView(R.id.userName)
    TextView userName;
    @BindView(R.id.userId)
    TextView userId;
    @BindView(R.id.user)
    LinearLayout user;
    @BindView(R.id.cryptoWallet)
    MyItemView cryptoWallet;
    @BindView(R.id.shareFriend)
    MyItemView shareFriend;
    @BindView(R.id.joinCommunity)
    MyItemView joinCommunity;
    @BindView(R.id.contactUs)
    MyItemView contactUs;
    @BindView(R.id.settings)
    MyItemView settings;
    boolean isLogin = false;
    @BindView(R.id.testView)
    View testView;
    @BindView(R.id.claimQlc)
    MyItemView claimQlc;
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, null);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        Bundle mBundle = getArguments();
        viewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        return view;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void loginSuccess(LoginSuccess loginSuccess) {
        List<UserAccount> userAccounts = AppConfig.getInstance().getDaoSession().getUserAccountDao().loadAll();
        if (userAccounts.size() > 0) {
            for (UserAccount userAccount : userAccounts) {
                if (userAccount.getIsLogin()) {
                    loginUser = userAccount;
                    ConstantValue.currentUser = userAccount;
                    viewModel.currentUserAccount.postValue(userAccount);
                    isLogin = true;
                    userName.setText(loginUser.getAccount());
                    if (ConstantValue.currentUser.getAvatar() != null && !"".equals(ConstantValue.currentUser.getAvatar())) {
                        Glide.with(this)
                                .load(MainAPI.MainBASE_URL + ConstantValue.currentUser.getAvatar())
                                .apply(AppConfig.getInstance().options)
                                .into(userAvatar);
                    } else {
                        Glide.with(this)
                                .load(R.mipmap.icon_user_default)
                                .apply(AppConfig.getInstance().options)
                                .into(userAvatar);
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateImg(UpdateAvatar updateAvatar) {
        if (ConstantValue.currentUser.getAvatar() != null && !"".equals(ConstantValue.currentUser.getAvatar())) {
            Glide.with(this)
                    .load(MainAPI.MainBASE_URL + ConstantValue.currentUser.getAvatar())
                    .apply(AppConfig.getInstance().options)
                    .into(userAvatar);
        } else {
            Glide.with(this)
                    .load(R.mipmap.icon_user_default)
                    .apply(AppConfig.getInstance().options)
                    .into(userAvatar);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<UserAccount> userAccounts = AppConfig.getInstance().getDaoSession().getUserAccountDao().loadAll();
        if (userAccounts.size() > 0) {
            for (UserAccount userAccount : userAccounts) {
                if (userAccount.getIsLogin()) {
                    loginUser = userAccount;
                    ConstantValue.currentUser = userAccount;
                    viewModel.currentUserAccount.postValue(userAccount);
                    isLogin = true;
                    userName.setText(loginUser.getAccount());
                    if (ConstantValue.currentUser.getAvatar() != null && !"".equals(ConstantValue.currentUser.getAvatar())) {
                        Glide.with(this)
                                .load(MainAPI.MainBASE_URL + ConstantValue.currentUser.getAvatar())
                                .apply(AppConfig.getInstance().options)
                                .into(userAvatar);
                    } else {
                        Glide.with(this)
                                .load(R.mipmap.icon_user_default)
                                .apply(AppConfig.getInstance().options)
                                .into(userAvatar);
                    }
                    Map map = new HashMap<String, String>();
                    map.put("account", userAccount.getAccount());
                    map.put("token", AccountUtil.getUserToken());
                    mPresenter.getUserInfo(map);
                }
            }
        }
        claimQlc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLogin) {
                    if (ConstantValue.currentUser.getBindDate() == null || "".equals(ConstantValue.currentUser.getBindDate())) {
                        viewModel.isBind.postValue(false);
                    } else {
                        claimQlc.setDotViewVisible(View.INVISIBLE);
                        startActivity(new Intent(getActivity(), MyClaimActivity.class));
                    }
                } else {
                    startActivityForResult(new Intent(getActivity(), AccountActivity.class), 0);
                }
            }
        });
    }

    @Override
    protected void setupFragmentComponent() {
        DaggerMyComponent
                .builder()
                .appComponent(((AppConfig) getActivity().getApplication()).getApplicationComponent())
                .myModule(new MyModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void setPresenter(MyContract.MyContractPresenter presenter) {
        mPresenter = (MyPresenter) presenter;
    }

    @Override
    protected void initDataFromLocal() {

    }

    UserAccount loginUser;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isLogin = false;
        List<UserAccount> userAccounts = AppConfig.getInstance().getDaoSession().getUserAccountDao().loadAll();
        if (userAccounts.size() > 0) {
            for (UserAccount userAccount : userAccounts) {
                if (userAccount.getIsLogin()) {
                    loginUser = userAccount;
                    ConstantValue.currentUser = userAccount;
                    isLogin = true;
                    userName.setText(loginUser.getAccount());
                    if (ConstantValue.currentUser.getAvatar() != null && !"".equals(ConstantValue.currentUser.getAvatar())) {
                        Glide.with(this)
                                .load(MainAPI.MainBASE_URL + ConstantValue.currentUser.getAvatar())
                                .apply(AppConfig.getInstance().options)
                                .into(userAvatar);
                    } else {
                        Glide.with(this)
                                .load(R.mipmap.icon_user_default)
                                .apply(AppConfig.getInstance().options)
                                .into(userAvatar);
                    }
                    Map map = new HashMap<String, String>();
                    map.put("account", userAccount.getAccount());
                    map.put("token", AccountUtil.getUserToken());
                    mPresenter.getUserInfo(map);
                }
            }
        }
        if (!isLogin) {
            userName.setText(R.string.login_register);
            Glide.with(this)
                    .load(R.mipmap.icon_user_default)
                    .apply(AppConfig.getInstance().options)
                    .into(userAvatar);
        }
    }

    private void getCanClaimTotal() {
        HashMap<String, String> infoMap = new HashMap<>();
        infoMap.put("account", ConstantValue.currentUser.getAccount());
        infoMap.put("token", AccountUtil.getUserToken());
        infoMap.put("type", "REGISTER");
        infoMap.put("status", "NEW");
        mPresenter.getCanClaimTotal(infoMap);
    }

    private void getCanInviteClaimTotal() {
        HashMap<String, String> infoMap = new HashMap<>();
        infoMap.put("account", ConstantValue.currentUser.getAccount());
        infoMap.put("token", AccountUtil.getUserToken());
        infoMap.put("status", "NO_AWARD");
        mPresenter.getCanInviteClaimTotal(infoMap);
    }

    @Override
    public void showProgressDialog() {
        progressDialog.show();
    }

    @Override
    public void closeProgressDialog() {
        progressDialog.hide();
    }

    @Override
    public void setUsrInfo(UserInfo usrInfo) {
        if (usrInfo.getData().getBindDate() == null || "".equals(usrInfo.getData().getBindDate())) {
            viewModel.isBind.postValue(false);
        } else {
            getCanClaimTotal();
        }
        KLog.i("更新呢用户信息");
        ConstantValue.currentUser.setHoldingPhoto(usrInfo.getData().getHoldingPhoto());
        ConstantValue.currentUser.setFacePhoto(usrInfo.getData().getFacePhoto());
        ConstantValue.currentUser.setTotalInvite(usrInfo.getData().getTotalInvite());
        ConstantValue.currentUser.setVstatus(usrInfo.getData().getVStatus());
        ConstantValue.currentUser.setBindDate(usrInfo.getData().getBindDate());
        ConstantValue.currentUser.setInviteCode(usrInfo.getData().getNumber());
        ConstantValue.currentUser.setAvatar(usrInfo.getData().getHead());
        ConstantValue.currentUser.setUserName(usrInfo.getData().getNickname());
        ConstantValue.currentUser.setUserId(usrInfo.getData().getId());
        AppConfig.getInstance().getDaoSession().getUserAccountDao().update(ConstantValue.currentUser);
    }

    @Override
    public void setCanClaimTotal(RewardTotal rewardTotal) {
        getCanInviteClaimTotal();
        if (rewardTotal.getRewardTotal() != 0) {
            claimQlc.setDotViewVisible(View.VISIBLE);
        } else {
            claimQlc.setDotViewVisible(View.INVISIBLE);
        }
    }

    @Override
    public void setCanInviteClaimTotal(InviteTotal rewardTotal) {
        if (!"0".equals(rewardTotal.getInviteTotal())) {
            shareFriend.setDotViewVisible(View.VISIBLE);
        } else {
            shareFriend.setDotViewVisible(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @OnClick({R.id.user, R.id.cryptoWallet, R.id.shareFriend, R.id.joinCommunity, R.id.contactUs, R.id.settings, R.id.testView})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.user:
                isLogin = false;
                List<UserAccount> userAccounts = AppConfig.getInstance().getDaoSession().getUserAccountDao().loadAll();
                if (userAccounts.size() > 0) {
                    for (UserAccount userAccount : userAccounts) {
                        if (userAccount.getIsLogin()) {
                            loginUser = userAccount;
                            ConstantValue.currentUser = userAccount;
                            isLogin = true;
                            userName.setText(loginUser.getAccount());
                            startActivity(new Intent(getActivity(), PersonActivity.class));
                        }
                    }
                    if (!isLogin) {
                        startActivityForResult(new Intent(getActivity(), AccountActivity.class), 0);
                    }
                } else {
                    startActivityForResult(new Intent(getActivity(), AccountActivity.class), 0);
                }
                break;
            case R.id.cryptoWallet:
                EventBus.getDefault().post(new ChangeViewpager(1));
                break;
            case R.id.shareFriend:
                if (isLogin) {
                    shareFriend.setDotViewVisible(View.INVISIBLE);
                    startActivity(new Intent(getActivity(), InviteActivity.class));
                } else {
                    startActivityForResult(new Intent(getActivity(), AccountActivity.class), 0);
                }
                break;
            case R.id.joinCommunity:
                startActivity(new Intent(getActivity(), JoinCommunityActivity.class));
                break;
            case R.id.contactUs:
                break;
            case R.id.settings:
                startActivityForResult(new Intent(getActivity(), SettingsActivity.class), 0);
                break;
            case R.id.testView:
                startActivityForResult(new Intent(getActivity(), TestActivity.class), 0);
                break;
            default:
                break;
        }
    }

}