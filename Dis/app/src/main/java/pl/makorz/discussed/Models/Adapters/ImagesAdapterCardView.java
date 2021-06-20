package pl.makorz.discussed.Models.Adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import pl.makorz.discussed.R;

public class ImagesAdapterCardView extends RecyclerView.Adapter<ImagesAdapterCardView.ViewHolder> {

    private String[] captions;
    private int[] imageIds;
    private ImagesAdapterCardView.Listener listener;

    public static interface Listener {
        public void onClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public ViewHolder(CardView v) {
            super(v);
            cardView=v;
        }
    }

    public ImagesAdapterCardView(String[] captions, int[] imageIds){
        this.captions = captions;
        this.imageIds = imageIds;
    }

    public void setListener(ImagesAdapterCardView.Listener listener) {
        this.listener = listener;
    }

    @Override
    public ImagesAdapterCardView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_images_view, parent, false);
        return new ImagesAdapterCardView.ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(ImagesAdapterCardView.ViewHolder holder, final int position) {
        CardView cardView = holder.cardView;
        ImageView imageView = (ImageView)cardView.findViewById(R.id.info_image);
        Drawable drawable = cardView.getResources().getDrawable(imageIds[position], null);
        imageView.setImageDrawable(drawable);
        imageView.setContentDescription(captions[position]);
        TextView textView = (TextView)cardView.findViewById(R.id.info_text);
        textView.setText(captions[position]);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return captions.length;
    }
}
