package com.pppcar.richeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.luck.picture.lib.engine.ImageEngine;
import com.luck.picture.lib.interfaces.OnCallbackListener;
import com.luck.picture.lib.utils.ActivityCompatHelper;

/**
 * Author by logan, Date on 2020/6/11.
 */
public class GlideEngine implements ImageEngine {

    /**
     * 加载图片
     *
     * @param context   上下文
     * @param url       资源url
     * @param imageView 图片承载控件
     */
    @Override
    public void loadImage(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .into(imageView);
    }

    /**
     * 加载指定url并返回bitmap
     *
     * @param context   上下文
     * @param url       资源url
     * @param maxWidth  资源最大加载尺寸
     * @param maxHeight 资源最大加载尺寸
     * @param call      回调接口
     */
    @Override
    public void loadImageBitmap(@NonNull Context context, @NonNull String url, int maxWidth, int maxHeight, OnCallbackListener<Bitmap> call) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context)
                .asBitmap()
                .override(maxWidth, maxHeight)
                .load(url)
                .into(new CustomTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (call != null) {
                            call.onCall(resource);
                        }
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        if (call != null) {
                            call.onCall(null);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                });
    }

    /**
     * 加载相册目录封面
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    @Override
    public void loadAlbumCover(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context)
                .asBitmap()
                .load(url)
                .override(180, 180)
                .centerCrop()
                .sizeMultiplier(0.5f)
                .placeholder(com.luck.picture.lib.R.drawable.ps_image_placeholder)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.
                                        create(context.getResources(), resource);
                        circularBitmapDrawable.setCornerRadius(8);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }


    /**
     * 加载图片列表图片
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    @Override
    public void loadGridImage(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .override(200, 200)
                .centerCrop()
                .placeholder(com.luck.picture.lib.R.drawable.ps_image_placeholder)
                .into(imageView);
    }

    @Override
    public void pauseRequests(Context context) {
        Glide.with(context).pauseRequests();
    }

    @Override
    public void resumeRequests(Context context) {
        Glide.with(context).resumeRequests();
    }

    private GlideEngine() {
    }

    private static GlideEngine instance;

    public static GlideEngine createGlideEngine() {
        if (null == instance) {
            synchronized (GlideEngine.class) {
                if (null == instance) {
                    instance = new GlideEngine();
                }
            }
        }
        return instance;
    }
}
