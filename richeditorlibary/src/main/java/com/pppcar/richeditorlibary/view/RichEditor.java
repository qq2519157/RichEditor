package com.pppcar.richeditorlibary.view;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.pppcar.richeditorlibary.R;
import com.pppcar.richeditorlibary.activity.ImageRotateAct;
import com.pppcar.richeditorlibary.utils.SDCardUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import cn.jzvd.Jzvd;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * 作者:  Logan on 2017/11/30.
 * 邮箱:  490636907@qq.com
 * 描述:  可编辑富文本
 */
@SuppressLint({"InflateParams","SetTextI18n"})
@SuppressWarnings("unused")
public class RichEditor extends ScrollView {

    private static final int EDIT_PADDING = 10; // edittext常规padding是10dp
    private final OnClickListener btnImgClickListener;
    private static final int IMG_TEXT = 0;
    private static final int VIDEO = 1;
    private int viewTagIndex = 1; // 新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
    private DragLinearLayout allLayout; // 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
    private LayoutInflater inflater;
    private OnKeyListener keyListener; // 所有EditText的软键盘监听器
    private OnClickListener btnListener; // 图片右上角红叉按钮监听器
    private OnClickListener btnVideoListener; // 视频右上角红叉按钮监听器
    private OnFocusChangeListener focusListener; // 所有EditText的焦点监听listener
    private EditText lastFocusEdit; // 最近被聚焦的EditText
    private int disappearingIndex = 0;
    private Context mContext;
    private OnClickListener btnRotateListener;
    public static final int ROTATE_IMAGE = 101;
    public static final int VIDEO_REQUEST = 102;
    private boolean isMenuShow;
    private int type;


    public RichEditor(Context context) {
        this(context, null);
    }

