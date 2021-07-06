package cn.wu1588.dancer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.bean.UserBean;
import cn.wu1588.common.interfaces.CommonCallback;
import cn.wu1588.common.utils.SpUtil;
import cn.wu1588.dancer.R;
import cn.wu1588.main.activity.LoginActivity;
import cn.wu1588.main.activity.MainActivity;
import cn.wu1588.main.http.MainHttpUtil;

public class GuideActivity extends AppCompatActivity {

    ViewPager vp;

    int[] ids = {R.mipmap.guide1, R.mipmap.guide2, R.mipmap.guide3};
    TextView tvSkip;
    TextView tvExperience;
    private Context context;

    public static void lauch(Activity activity) {
        Intent intent = new Intent(activity, GuideActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        context = this;
        vp = findViewById(R.id.vp);
        tvExperience = findViewById(R.id.tv_experience);
        vp.setAdapter(new MyAdapter());
        initListner();
        SpUtil.getInstance().setBooleanValue(SpUtil.HAS_GUIDE, true);
    }

    private void initListner() {
        tvExperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toMain();
            }
        });
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == ids.length - 1) {
                    tvExperience.setVisibility(View.VISIBLE);
                } else {
                    tvExperience.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void toMain() {
        finish();
        String[] uidAndToken = SpUtil.getInstance().getMultiStringValue(
                new String[]{SpUtil.UID, SpUtil.TOKEN});
        final String uid = uidAndToken[0];
        final String token = uidAndToken[1];
        if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(token)) {
            MainHttpUtil.getBaseInfo(uid, token, new CommonCallback<UserBean>() {
                @Override
                public void callback(UserBean bean) {
                    if (bean != null) {
                        CommonAppConfig.getInstance().setLoginInfo(uid, token, false);
                        forwardMainActivity();
                    }
                }
            });
        } else {
            LoginActivity.forward();
        }
    }

    private void forwardMainActivity() {
        MainActivity.forward(context);
        finish();
    }

    class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return ids.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView iv = new ImageView(context);
            iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setImageResource(ids[position]);
            container.addView(iv);
            return iv;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }


}
