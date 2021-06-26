package cn.wu1588.main.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.wu1588.common.interfaces.OnItemClickListener;
import cn.wu1588.main.R;
import cn.wu1588.video.bean.VideoWithAds;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class TabTeachFragment extends Fragment implements OnItemClickListener<VideoWithAds> {

    @Override
    public void onItemClick(VideoWithAds bean, int position) {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tabment, container, false);
    }
}