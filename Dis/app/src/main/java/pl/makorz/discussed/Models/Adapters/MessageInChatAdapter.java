package pl.makorz.discussed.Models.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.ocpsoft.prettytime.PrettyTime;

import pl.makorz.discussed.Models.MessageInChat;
import pl.makorz.discussed.R;

public class MessageInChatAdapter extends FirestoreRecyclerAdapter<MessageInChat,MessageInChatAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    PrettyTime p = new PrettyTime();
    FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */

    public MessageInChatAdapter(@NonNull FirestoreRecyclerOptions<MessageInChat> options) {
        super(options);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if(viewType == MSG_TYPE_RIGHT)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right, parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left, parent, false);

        return new ViewHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull MessageInChat model) {
        holder.textOfMessage.setText(model.getTextOfMessage());
        holder.userNameID.setText(model.getUserNameID());
        holder.dateOfMessage.setText(p.format(model.getDateOfMessage()));
    }


    @Override
    public int getItemViewType(int position) {
        if(getItem(position).getUserID().equals(user.getUid()))
            return MSG_TYPE_RIGHT;
        else
            return MSG_TYPE_LEFT;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textOfMessage, userID, userNameID, dateOfMessage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameID = itemView.findViewById(R.id.userNameID);
            textOfMessage = itemView.findViewById(R.id.textOfMessage);
            dateOfMessage = itemView.findViewById(R.id.dateOfMessage);
        }
    }
}