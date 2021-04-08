package ca.uwaterloo.cs349;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import java.util.Vector;

public class PathView extends View implements android.view.ViewTreeObserver.OnPreDrawListener {
    public Vector<Pair<Float, Float>> points = new Vector<>();

    public PathView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void onSizeChanged() {
        requestLayout();
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        System.out.println("drawing path with " + points.size() + " points");
        Paint paint = new Paint();
        paint.setStrokeWidth(10);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        if (!points.isEmpty()) {
            float currentX = points.elementAt(0).first;
            float currentY = points.elementAt(0).second;
            for (int i = 1; i < points.size(); i++) {
                c.drawLine(currentX, currentY, points.elementAt(i).first, points.elementAt(i).second, paint);

                currentX = points.elementAt(i).first;
                currentY = points.elementAt(i).second;
                c.drawCircle(currentX, currentY, 10, paint);
            }
        }
    }

    @Override
    public boolean onPreDraw() {
        return false;
    }
}