    public RichEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflater = LayoutInflater.from(context);
        this.mContext = context;
        // 1. 初始化allLayout
//        allLayout = new LinearLayout(context);
        allLayout = new DragLinearLayout(context);
        allLayout.setOrientation(LinearLayout.VERTICAL);
        allLayout.setContainerScrollView(this);
        //子控件拖拽监听
        allLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View firstView, int firstPosition, View secondView, int secondPosition) {
                //移除FirstView
                allLayout.removeDragView(firstView);
                //移除SecondView
                if (secondView instanceof RelativeLayout) {
                    if ("image".equals(secondView.getTag(R.id.richEditor))) {
                        allLayout.removeDragView(secondView);
                    } else {
                        allLayout.removeView(secondView);
                    }
                } else {
                    allLayout.removeView(secondView);
                }
                if (firstPosition >= secondPosition) {
                    //底下的View往上拖,先添加firstView
                    allLayout.addDragView(firstView, firstView.findViewById(R.id.move), secondPosition);
                    //添加SecondView
                    if (secondView instanceof RelativeLayout) {
                        if ("image".equals(secondView.getTag(R.id.richEditor))) {
                            allLayout.addDragView(secondView, secondView.findViewById(R.id.move), firstPosition);
                        } else {
//                            allLayout.addView(secondView,firstPosition);
                            //findViewById(R.id.video_move)
//                            allLayout.addDragView(secondView,firstPosition);
                            allLayout.addDragView(secondView, secondView.findViewById(R.id.video_move), firstPosition);
                        }
                    } else {
//                        allLayout.addView(secondView,firstPosition);
                        allLayout.addDragView(secondView, firstPosition);
                    }
                } else {
                    //上面往底下拖,先添加SecondView
                    if (secondView instanceof RelativeLayout) {
                        if ("image".equals(secondView.getTag(R.id.richEditor))) {
                            allLayout.addDragView(secondView, secondView.findViewById(R.id.move), firstPosition);
                        } else {
                            allLayout.addDragView(secondView, secondView.findViewById(R.id.video_move), firstPosition);
                        }
                    } else {
                        allLayout.addDragView(secondView, firstPosition);
                    }
                    allLayout.addDragView(firstView, firstView.findViewById(R.id.move), secondPosition);
                }


            }
        });
        //allLayout.setBackgroundColor(Color.WHITE);
        setupLayoutTransitions();
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        allLayout.setPadding(50, 15, 50, 15);//设置间距，防止生成图片时文字太靠边，不能用margin，否则有黑边
        addView(allLayout, layoutParams);

        // 2. 初始化键盘退格监听
        // 主要用来处理点击回删按钮时，view的一些列合并操作
        keyListener = new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    EditText edit = (EditText) v;
                    onBackspacePress(edit);
                }
                return false;
            }
        };

        // 3. 图片叉掉处理
        btnListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                FrameLayout view = (FrameLayout) v.getParent();
                RelativeLayout parentView = (RelativeLayout) view.getParent();
                onImageCloseClick(parentView);
            }
        };
        btnRotateListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                //图片旋转
                FrameLayout view = (FrameLayout) v.getParent();
                RelativeLayout parentView = (RelativeLayout) view.getParent();
                goToRotateAct(parentView);
            }
        };
        btnImgClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击图片
                RelativeLayout parentView = (RelativeLayout) v.getParent();
                ImageView openMenu = parentView.findViewById(R.id.iv_open);
                ImageView delete = parentView.findViewById(R.id.iv_delete);
                ImageView rotate = parentView.findViewById(R.id.iv_rotate);
                hideMenu(openMenu, delete, rotate);
                goToRotateAct(parentView);
            }
        };
        btnVideoListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout parentView = (RelativeLayout) v.getParent();
                onVideoCloseClick(parentView);
            }
        };
        focusListener = new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lastFocusEdit = (EditText) v;
                }
            }
        };

        LinearLayout.LayoutParams firstEditParam = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        //editNormalPadding = dip2px(EDIT_PADDING);
        final EditText firstEdit = createEditText("请输入正文", dip2px(context, EDIT_PADDING));
        firstEdit.setHintTextColor(getResources().getColor(R.color.main_bg_gray_));
        allLayout.addDragView(firstEdit, firstEditParam);
        lastFocusEdit = firstEdit;
    }

    private void goToRotateAct(RelativeLayout parentView) {
        int index = allLayout.indexOfChild(parentView);
        DataImageView childAt = (DataImageView) parentView.getChildAt(0);
        String path = childAt.getAbsolutePath();
        Intent intent = new Intent(mContext, ImageRotateAct.class);
        intent.putExtra("imagePath", path);
        intent.putExtra("index", index);
        ((Activity) mContext).startActivityForResult(intent, ROTATE_IMAGE);
    }

    /**
     * 初始化transition动画
     */
    private void setupLayoutTransitions() {
        // 只在图片View添加或remove时，触发transition动画
        LayoutTransition transitioner = new LayoutTransition();
        allLayout.setLayoutTransition(transitioner);
        transitioner.addTransitionListener(new LayoutTransition.TransitionListener() {

            @Override
            public void startTransition(LayoutTransition transition,
                                        ViewGroup container, View view, int transitionType) {

            }

            @Override
            public void endTransition(LayoutTransition transition,
                                      ViewGroup container, View view, int transitionType) {
                transition.isRunning();
            }
        });
        transitioner.setDuration(300);
    }

    public int dip2px(Context context, float dipValue) {
        float m = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

    /**
     * 处理软键盘backSpace回退事件
     *
     * @param editTxt 光标所在的文本输入框
     */
    private void onBackspacePress(EditText editTxt) {
        int startSelection = editTxt.getSelectionStart();
        // 只有在光标已经顶到文本输入框的最前方，在判定是否删除之前的图片，或两个View合并
        if (startSelection == 0) {
            int editIndex = allLayout.indexOfChild(editTxt);
            View preView = allLayout.getChildAt(editIndex - 1); // 如果editIndex-1<0,
            // 则返回的是null
            if (null != preView) {
                if (preView instanceof RelativeLayout) {
                    if ("image".equals(preView.getTag(R.id.richEditor))) {
                        // 光标EditText的上一个view对应的是图片
                        onImageCloseClick(preView);
                    } else {
                        // 光标EditText的上一个view对应的是视频
                        onVideoCloseClick(preView);
                    }
                } else if (preView instanceof EditText) {
                    // 光标EditText的上一个view对应的还是文本框EditText
                    String str1 = editTxt.getText().toString();
                    EditText preEdit = (EditText) preView;
                    String str2 = preEdit.getText().toString();

//                    allLayout.removeView(editTxt);
                    allLayout.removeDragView(editTxt);

                    // 文本合并
                    preEdit.setText(str2 + str1);
                    preEdit.requestFocus();
                    preEdit.setSelection(str2.length(), str2.length());
                    lastFocusEdit = preEdit;
                }
            }
        }
    }

    /**
     * 处理图片叉掉的点击事件
     *
     * @param view 整个image对应的relativeLayout view
     *  删除类型 0代表backspace删除 1代表按红叉按钮删除
     */
    private void onImageCloseClick(View view) {
        disappearingIndex = allLayout.indexOfChild(view);
        //删除文件夹里的图片
        List<EditData> dataList = buildEditData();
        EditData editData = dataList.get(disappearingIndex);
        //Log.i("", "editData: "+editData);
        if (editData.imagePath != null) {
            SDCardUtil.deleteFile(editData.imagePath);
        }
        allLayout.removeView(view);
//        allLayout.removeDragView(view);
    }

    /**
     * 处理视频叉掉的点击事件
     *
     * @param view 整个video对应的relativeLayout view
     *  删除类型 0代表backspace删除 1代表按红叉按钮删除
     */
    private void onVideoCloseClick(View view) {
        disappearingIndex = allLayout.indexOfChild(view);
        //删除文件夹里的图片
        List<EditData> dataList = buildEditData();
        EditData editData = dataList.get(disappearingIndex);
        allLayout.removeView(view);
        type = IMG_TEXT;

    }

    public void clearAllLayout() {
        allLayout.removeAllViews();
    }

    public int getLastIndex() {
        return allLayout.getChildCount();
    }

    /**
     * 生成文本输入框
     */
    public EditText createEditText(String hint, int paddingTop) {
        final EditText editText = (EditText) inflater.inflate(R.layout.rich_edittext, null);
        editText.setOnKeyListener(keyListener);
        editText.setTag(viewTagIndex++);
        //
        int editNormalPadding = 0;
        editText.setPadding(editNormalPadding, paddingTop, editNormalPadding, paddingTop);
        editText.setHint(hint);
        editText.setOnFocusChangeListener(focusListener);
        return editText;
    }

    /**
     * 生成图片View
     */
    private RelativeLayout createImageLayout() {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.edit_imageview, null);
        layout.setTag(viewTagIndex++);
        final ImageView openMenu = layout.findViewById(R.id.iv_open);
        final ImageView delete = layout.findViewById(R.id.iv_delete);
        final ImageView rotate = layout.findViewById(R.id.iv_rotate);
        delete.setTag(layout.getTag());
        delete.setOnClickListener(btnListener);
        rotate.setOnClickListener(btnRotateListener);
        openMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMenuShow) {
                    openMenu.setImageResource(R.mipmap.open);

                    hideMenu(openMenu, delete, rotate);

                } else {
                    openMenu.setImageResource(R.mipmap.close);
                    delete.setVisibility(VISIBLE);
                    rotate.setVisibility(VISIBLE);
                    openMenu(openMenu, delete, rotate);

                }
            }
        });
        return layout;
    }


    private void openMenu(ImageView openMenu, final ImageView delete, final ImageView rotate) {
        isMenuShow = true;
        int distance1 = 150;
        int x = (int) openMenu.getX();
        int y = (int) openMenu.getY();
        ValueAnimator v1 = ValueAnimator.ofInt(x, x - distance1);
        v1.setDuration(300);
        v1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int l = (int) animation.getAnimatedValue();
                int t = (int) delete.getY();
                int r = delete.getWidth() + l;
                int b = delete.getHeight() + t;
                delete.layout(l, t, r, b);
            }
        });
        int distance2 = 300;
        ValueAnimator v2 = ValueAnimator.ofInt(x, x - distance2);
        v2.setDuration(300).addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int l = (int) animation.getAnimatedValue();
                int t = (int) rotate.getY();
                int r = rotate.getWidth() + l;
                int b = rotate.getHeight() + t;
                rotate.layout(l, t, r, b);
            }
        });


        v1.start();
        v2.start();
    }

    public void hideMenu(ImageView openMenu, final ImageView delete, final ImageView rotate) {
        isMenuShow = false;
        int x = (int) delete.getX();
        ValueAnimator v1 = ValueAnimator.ofInt(x, (int) openMenu.getX());
        v1.setDuration(300);
        v1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int l = (int) animation.getAnimatedValue();
                int t = (int) delete.getY();
                int r = delete.getWidth() + l;
                int b = delete.getHeight() + t;
                delete.layout(l, t, r, b);
            }
        });
        x = (int) rotate.getX();
        ValueAnimator v2 = ValueAnimator.ofInt(x, (int) openMenu.getX());
        v2.setDuration(300).addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int l = (int) animation.getAnimatedValue();
                int t = (int) rotate.getY();
                int r = rotate.getWidth() + l;
                int b = rotate.getHeight() + t;
                rotate.layout(l, t, r, b);
            }
        });

        v1.start();
        v2.start();
        v1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                delete.setVisibility(GONE);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        v2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                rotate.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }


    /**
     * 生成视频View
     */
    private RelativeLayout createVideoLayout() {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.edit_videoview, null);
        //不允许视频拖拽
        layout.setOnDragListener(null);
        layout.setTag(viewTagIndex++);
        View closeView = layout.findViewById(R.id.video_close);
        //closeView.setVisibility(GONE);
        closeView.setTag(layout.getTag());
        closeView.setOnClickListener(btnVideoListener);
        return layout;
    }


    /**
     * 根据绝对路径添加view
     *
     * @param imagePath 绝对路径
     */
    public void insertImage(String imagePath, int width) {
        Bitmap bmp = getScaledBitmap(imagePath, width);
        insertImage(bmp, imagePath);
    }


    /**
     * 插入一张图片
     */
    public void insertImage(Bitmap bitmap, String imagePath) {
        String lastEditStr = lastFocusEdit.getText().toString();
        int cursorIndex = lastFocusEdit.getSelectionStart();
        String editStr1 = lastEditStr.substring(0, cursorIndex).trim();
        int lastEditIndex = allLayout.indexOfChild(lastFocusEdit);

        if (lastEditStr.length() == 0 || editStr1.length() == 0) {
            if (lastEditIndex != 0 && allLayout.getChildAt(lastEditIndex - 1) instanceof RelativeLayout) {
                addEditTextAtIndex(lastEditIndex, "");
                addImageViewAtIndex(lastEditIndex + 1, imagePath);
            } else {
                // 如果EditText为空，或者光标已经顶在了editText的最前面，则直接插入图片，并且EditText下移即可
                addImageViewAtIndex(lastEditIndex, imagePath);
            }
        } else {
            // 如果EditText非空且光标不在最顶端，则需要添加新的imageView和EditText
            lastFocusEdit.setText(editStr1);
            String editStr2 = lastEditStr.substring(cursorIndex).trim();
            if (editStr2.length() == 0) {
                editStr2 = " ";
            }
            if (allLayout.getChildCount() - 1 == lastEditIndex) {
                addEditTextAtIndex(lastEditIndex + 1, editStr2);
            }

            addImageViewAtIndex(lastEditIndex + 1, imagePath);
            lastFocusEdit.requestFocus();
            lastFocusEdit.setSelection(editStr1.length(), editStr1.length());//TODO
        }
        hideKeyBoard();
    }

    /**
     * 插入一张图片
     */
    public void insertImage(String imagePath) {
        String lastEditStr = lastFocusEdit.getText().toString();
        int cursorIndex = lastFocusEdit.getSelectionStart();
        String editStr1 = lastEditStr.substring(0, cursorIndex).trim();
        int lastEditIndex = allLayout.indexOfChild(lastFocusEdit);

        if (lastEditStr.length() == 0 || editStr1.length() == 0) {
            if (lastEditIndex != 0 && allLayout.getChildAt(lastEditIndex - 1) instanceof RelativeLayout) {
                addEditTextAtIndex(lastEditIndex, "");
                addImageViewAtIndex(lastEditIndex + 1, imagePath);
            } else {
                // 如果EditText为空，或者光标已经顶在了editText的最前面，则直接插入图片，并且EditText下移即可
                addImageViewAtIndex(lastEditIndex, imagePath);
            }
        } else {
            // 如果EditText非空且光标不在最顶端，则需要添加新的imageView和EditText
            lastFocusEdit.setText(editStr1);
            String editStr2 = lastEditStr.substring(cursorIndex).trim();
            if (editStr2.length() == 0) {
                editStr2 = " ";
            }
            if (allLayout.getChildCount() - 1 == lastEditIndex) {
                addEditTextAtIndex(lastEditIndex + 1, editStr2);
            }

            addImageViewAtIndex(lastEditIndex + 1, imagePath);
            lastFocusEdit.requestFocus();
            lastFocusEdit.setSelection(editStr1.length(), editStr1.length());//TODO
        }
        hideKeyBoard();
    }

    /**
     * 插入一个视频
     */
    public void insertVideo(String videoPath, String firstImgUrl) {
        String lastEditStr = lastFocusEdit.getText().toString();
        int cursorIndex = lastFocusEdit.getSelectionStart();
        String editStr1 = lastEditStr.substring(0, cursorIndex).trim();
        int lastEditIndex = allLayout.indexOfChild(lastFocusEdit);

        if (lastEditStr.length() == 0 || editStr1.length() == 0) {
            // 如果EditText为空，或者光标已经顶在了editText的最前面，则直接插入图片，并且EditText下移即可
            addVideoViewAtIndex(lastEditIndex, videoPath, firstImgUrl);
        } else {
            // 如果EditText非空且光标不在最顶端，则需要添加新的videoView和EditText
            lastFocusEdit.setText(editStr1);
            String editStr2 = lastEditStr.substring(cursorIndex).trim();
            if (editStr2.length() == 0) {
                editStr2 = " ";
            }
            if (allLayout.getChildCount() - 1 == lastEditIndex) {
                addEditTextAtIndex(lastEditIndex + 1, editStr2);
            }

            addVideoViewAtIndex(lastEditIndex + 1, videoPath, firstImgUrl);
            lastFocusEdit.requestFocus();
            lastFocusEdit.setSelection(editStr1.length(), editStr1.length());//TODO
        }
        hideKeyBoard();
    }

    /**
     * 隐藏小键盘
     */
    public void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(lastFocusEdit.getWindowToken(), 0);
        }
    }

    /**
     * 在特定位置插入EditText
     *
     * @param index   位置
     * @param editStr EditText显示的文字
     */
    public void addEditTextAtIndex(final int index, CharSequence editStr) {
        EditText editText2 = createEditText("", EDIT_PADDING);
        editText2.setHint(R.string.say_somthing);
        editText2.setText(editStr);
        editText2.setOnFocusChangeListener(focusListener);
        allLayout.addDragView(editText2, index);
    }

    /**
     * 插入EditText
     *
     * @param editStr EditText显示的文字
     */
    public void addEditText(CharSequence editStr) {
        EditText editText2 = createEditText("", EDIT_PADDING);
        editText2.setText(editStr);
        editText2.setOnFocusChangeListener(focusListener);
        allLayout.addDragView(editText2);
    }


    /**
     * 清除特定位置的ImageView
     */
    public void removeImageViewAtIndex(int index) {
        View childAt = allLayout.getChildAt(index);
        onImageCloseClick(childAt);

    }

    /**
     * 在特定位置添加ImageView
     */
    public void addImageViewAtIndex(final int index, String imagePath) {
        final RelativeLayout imageLayout = createImageLayout();
        imageLayout.setTag(R.id.richEditor, "image");
        DataImageView imageView = imageLayout.findViewById(R.id.edit_imageView);
        ImageView move = imageLayout.findViewById(R.id.move);
        imageView.setOnClickListener(btnImgClickListener);
        DrawableCrossFadeFactory factory =
                new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
        Glide.with(getContext()).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).transition(withCrossFade(factory)).centerCrop().into(imageView);
        imageView.setAbsolutePath(imagePath);//保留这句，后面保存数据会用
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);//裁剪剧中

        // 调整imageView的高度，根据宽度来调整高度
