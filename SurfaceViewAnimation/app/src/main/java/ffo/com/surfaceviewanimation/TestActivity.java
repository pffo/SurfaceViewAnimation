package ffo.com.surfaceviewanimation;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

/**
 * @author: huchunhua
 * @time: 2018/11/2
 * @package: ffo.com.surfaceviewanimation
 * @project: SurfaceViewAnimation
 * @mail: huachunhu@qq.com
 * @describe: 一句话描述
 */
public class TestActivity extends Activity {

    private int[] mFrames_mvw; // 帧数组
    private int[] mFrames_listen; // 帧数组
    private int[] mFrames_think; // 帧数组

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFrames_listen = getData(R.array.listen);
        mFrames_mvw = getData(R.array.mvw_first);
        mFrames_think = getData(R.array.think);

        final SurfaceViewAnimation surfaceViewAnimation = findViewById(R.id.surfaceAnimaion);

        Button mvw = findViewById(R.id.mvw);
        Button listen = findViewById(R.id.listen);
        Button think = findViewById(R.id.think);

        surfaceViewAnimation.setBitmapResoursID(mFrames_mvw);
        surfaceViewAnimation.start();


        mvw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceViewAnimation.setBitmapResoursID(mFrames_mvw);
            }
        });

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceViewAnimation.setBitmapResoursID(mFrames_listen);
            }
        });

        think.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceViewAnimation.setBitmapResoursID(mFrames_think);
            }
        });


    }

    /**
     * 从xml中读取帧数组
     *
     * @param resId
     * @return
     */
    public int[] getData(int resId) {
        TypedArray array = getResources().obtainTypedArray(resId);

        int len = array.length();
        int[] intArray = new int[array.length()];

        for (int i = 0; i < len; i++) {
            intArray[i] = array.getResourceId(i, 0);
        }
        array.recycle();
        return intArray;
    }
}
