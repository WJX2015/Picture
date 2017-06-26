package com.example.lenovo_g50_70.picture;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by lenovo-G50-70 on 2017/6/24.
 */

public class ZoomImageView extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener,View.OnTouchListener{
    //是否第一次加载
    private boolean mOnce=false;
    //初始化缩放的值
    private float mInitScale;
    //双击放大的值
    private float mMidScale;
    //最大的放大值
    private float mMaxScale;
    //矩阵
    private Matrix mScaleMatrix;
    //捕获用户多点触控时缩放的比例
    private ScaleGestureDetector mDetector;
    //自由移动------------------------------
    //记录上一次多点触控的数量
    private int mLastPointerCount;
    //记录上一次多点触控的中心点
    private float mLastX;
    private float mLastY;

    private int mTouchSlop;
    private boolean isCanDrag;

    //图片是否需要边界检查
    private boolean isCheckLeftAndRight;
    private boolean isCheckTopAndBottom;

    public ZoomImageView(Context context) {
        this(context,null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mScaleMatrix=new Matrix();
        setScaleType(ScaleType.MATRIX);
        //上下文，接口对象
        mDetector =new ScaleGestureDetector(context,this);
        setOnTouchListener(this);

        mTouchSlop= ViewConfiguration.get(context).getScaledTouchSlop();
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

    //获取当前图片的缩放值
    public float getScale(){
        float[] values =new float[9];
        //拿到Matrix的值
        mScaleMatrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    @Override   //缩放区间mInitScale-maxScale
    public boolean onScale(ScaleGestureDetector detector) {
        //获取当前图片的缩放值
        float scale=getScale();
        //获取当前用户想缩放的值，大于1.0代表放大，小于1.0代表缩小
        float scaleFactor=detector.getScaleFactor();
        if(getDrawable()==null){
            return true;
        }
        //缩放范围的控制，如果当前缩放比例小于最大缩放比例,并且想放大.如果当前缩放比例大于最小缩放比例，并且想缩小
        if((scale<mMaxScale && scaleFactor>1.0f)||(scale>mInitScale && scaleFactor<1.0f)){
            //如果想缩小，缩小的比例也不能小于最小比例
            if(scale*scaleFactor<mInitScale){
                //当小于最小比例时，缩放最小比例
                scaleFactor=mInitScale/scale;
            }
            //同理，放大也不能超过放大的最大比例
            if(scale*scaleFactor>mMaxScale){
                scaleFactor=mMaxScale/scale;
            }
            //放大缩小平移等操作
            mScaleMatrix.postScale(scaleFactor,scaleFactor,detector.getFocusX(),detector.getFocusY());

            checkBorderAndCenterWhenScale();

            setImageMatrix(mScaleMatrix);
        }
        return true;
    }

    //获得图片放大缩小后的宽和高，以及left,top,right,bottom
    private RectF getMatrixRectF(){
        Matrix matrix=mScaleMatrix;
        RectF rectF =new RectF();
        Drawable drawable =getDrawable();
        if(drawable!=null){
            rectF.set(0,0, drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF;
    }

    //在缩放时，进行边界控制以及图片位置的控制
    private void checkBorderAndCenterWhenScale() {
        RectF rect=getMatrixRectF();

        // /XY偏移操作
        float deltaX=0;
        float deltaY=0;

        //拿到控件的宽高
        int width=getWidth();
        int height=getHeight();

        //缩放时水平方向的检测,防止出现白边
        if(rect.width()>=width){
            if(rect.left>0){//rect.left是一个屏幕坐标值，0为屏幕最左端
                //当图片左边与屏幕有空隙时，向左平移图片
                deltaX=-rect.left;
            }
            if(rect.right<width){//同理，width是屏幕最右端
                //当图片右边与屏幕有空隙时，向右平移图片
                deltaX=width-rect.right;
            }
        }

        //缩放时垂直方向的检测,防止出现白边
        if(rect.height()>=height){
            if(rect.top>0){
                deltaY=-rect.top;
            }
            if(rect.bottom<height){
                deltaY=height-rect.bottom;
            }
        }

        //如果宽度或者高度小于控件的宽或者高,则让其居中
        if(rect.width()<width){
            deltaX=width/2-rect.right+rect.width()/2;
        }
        if(rect.height()<height){
            deltaY=height/2-rect.bottom+rect.height()/2;
        }
        mScaleMatrix.postTranslate(deltaX,deltaY);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //把捕获的事件交给ScaleGestureDetector处理,在onScale执行
        mDetector.onTouchEvent(event);

        //图片自由移动代码的编写

        //中心点的XY坐标
        float x=0;
        float y=0;

        //获取多点触控的数量
        int pointCount=event.getPointerCount();

        for(int i=0;i< pointCount;i++){
            x+=event.getX(i);
            y+=event.getY(i);
        }

        x/=pointCount;
        y/=pointCount;

        if(mLastPointerCount!=pointCount){
            //如果触控的点发生了变化，重新记录中心点
            isCanDrag=false;
            mLastX=x;
            mLastY=y;
        }

        //记录最后一次多少个点触控
        mLastPointerCount=pointCount;

        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                //获取中心点改变后的偏移量
                float dx=x-mLastX;
                float dy=y-mLastY;

                //如果图片不能移动
                if(!isCanDrag){
                    isCanDrag=isMoveAction(dx,dy);
                }

                //如果图片能移动
                if(isCanDrag){
                    RectF rectF=getMatrixRectF();
                    if(getDrawable()!=null){

                        isCheckLeftAndRight=true;
                        isCheckTopAndBottom=true;

                        //如果图片宽度小于控件宽度，不允许横向移动
                        if(rectF.width()<getWidth()){
                            isCheckLeftAndRight=false;
                            dx=0;
                        }

                        //如果图片宽度小于控件高度，不允许横向移动
                        if(rectF.height()<getHeight()){
                            isCheckTopAndBottom=false;
                            dy=0;
                        }

                        //图片可以移动
                        mScaleMatrix.postTranslate(dx,dy);
                        //边界检查,以防拖动后边界出现白边
                        checkBorderWhenTranslate();
                        setImageMatrix(mScaleMatrix);
                    }
                }

                //记录上一次的中心点XY
                mLastX=x;
                mLastY=y;
                break;
            case MotionEvent.ACTION_UP:
                //手指抬起后，没有触控的点
                mLastPointerCount=0;
                break;
        }

        return true;
    }

    //当移动时进行移动检查
    private void checkBorderWhenTranslate() {
        RectF rectF =getMatrixRectF();

        float deltaX=0;
        float deltaY=0;

        int width=getWidth();
        int height =getHeight();

        //顶部出现白边，并需要检查边界
        if(rectF.top>0 && isCheckTopAndBottom){
            deltaY=-rectF.top;
        }

        //底部出现白边，并需要检查边界
        if(rectF.bottom<height && isCheckTopAndBottom){
            deltaY=height-rectF.bottom;
        }

        //左部出现白边，并需要检查边界
        if(rectF.left>0 && isCheckLeftAndRight){
            deltaX=-rectF.left;
        }

        //右部出现白边，并需要检查边界
        if(rectF.right<width && isCheckLeftAndRight){
            deltaX=width-rectF.right;
        }

        mScaleMatrix.postTranslate(deltaX,deltaY);
    }

    /**
     * 判断是否是move
     * @param dx
     * @param dy
     * @return
     */
    private boolean isMoveAction(float dx,float dy){
        return Math.sqrt(dx*dx+dy*dy)>mTouchSlop;
    }
}