//        Bitmap bmp = BitmapFactory.decodeFile(imagePath);
//        int imageHeight = 300;
        int imageHeight = allLayout.getWidth() * 3 / 5;
        /*if (bmp != null) {
//            imageHeight = allLayout.getWidth() * bmp.getHeight() / bmp.getWidth();
            imageHeight = allLayout.getWidth() * 3 / 5;
            bmp.recycle();
        }*/
        // 调整图片高度，这里是否有必要，如果出现微博长图，可能会很难看
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, imageHeight);//设置图片固定高度
        lp.bottomMargin = 10;
        imageView.setLayoutParams(lp);
//        allLayout.addView(imageLayout, index);
        allLayout.addDragView(imageLayout, move, index);
    }


    /**
     * 在特定位置添加VideoView
     */
    public void addVideoViewAtIndex(final int index, String videoPath, String firstImgUrl) {
        final RelativeLayout videoLayout = createVideoLayout();
        videoLayout.setTag(R.id.richEditor, "video");
//        DataVideoView videoView = (DataVideoView) videoLayout.findViewById(R.id.edit_videoView);
        DataVideoView videoView = videoLayout.findViewById(R.id.edit_videoView);
        ImageView videoMove = videoLayout.findViewById(R.id.video_move);
        /*videoView.setVideoPath(videoPath);
        videoView.setMediaController(new MediaController(mContext));*/
        videoView.setUp(videoPath, "", Jzvd.SCREEN_NORMAL);
        Glide.with(mContext).load(firstImgUrl).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(videoView.thumbImageView);
        videoView.setAbsolutePath(videoPath);//保留这句，后面保存数据会用

        // 调整imageView的高度，根据宽度来调整高度
        Bitmap bmp = BitmapFactory.decodeFile(videoPath);
        int imageHeight = allLayout.getWidth() * 9 / 16;
        //16:9
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, imageHeight);//设置视频固定高度
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        lp.bottomMargin = 10;
        videoView.setLayoutParams(lp);

