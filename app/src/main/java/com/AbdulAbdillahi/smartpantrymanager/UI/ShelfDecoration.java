package com.AbdulAbdillahi.smartpantrymanager.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.AbdulAbdillahi.smartpantrymanager.R;

/**
 * Draws a wooden shelf plank under every row of jars.
 * Decorations are drawn onto the RecyclerView itself, so the planks scroll with the jars.
 */
public class ShelfDecoration extends RecyclerView.ItemDecoration {

    private final int spanCount;
    private final float plankHeight;
    private final float shadowHeight;
    private final float cornerRadius;
    private final Paint plankPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public ShelfDecoration(Context context, int spanCount) {
        this.spanCount = spanCount;
        float density = context.getResources().getDisplayMetrics().density; // dp → pixels
        plankHeight = 10 * density;
        shadowHeight = 5 * density;
        cornerRadius = 3 * density;
        plankPaint.setColor(ContextCompat.getColor(context, R.color.wood));
        shadowPaint.setColor(ContextCompat.getColor(context, R.color.wood_shadow));
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        // Leave space under every jar for the plank and its shadow
        outRect.bottom = (int) (plankHeight + shadowHeight);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent,
                       @NonNull RecyclerView.State state) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);

            // One plank per row: only draw under the first jar of each row
            if (position == RecyclerView.NO_POSITION || position % spanCount != 0) {
                continue;
            }

            float top = child.getBottom() + child.getTranslationY();
            float left = parent.getPaddingLeft() / 2f;
            float right = parent.getWidth() - parent.getPaddingRight() / 2f;

            // Shadow first (slightly lower), then the plank on top of it
            rect.set(left, top + shadowHeight, right, top + plankHeight + shadowHeight);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, shadowPaint);
            rect.set(left, top, right, top + plankHeight);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, plankPaint);
        }
    }
}