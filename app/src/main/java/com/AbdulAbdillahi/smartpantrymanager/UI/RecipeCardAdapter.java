package com.AbdulAbdillahi.smartpantrymanager.UI;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.AbdulAbdillahi.smartpantrymanager.R;
import com.AbdulAbdillahi.smartpantrymanager.model.Recipe;
import com.AbdulAbdillahi.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/** Shows each suggested recipe as an index card. */
public class RecipeCardAdapter extends RecyclerView.Adapter<RecipeCardAdapter.CardViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    /** One suggested recipe, plus the names of expiring pantry items it would use up. */
    public static final class Card {
        final Recipe recipe;
        final List<String> usesUp;

        public Card(Recipe recipe, List<String> usesUp) {
            this.recipe = recipe;
            this.usesUp = usesUp;
        }
    }

    private final List<Card> cards = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public RecipeCardAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void setCards(List<Card> newCards) {
        cards.clear();
        cards.addAll(newCards);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.bind(cards.get(position), position, listener);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView haveAll;
        private final TextView ingredients;
        private final TextView meta;
        private final TextView usesUp;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipeName);
            haveAll = itemView.findViewById(R.id.recipeHaveAll);
            ingredients = itemView.findViewById(R.id.recipeIngredients);
            meta = itemView.findViewById(R.id.recipeMeta);
            usesUp = itemView.findViewById(R.id.recipeUsesUp);
        }

        void bind(Card card, int position, OnRecipeClickListener listener) {
            Context context = itemView.getContext();
            Recipe recipe = card.recipe;

            name.setText(recipe.getName());

            int count = recipe.getIngredients().size();
            haveAll.setText(context.getResources()
                    .getQuantityString(R.plurals.recipe_have_all, count, count));

            List<String> names = new ArrayList<>();
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
             names.add(TextFormat.capitalise(ingredient.getName()));
            }
            ingredients.setText(TextUtils.join(" · ", names));

            meta.setText(context.getString(R.string.recipe_meta,
                    recipe.getPrepMinutes(), recipe.getServings()));

            if (card.usesUp.isEmpty()) {
                usesUp.setVisibility(View.GONE);
            } else {
                usesUp.setVisibility(View.VISIBLE);
                usesUp.setText(context.getString(R.string.recipe_uses_up,
                        TextUtils.join(" and ", card.usesUp)));
            }

            // Alternate a slight tilt so the cards look hand-placed, like real index cards
            itemView.setRotation(position % 2 == 0 ? -0.8f : 0.7f);
            itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
        }

    }
}