//        allLayout.addView(videoLayout, index);
//        allLayout.addDragView(videoLayout, index);
        allLayout.addDragView(videoLayout, videoMove, index);
        type = VIDEO;
    }

    /**
     * 获取当前的类型是否是视频类型
     */
    public boolean isVideoType() {
        return type == VIDEO;
    }

    /**
     * 根据view的宽度，动态缩放bitmap尺寸
     *
     * @param width view的宽度
     */
    public Bitmap getScaledBitmap(String filePath, int width) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int sampleSize = options.outWidth > width ? options.outWidth / width
                + 1 : 1;
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(filePath, options);
    }


    /**
     * 对外提供的接口, 生成编辑数据上传
     */
    public List<EditData> buildEditData() {
        List<EditData> dataList = new ArrayList<>();
        int num = allLayout.getChildCount();
        for (int index = 0; index < num; index++) {
            View itemView = allLayout.getChildAt(index);
            EditData itemData = new EditData();
            if (itemView instanceof EditText) {
                EditText item = (EditText) itemView;
                String trim = item.getText().toString().trim();
                if (TextUtils.isEmpty(trim)) {
                    continue;
                }
                itemData.inputStr = item.getText().toString();
            } else if (itemView instanceof RelativeLayout) {
                if ("image".equals(itemView.getTag(R.id.richEditor))) {
                    DataImageView item = itemView.findViewById(R.id.edit_imageView);
                    itemData.imagePath = item.getAbsolutePath();
                }

                if ("video".equals(itemView.getTag(R.id.richEditor))) {
                    DataVideoView item = itemView.findViewById(R.id.edit_videoView);
                    itemData.videoPath = item.getAbsolutePath();
                }

            }
            dataList.add(itemData);
        }

        return dataList;
    }

    public class EditData {
        String inputStr;
        String imagePath;
        String videoPath;

        @NonNull
        @Override
        public String toString() {
            return "EditData{" +
                    "inputStr='" + inputStr + '\'' +
                    ", imagePath='" + imagePath + '\'' +
                    ", videoPath='" + videoPath + '\'' +
                    '}';
        }
    }
}
