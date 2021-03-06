package com.tangzy.tzymvp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.tangzy.tzymvp.R;
import com.tangzy.tzymvp.util.DrawUtil;
import com.tangzy.tzymvp.util.Logger;

import java.util.Stack;

@SuppressLint("AppCompatCustomView")
public class MyView extends ImageView {
    private static final String TAG = "MyView";
    private Context context;

    private Stack<Path> stack = new Stack<>();

    /**
     * 创建一个继承View的class
     *View一共有四个构造器 这里先说两个
     * 第一个构造器的参数就是context,这是在Activity实例化的时候所需的,比如Button button = new Button(context);
     * 第二个构造器有两个参数 第一个同样是context 第二个参数AttributeSet放入了一些属性,这是我们在写XML文件时用到的比如
     * android:layout_width="match_parent"
     * android:layout_height="match_parent"如果不写函数的话是无法通过XML添加View
     */
    public MyView(Context context) {
        super(context);
        this.context = context;
    }

    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Logger.d(TAG, "onMeasure");
//        Logger.d(TAG, "widthMeasureSpec = "+widthMeasureSpec);
//        Logger.d(TAG, "heightMeasureSpec = "+heightMeasureSpec);
//
//        final int minimumWidth = getSuggestedMinimumWidth();
//        final int minimumHeight = getSuggestedMinimumHeight();
//        Logger.d(TAG, "---minimumWidth = " + minimumWidth + "");
//        Logger.d(TAG, "---minimumHeight = " + minimumHeight + "");
//        int width = measureWidth(minimumWidth, widthMeasureSpec);
//        int height = measureHeight(minimumHeight, heightMeasureSpec);
//        setMeasuredDimension(width, height);
    }

    private int measureWidth(int defaultWidth, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        Logger.d(TAG, "---speSize = " + specSize + "");
        switch (specMode) {
            case MeasureSpec.AT_MOST:
//                defaultWidth = (int) mPaint.measureText(mText) + getPaddingLeft() + getPaddingRight();
                Logger.d(TAG, "---speMode = AT_MOST");
                break;
            case MeasureSpec.EXACTLY:
                Logger.d(TAG, "---speMode = EXACTLY");
                defaultWidth = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                Logger.d(TAG, "---speMode = UNSPECIFIED");
                defaultWidth = Math.max(defaultWidth, specSize);
            default:
                break;
        }
//        invalidate(1,1,1,1);

        return defaultWidth;
    }

    private int measureHeight(int defaultHeight, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        Logger.d(TAG, "---speSize = " + specSize + "");

        switch (specMode) {
            case MeasureSpec.AT_MOST:
//                defaultHeight = (int) (-mPaint.ascent() + mPaint.descent()) + getPaddingTop() + getPaddingBottom();
                Logger.d(TAG, "---speMode = AT_MOST");
                break;
            case MeasureSpec.EXACTLY:
                defaultHeight = specSize;
                Logger.d(TAG, "---speSize = EXACTLY");
                break;
            case MeasureSpec.UNSPECIFIED:
                defaultHeight = Math.max(defaultHeight, specSize);
                Logger.d(TAG, "---speSize = UNSPECIFIED");
//        1.基准点是baseline
//        2.ascent：是baseline之上至字符最高处的距离
//        3.descent：是baseline之下至字符最低处的距离
//        4.leading：是上一行字符的descent到下一行的ascent之间的距离,也就是相邻行间的空白距离
//        5.top：是指的是最高字符到baseline的值,即ascent的最大值
//        6.bottom：是指最低字符到baseline的值,即descent的最大值
                break;
            default:
                break;
        }
        return defaultHeight;

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Logger.d(TAG, "onLayout");// 循环所有子View改变子view位置
        Logger.d(TAG, "left = "+left +",top = "+ top+",right = "+right+",bottom = "+bottom);// 循环所有子View
    }

    @Override
    public void layout(int left, int top, int right, int bottom) {
        Logger.d(TAG, "layout");// 循环所有子View改变子view位置
        Logger.d(TAG, "left = "+left +",top = "+ top+",right = "+right+",bottom = "+bottom);// 循环所有子View
//        super.layout(l, t, r, b);
        super.layout(20, 20, 900, 1500);//改变view位置
    }

    private float sx,sy, tx,ty = -1;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Logger.d(TAG, "dispatchTouchEvent");
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                sx = event.getX();
                sy = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                tx = event.getX();
                ty = event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                tx = event.getX();
                ty = event.getY();
                Path path = new Path();
                updateRectPath(path,sx,sy,tx,ty);
                stack.add(path);
                invalidate();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    //重写onDraw方法
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Logger.d(TAG, "onDraw");

//        int width = View.MeasureSpec.makeMeasureSpec(0,
//                View.MeasureSpec.UNSPECIFIED);
//        int height = View.MeasureSpec.makeMeasureSpec(0,
//                View.MeasureSpec.UNSPECIFIED);
////调用measure方法之后就可以获取宽高
//        view.measure(width, height);
//        view.getMeasuredWidth(); // 获取宽度
//        view.getMeasuredHeight(); // 获取高度


        canvas.drawColor(Color.RED);      //设置canvas的背景色
        float radius = 50;                //给定半径
        //给定圆心的的坐标
        float cx = 50;
        float cy = 50;
        Paint paint = new Paint();       //实例化一个Paint对象
        paint.setColor(Color.BLUE);      //设置圆的颜色
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);

//        canvas.drawLine(10,30,100,200, paint);
//        float[] pts = {10,350, 100, 50, 500,500,600,600};
//        float[] pts = new float[1000];
//        for (int i = 0; i <1000;){
//            pts[i] = i;
//            pts[i+1] = i%50;
//            i=i+2;
//        }

