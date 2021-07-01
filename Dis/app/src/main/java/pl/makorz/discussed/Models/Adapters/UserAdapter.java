package pl.makorz.discussed.Models.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import pl.makorz.discussed.Models.User;
import pl.makorz.discussed.R;

public class UserAdapter extends FirestoreRecyclerAdapter<User,UserAdapter.ViewHolder> {

    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull User model) {
        holder.description.setText(model.getDescription());
        holder.name.setText(model.getDisplayName());
        holder.age.setText(model.getAgeString());
        holder.location.setText(model.getLocationString());
        holder.firstPhoto.setText(model.getFirstPhotoUri());
        holder.secondPhoto.setText(model.getSecondPhotoUri());
        holder.thirdPhoto.setText(model.getThirdPhotoUri());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView description, name, age, location, isFemale, firstPhoto, secondPhoto, thirdPhoto;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.own_description_my_profile);
            name = itemView.findViewById(R.id.name_my_profile);
            age = itemView.findViewById(R.id.own_age_my_profile);
            location = itemView.findViewById(R.id.own_location_my_profile);

        }
    }



}
