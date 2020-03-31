package de.datenkraken.datenkrake.ui.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.lang.ref.WeakReference;

/**
 * Extracts Image from {@link de.datenkraken.datenkrake.model.Source}
 * and displays it in a TextView using Glide. <br>
 * Implements Html.ImageGetter.
 *
 * @author Tobias Kröll - tobias.kroell@stud.tu-darmstadt.de
 */
public class GlideImageGetter implements Html.ImageGetter {

    private final WeakReference<Context> context;
    final TextView textView;

    /**
     * Constructor for the GlideImageGetter. <br>
     * Display an Image from a Html text in a TextView.
     *
     * @param context used by Glide to display the image.
     * @param target of Image to be displayed in.
     */
    public GlideImageGetter(Context context, TextView target) {
        this.context = new WeakReference<>(context);
        textView = target;
    }

    /**
     * Loads the Image given as a url string into a Drawable. <br>
     * Creates a new {@link DrawablePlaceholder} and loads the image form the given url into it.
     *
     * @param url of Image.
     * @return Drawable with the image or null, if context or url are null.
     */
    @Override
    public Drawable getDrawable(String url) {
        DrawablePlaceholder drawable = new DrawablePlaceholder();

        if (context.get() != null && url != null) {
            Glide.with(context.get())
                .asDrawable()
                .load(url)
                .into(drawable);

            return drawable;
        } else {
            return null;
        }
    }

    /**
     * Drawable that implements Target. <br>
     * Calculates the size of the drawable used in the TextView.
     * The drawable is loaded into a TextView.
     */
    private class DrawablePlaceholder extends Drawable implements Target<Drawable> {

        private Drawable drawable;
        @Nullable private Request request;
        @Nullable private ColorFilter colorFilter;

        /**
         * Sets the drawable to be loaded and calculates size and bounds of the drawable.
         * Also sets the color filter, and loads the drawable into the TextView.
         *
         * @param drawable to be loaded into TextView.
         */
        private void setDrawable(Drawable drawable) {
            this.drawable = drawable;

            // drawable can also be null
            if (drawable != null) {
                int drawableWidth = drawable.getIntrinsicWidth();
                int drawableHeight = drawable.getIntrinsicHeight();
                int maxWidth = textView.getMeasuredWidth();

                if (drawableWidth > maxWidth) {
                    int calculatedHeight = maxWidth * drawableHeight / drawableWidth;
                    drawable.setBounds(0, 0, maxWidth, calculatedHeight);
                    setBounds(0, 0, maxWidth, calculatedHeight);
                } else {
                    drawable.setBounds(0, 0, drawableWidth, drawableHeight);
                    setBounds(0, 0, drawableWidth, drawableHeight);
                }

                drawable.setColorFilter(this.colorFilter);
            }

            // Refresh view object
            textView.setText(textView.getText());
        }


        /**
         * Upon start of the loading of the image, sets a placeholder image into the drawable.
         *
         * @param placeholder to be loaded.
         */
        @Override
        public void onLoadStarted(@Nullable Drawable placeholder) {
            setDrawable(placeholder);
        }

        /**
         * When the loading of the image fails, loads an backup image.
         *
         * @param errorDrawable image to be loaded, when loading of wanted image fails.
         */
        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            setDrawable(errorDrawable);
        }

        /**
         * Loads the image into the drawable, when the image is ready.
         *
         * @param resource to be loaded into the drawable.
         * @param transition animation for image, not used here.
         */
        @Override
        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
            setDrawable(resource);
        }

        /**
         * Sets a placeholder, when the loading is cleared.
         *
         * @param placeholder to be set for drawable.
         */
        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {
            setDrawable(placeholder);
        }

        /**
         * Sets the sizeof a callback.
         * Uses the original sizes of the image.
         *
         * @param cb callback for which the size should be set.
         */
        @Override
        public void getSize(@NonNull SizeReadyCallback cb) {
            cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        }

        /**
         * Removes the SizeReadyCallback. Not used here.
         *
         * @param cb to be removed.
         */
        @Override
        public void removeCallback(@NonNull SizeReadyCallback cb) {
            // Do nothing, we never retain a reference to the callback.
        }

        /**
         * Sets the Glide request for the drawable.
         *
         * @param request to be set for the drawable.
         */
        @Override
        public void setRequest(@Nullable Request request) {
            this.request = request;
        }

        /**
         * Gets the Glide request of the drawable.
         *
         * @return Glide request of the drawable.
         */
        @Nullable
        @Override
        public Request getRequest() {
            return this.request;
        }

        /**
         * Called on start of the class. Not used here.
         */
        @Override
        public void onStart() {

        }

        /**
         * Called on stop of the class. Not used here.
         */
        @Override
        public void onStop() {

        }

        /**
         * Called on destruction of the class. Not used here.
         */
        @Override
        public void onDestroy() {

        }

        /**
         * Draws the drawable onto a given canvas.
         *
         * @param canvas for drawable to be drawn on, can not be null.
         */
        @Override
        public void draw(@NonNull Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        /**
         * Sets alpha value of the drawable. Not used here.
         *
         * @param alpha value to be set.
         */
        @Override
        public void setAlpha(int alpha) {

        }

        /**
         * Sets a color filter for the drawable.
         *
         * @param colorFilter to be used on the drawable.
         */
        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            this.colorFilter = colorFilter;
            if (drawable != null) {
                drawable.setColorFilter(colorFilter);
            }
        }

        /**
         * Gets the opacity or alpha value of the drawable.
         *
         * @return int displaying the alpha value.
         */
        @Override
        public int getOpacity() {
            return (drawable == null) ? PixelFormat.UNKNOWN : drawable.getAlpha();
        }
    }
}
