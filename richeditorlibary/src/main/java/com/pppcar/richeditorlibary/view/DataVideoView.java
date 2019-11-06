package com.pppcar.richeditorlibary.view;

import android.content.Context;
import android.util.AttributeSet;

import cn.jzvd.JzvdStd;

/**
 * 作者:  Logan on 2017/11/30.
 * 邮箱:  490636907@qq.com
 * 描述:  带数据的视频播放器
 */

public class DataVideoView extends JzvdStd {
    private String absolutePath;
    private String  url;

    public DataVideoView(Context context) {
        super(context);
    }

    public DataVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
