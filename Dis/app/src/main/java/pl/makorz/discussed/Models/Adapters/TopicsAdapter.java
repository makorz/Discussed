package pl.makorz.discussed.Models.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import pl.makorz.discussed.Models.Topic;
import pl.makorz.discussed.R;

public class TopicsAdapter extends FirestoreRecyclerAdapter<Topic,TopicsAdapter.ViewHolder> {

    private static OnItemClickListener listener;

    public TopicsAdapter(@NonNull FirestoreRecyclerOptions<Topic> options) {
        super(options);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.topics_view, parent, false);
        return new TopicsAdapter.ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Topic model) {
        String topic = model.getTopicTitle() ;
        holder.topicTitle.setText(topic);
        holder.isTopicInFavorites.isClickable();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView topicTitle;
        CheckBox isTopicInFavorites;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            topicTitle = itemView.findViewById(R.id.single_topic_name);
            isTopicInFavorites = itemView.findViewById(R.id.is_topic_favorite_check);

            isTopicInFavorites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        boolean isFavorite;
                        isFavorite = isTopicInFavorites.isChecked();
                        listener.onItemClick(isFavorite, position, topicTitle.getText().toString(),isTopicInFavorites);
                    }

                }
            });

        }
    }

    // This methods below allow to send data from adapter to activity
    public interface OnItemClickListener {
        void onItemClick(boolean isFavorite, int position, String topicTitle, CompoundButton cb);
    }

    public void setOnItemCLickListener(OnItemClickListener listener) {
        TopicsAdapter.listener = listener;
    }

}
