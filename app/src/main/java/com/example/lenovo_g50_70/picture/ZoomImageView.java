package com.example.lenovo_g50_70.picture;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by lenovo-G50-70 on 2017/6/24.
 */

public class ZoomImageView extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener{
    //是否第一次加载
    private boolean mOnce=false;
    //初始化缩放的值
    private float mInitScale;
    //双击放大的值
    private float mMidScale;
    //最大的放大值
    private float mMaxScale;

    private Matrix mScaleMatrix;

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
        mScaleMatrix=new Matrix();
        super.setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //注册接口
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //反注册接口
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
            //得到初始化时缩放的比例
            mInitScale=scale;
            mMaxScale=mInitScale*4;
            mMidScale=mInitScale*2;

            //将图片移动至控件的中心
            int dx=getWidth()/2-dw/2;
            int dy=getHeight()/2-dh/2;
            //平移
            mScaleMatrix.postTranslate(dx,dy);
            //缩放
            mScaleMatrix.postScale(mInitScale,mInitScale,width/2,height/2);
            //设置Maatrix
            setImageMatrix(mScaleMatrix);
           mOnce=true;
        }
    }
}