//        canvas.drawLines(pts, paint);
//        canvas.drawLines(pts, 10, 20, paint);

//        canvas.drawPoints(pts, paint);

//        saveMatrix(canvas);

        //通过canvas的drawCircle方法画一个圆圈.
//        canvas.drawCircle(cx, cy, radius, paint);//
//        canvas.drawOval(0,0,100,100,paint);//画一个椭圆.
//        canvas.drawRoundRect(0,0,200,400,100, 50 ,paint);//圆角矩形
//        canvas.drawArc(300,500,500,900,0, 200, true ,paint);//扇面弓形（可椭圆）
//        Drawable drawable = context.getDrawable(R.mipmap.ic_launcher);
//        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
//        canvas.drawBitmap(bitmap, 50, 50, paint);


        Path path = new Path();
//        path.addArc(10,10,500,500,500,300);
////        canvas.drawPath(path, paint);
//        canvas.drawArc(10,10,500,500,500,300, true, paint);

//        float  sx = 139.55554f;
//        float sy = 283.55554f;
//        float dx = 651.0857f;
//        float  dy = 623.9746f;
//            // TODO: 2020/8/21

//        updateRectPath(path, sx, sy, dx, dy);// addRect
//        canvas.drawPath(path, paint);

        //绘制矩形
//        canvas.drawRect(10, 150, 70, 190, paint);

        if (tx > -1){

            updateRectPath(path, sx, sy, tx, ty);// addRect
            canvas.drawPath(path, paint);
        }
        for (Path item :stack){
            canvas.drawPath(item, paint);
        }

    }

    //---------计算Path
    private Path mArrowTrianglePath;

    private void updateArrowPath(Path path, float sx, float sy, float ex, float ey, float size) {
        float arrowSize = size*4;
        double H = arrowSize; // 箭头高度
        double L = arrowSize / 2; // 底边的一�?

        double awrad = Math.atan(L / 2 / H); // 箭头角度
        double arraow_len = Math.sqrt(L / 2 * L / 2 + H * H) - 5; // 箭头的长�?
        double[] arrXY_1 = DrawUtil.rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
        double[] arrXY_2 = DrawUtil.rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
        float x_3 = (float) (ex - arrXY_1[0]); // (x3,y3)是第�?端点
        float y_3 = (float) (ey - arrXY_1[1]);
        float x_4 = (float) (ex - arrXY_2[0]); // (x4,y4)是第二端�?
        float y_4 = (float) (ey - arrXY_2[1]);
        // 画线
        path.moveTo(sx, sy);
        path.lineTo(x_3, y_3);
        path.lineTo(x_4, y_4);
        path.close();

        awrad = Math.atan(L / H); // 箭头角度
        arraow_len = Math.sqrt(L * L + H * H); // 箭头的长�?
        arrXY_1 = DrawUtil.rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
        arrXY_2 = DrawUtil.rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
        x_3 = (float) (ex - arrXY_1[0]); // (x3,y3)是第�?端点
        y_3 = (float) (ey - arrXY_1[1]);
        x_4 = (float) (ex - arrXY_2[0]); // (x4,y4)是第二端�?
        y_4 = (float) (ey - arrXY_2[1]);
        if (mArrowTrianglePath == null) {
            mArrowTrianglePath = new Path();
        }
        mArrowTrianglePath.reset();
        mArrowTrianglePath.moveTo(ex, ey);
        mArrowTrianglePath.lineTo(x_4, y_4);
        mArrowTrianglePath.lineTo(x_3, y_3);
        mArrowTrianglePath.close();
        path.addPath(mArrowTrianglePath);
    }

    private void updateLinePath(Path path, float sx, float sy, float ex, float ey, float size) {
        path.moveTo(sx, sy);
        path.lineTo(ex, ey);
    }

    private void updateCirclePath(Path path, float sx, float sy, float dx, float dy, float size) {
        float radius = (float) Math.sqrt((sx - dx) * (sx - dx) + (sy - dy) * (sy - dy));
        path.addCircle(sx, sy, radius, Path.Direction.CCW);

    }

    private void updateRectPath(Path path, float sx, float sy, float dx, float dy) {
        // 保证　左上角　与　右下角　的对应关系
        if (sx < dx) {
            if (sy < dy) {
                path.addRect(sx, sy, dx, dy, Path.Direction.CCW);
            } else {
                path.addRect(sx, dy, dx, sy, Path.Direction.CCW);
            }
        } else {
            if (sy < dy) {
                path.addRect(dx, sy, sx, dy, Path.Direction.CCW);
            } else {
                path.addRect(dx, dy, sx, sy, Path.Direction.CCW);
            }
        }
    }

    private void saveMatrix(Canvas canvas) {
        Paint mPaint = new Paint();       //实例化一个Paint对象
        mPaint.setColor(Color.BLUE);      //设置圆的颜色
        mPaint.setStrokeWidth(5);
        //绘制蓝色矩形
        mPaint.setColor(getResources().getColor(android.R.color.holo_blue_light));
        canvas.drawRect(0, 0, 200, 200, mPaint);
        //保存
        canvas.save();
        //裁剪画布,并绘制红色矩形
        mPaint.setColor(getResources().getColor(android.R.color.holo_red_light));
        //平移.
        canvas.translate(100, 0);
        //缩放.
        canvas.scale(0.5f, 0.5f);
        //旋转
        canvas.rotate(-45);
        //倾斜
        canvas.skew(0, 0.5f);
        canvas.drawRect(0, 0, 200, 200, mPaint);
        //恢复画布
        canvas.restore();
        //绘制绿色矩形
        mPaint.setColor(getResources().getColor(android.R.color.holo_green_light));
        canvas.drawRect(0, 0, 50, 200, mPaint);
    }
}
