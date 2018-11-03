package ffo.com.surfaceviewanimation;

/**
 * @author: huchunhua
 * @time: 2018/11/2
 * @package: ffo.com.surfaceviewanimation
 * @project: SurfaceViewAnimation
 * @mail: huachunhu@qq.com
 * @describe: 一句话描述
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * @author: huchunhua
 * @time: 2018/11/2
 * @package: PACKAGE_NAME
 * @project: SurfaceViewAnimation
 * @mail: huachunhu@qq.com
 * @describe: 一句话描述
 */
public class SurfaceViewAnimation extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private String TAG = "SurfaceViewAnimation";

    private SurfaceHolder mSurfaceHolder;

    private boolean mIsThreadRunning = true; // 线程运行开关
    public static boolean mIsDestroy = false;// 是否已经销毁

    private int[] mBitmapResourceIds;// 用于播放动画的图片资源id数组
    private ArrayList<String> mBitmapResourcePaths;// 用于播放动画的图片资源path数组
    private int totalCount;//资源总数
    private Canvas mCanvas;
    private Bitmap mBitmap;// 显示的图片

    private int mCurrentIndext;// 当前动画播放的位置
    private int mGapTime = 50;// 每帧动画持续存在的时间
    private boolean mIsRepeat = false;

    private OnFrameFinishedListener mOnFrameFinishedListener;// 动画监听事件
    private Thread thread;

    Rect mSrcRect, mDestRect;

    public SurfaceViewAnimation(Context context) {
        this(context, null);
        initView();
    }

    public SurfaceViewAnimation(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public SurfaceViewAnimation(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initView();

    }

    private void initView() {

        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_HARDWARE);

        //设置透明背景
        //setZOrderOnTop(true) 必须在setFormat方法之前，不然png的透明效果不生效
        setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

        mBitmapResourceIds = new int[1];
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        destroy();
    }

    /**
     * 制图方法
     */
    private void drawView() {
        // 无资源文件退出
        if (mBitmapResourceIds == null && mBitmapResourcePaths == null) {
            Log.e("frameview", "the bitmapsrcIDs is null");
            mIsThreadRunning = false;

            return;
        }

        Log.d(TAG, "drawView: mCurrentIndext=" + mCurrentIndext);
        Log.d(TAG, "drawView: Thread id = " + Thread.currentThread().getId());

        //防止是获取不到Canvas
        SurfaceHolder surfaceHolder = mSurfaceHolder;
        // 锁定画布
        synchronized (surfaceHolder) {
            if (surfaceHolder != null) {
                mCanvas = surfaceHolder.lockCanvas();
                Log.d(TAG, "drawView: mCanvas= " + mCanvas);
                if (mCanvas == null) {
                    return;
                }
            }
            try {

                if (surfaceHolder != null && mCanvas != null) {

                    synchronized (mBitmapResourceIds) {
                        if (mBitmapResourceIds != null && mBitmapResourceIds.length > 0) {
                            mBitmap = BitmapUtil.decodeSampledBitmapFromResource(getResources(), mBitmapResourceIds[mCurrentIndext], getWidth(), getHeight());
                        } else if (mBitmapResourcePaths != null && mBitmapResourcePaths.size() > 0) {
                            mBitmap = BitmapFactory.decodeFile(mBitmapResourcePaths.get(mCurrentIndext));

                        }
                    }
                    mBitmap.setHasAlpha(true);

                    if (mBitmap == null) {
                        return;
                    }

                    Paint paint = new Paint();
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                    mCanvas.drawPaint(paint);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                    paint.setAntiAlias(true);
                    paint.setStyle(Paint.Style.STROKE);

                    mSrcRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
                    mDestRect = new Rect(0, 0, getWidth(), getHeight());
                    mCanvas.drawBitmap(mBitmap, mSrcRect, mDestRect, paint);

                    // 播放到最后一张图片
                    if (mCurrentIndext == totalCount - 1) {
                        //TODO 设置重复播放
                        //播放到最后一张，当前index置零
                        mCurrentIndext = 0;
                    }

                }

            } catch (Exception e) {
                Log.d(TAG, "drawView: e =" + e.toString());
                e.printStackTrace();
            } finally {

                mCurrentIndext++;

                if (mCurrentIndext >= totalCount) {
                    mCurrentIndext = 0;
                }
                if (mCanvas != null) {
                    // 将画布解锁并显示在屏幕上
                    if (getHolder() != null) {
                        surfaceHolder.unlockCanvasAndPost(mCanvas);
                    }
                }

                if (mBitmap != null) {
                    // 收回图片
                    mBitmap.recycle();
                }
            }
        }
    }

    @Override
    public void run() {
        if (mOnFrameFinishedListener != null) {
            mOnFrameFinishedListener.onStart();
        }
        Log.d(TAG, "run: mIsThreadRunning=" + mIsThreadRunning);
        // 每隔150ms刷新屏幕
        while (mIsThreadRunning) {
            drawView();
            try {
                Thread.sleep(mGapTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mOnFrameFinishedListener != null) {
            mOnFrameFinishedListener.onStop();
        }
    }

    /**
     * 开始动画
     */
    public void start() {
        if (!mIsDestroy) {
            mCurrentIndext = 0;
            mIsThreadRunning = true;
            thread = new Thread(this);
            thread.start();
        } else {
            // 如果SurfaceHolder已经销毁抛出该异常
            try {
                throw new Exception("IllegalArgumentException:Are you sure the SurfaceHolder is not destroyed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 防止内存泄漏
     */
    private void destroy() {
        //当surfaceView销毁时, 停止线程的运行. 避免surfaceView销毁了线程还在运行而报错.
        mIsThreadRunning = false;
        try {
            Thread.sleep(mGapTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mIsDestroy = true;

        thread.interrupt();
        thread = null;

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        if (mSurfaceHolder != null) {
            mSurfaceHolder.addCallback(null);
        }

        if (mOnFrameFinishedListener != null) {
            mOnFrameFinishedListener = null;
        }
    }

    /**
     * 设置动画播放素材的id
     *
     * @param bitmapResourceIds 图片资源id
     */
    public void setBitmapResoursID(int[] bitmapResourceIds) {
        synchronized (mBitmapResourceIds) {
            this.mBitmapResourceIds = bitmapResourceIds;
            totalCount = bitmapResourceIds.length;
        }
    }

    /**
     * 设置动画播放素材的路径
     *
     * @param bitmapResourcePaths
     */
    public void setmBitmapResourcePath(ArrayList bitmapResourcePaths) {
        this.mBitmapResourcePaths = bitmapResourcePaths;
        totalCount = bitmapResourcePaths.size();
    }

    /**
     * 设置每帧时间
     */
    public void setGapTime(int gapTime) {
        this.mGapTime = gapTime;
    }

    /**
     * 结束动画
     */
    public void stop() {
        mIsThreadRunning = false;
    }

    /**
     * 继续动画
     */
    public void reStart() {
        mIsThreadRunning = false;
    }

    /**
     * 设置动画监听器
     */
    public void setOnFrameFinisedListener(OnFrameFinishedListener onFrameFinishedListener) {
        this.mOnFrameFinishedListener = onFrameFinishedListener;
    }

    /**
     * 动画监听器
     *
     * @author qike
     */
    public interface OnFrameFinishedListener {

        /**
         * 动画开始
         */
        void onStart();

        /**
         * 动画结束
         */
        void onStop();
    }

    /**
     * 当用户点击返回按钮时，停止线程，反转内存溢出
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 当按返回键时，将线程停止，避免surfaceView销毁了,而线程还在运行而报错
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mIsThreadRunning = false;
        }

        return super.onKeyDown(keyCode, event);
    }


}
