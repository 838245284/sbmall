package cn.wu1588.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.wu1588.main.R;
import cn.wu1588.main.views.MainMallViewHolder;

/**
 * Created by cxf on 2018/9/29.
 * 我的粉丝 TA的粉丝
 */

public class MallActivity extends AppCompatActivity {

    public static void forward(Context context) {
        Intent intent = new Intent(context, MallActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mall);
        TextView tv = findViewById(R.id.titleView);
        tv.setText("购物商城");
        ViewGroup containre = findViewById(R.id.container);
        MainMallViewHolder vh = new MainMallViewHolder(this, containre);
        vh.addToParent();
        vh.subscribeActivityLifeCycle();
        vh.loadData();
    }

    public void backClick(View v){
        finish();
    }
}
