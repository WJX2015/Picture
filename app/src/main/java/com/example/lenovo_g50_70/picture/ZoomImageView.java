package com.example.lenovo_g50_70.picture;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by lenovo-G50-70 on 2017/6/24.
 */

public class ZoomImageView extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener{

    private boolean mOnce=false;//是否第一次加载

    public ZoomImageView(Context context) {
        this(context,null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override //全局布局加载完成后调用此方法
    public void onGlobalLayout() {
        //获取ImageView加载完成的图片
        if(!mOnce){
            //得到控件的宽高
            int width=getWidth();
            int height=getHeight();
            //得到我们的图片，以及宽高
            Drawable drawable=getDrawable();
            if(drawable==null){
                return;
            }
            int dw=drawable.getIntrinsicWidth();
            int dh=drawable.getIntrinsicHeight();

            float scale=1.0f;

            if(dw>width && dh<height){
                //图片宽度大于控件宽度，高度小于控件高度，将图片缩小
                scale=width*1.0f/dw;
            }else if(dw<width && dh>height){
                //图片宽度小于控件宽度，高度大于控件高度，将图片缩小
                scale=height*1.0f/dh;
            }else{
                //图片宽高都大于或小于控件的宽高，以此进行放大或缩小
                scale=Math.min(width*1.0f/dw,height*1.0f/dh);
            }
            mOnce=true;
        }
    }
}
