package com.AbdulAbdillahi.smartpantrymanager.UI;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.AbdulAbdillahi.smartpantrymanager.R;
import com.AbdulAbdillahi.smartpantrymanager.logic.Freshness;
import com.AbdulAbdillahi.smartpantrymanager.model.PantryItem;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shows each pantry item as a jar on a shelf.
 * Lid colour = freshness, fill level = how much of the original amount is left.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.JarViewHolder> {

    /** Lets the screen react when a jar is tapped (used for editing). */
    public interface OnJarClickListener {
        void onJarClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnJarClickListener listener;

    public PantryAdapter(OnJarClickListener listener) {
        this.listener = listener;
    }

    /** Replaces the whole list, e.g. after reloading from the database. */
    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry_jar, parent, false);
        return new JarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JarViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Shows 6.0 as "6" but keeps 0.5 as "0.5". */
    static String formatQuantity(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    /** Holds the views of one jar so they're looked up once, not on every scroll. */
   public static class JarViewHolder extends RecyclerView.ViewHolder {

        private final View lid;
        private final FrameLayout body;
        private final View fill;
        private final TextView name;
        private final TextView quantity;
        private final TextView label;

        JarViewHolder(@NonNull View itemView) {
            super(itemView);
            lid = itemView.findViewById(R.id.jarLid);
            body = itemView.findViewById(R.id.jarBody);
            fill = itemView.findViewById(R.id.jarFill);
            name = itemView.findViewById(R.id.jarName);
            quantity = itemView.findViewById(R.id.jarQuantity);
            label = itemView.findViewById(R.id.jarLabel);
            body.setClipToOutline(true); // keeps the fill inside the jar's rounded corners
        }

        void bind(PantryItem item, OnJarClickListener listener) {
            Context context = itemView.getContext();
            name.setText(item.getName());
            quantity.setText(context.getString(R.string.quantity_with_unit,
                    formatQuantity(item.getQuantity()), item.getUnit()));

            Freshness freshness = Freshness.of(item.getExpiryDate(),
                    LocalDate.now(), Freshness.DEFAULT_SOON_DAYS);
            applyFreshness(context, freshness, item);

            // Fill level: share of the original amount still left.
            // Minimum 10% so a nearly-used item is still visible.
            double ratio = item.getInitialQuantity() > 0
                    ? item.getQuantity() / item.getInitialQuantity() : 1.0;
            ratio = Math.max(0.1, Math.min(1.0, ratio));
            int innerHeight = body.getLayoutParams().height
                    - body.getPaddingTop() - body.getPaddingBottom();
            ViewGroup.LayoutParams params = fill.getLayoutParams();
            params.height = (int) (innerHeight * ratio);
            fill.setLayoutParams(params);

            itemView.setOnClickListener(v -> listener.onJarClick(item));
        }

        /** Colours the lid and fill, and shows a text label so colour is never the only signal. */
        private void applyFreshness(Context context, Freshness freshness, PantryItem item) {
            int lidColor;
            int fillColor;
            int labelColor = R.color.ink;
            String labelText = null;

            switch (freshness) {
                case EXPIRED:
                    lidColor = R.color.urgent;
                    fillColor = R.color.urgent_fill;
                    labelColor = R.color.urgent_text;
                    labelText = context.getString(R.string.label_expired);
                    break;
                case USE_TODAY:
                    lidColor = R.color.urgent;
                    fillColor = R.color.urgent_fill;
                    labelColor = R.color.urgent_text;
                    labelText = context.getString(R.string.label_use_today);
                    break;
                case USE_SOON:
                    lidColor = R.color.soon;
                    fillColor = R.color.soon_fill;
                    labelColor = R.color.soon_text;
                    String day = LocalDate.parse(item.getExpiryDate()).getDayOfWeek()
                            .getDisplayName(TextStyle.SHORT, Locale.getDefault());
                    labelText = context.getString(R.string.label_use_by, day);
                    break;
                default:
                    lidColor = R.color.fresh;
                    fillColor = R.color.fresh_fill;
                    break;
            }

            lid.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, lidColor)));
            fill.setBackgroundColor(ContextCompat.getColor(context, fillColor));

            if (labelText == null) {
                label.setVisibility(View.GONE);
            } else {
                label.setVisibility(View.VISIBLE);
                label.setText(labelText);
                label.setTextColor(ContextCompat.getColor(context, labelColor));
            }
        }
    }
}