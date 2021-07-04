package pl.makorz.discussed.Models.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.ocpsoft.prettytime.PrettyTime;

import pl.makorz.discussed.Controllers.ChatActivity;
import pl.makorz.discussed.Models.MessageInChat;
import pl.makorz.discussed.R;

public class MessageInChatAdapter extends FirestoreRecyclerAdapter<MessageInChat,MessageInChatAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_LEFT_GRADED = 2;
    public static final int MSG_TYPE_RIGHT_GRADED = 3;
    private static onLongItemClickListener listener;

    PrettyTime p = new PrettyTime();
    FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();

    public MessageInChatAdapter(@NonNull FirestoreRecyclerOptions<MessageInChat> options) {
        super(options);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if(viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right, parent, false);
        } else if (viewType == MSG_TYPE_RIGHT_GRADED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right_graded, parent, false);
        } else if (viewType == MSG_TYPE_LEFT_GRADED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left_graded, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left, parent, false);
        }
        return new ViewHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull MessageInChat model) {
        holder.textOfMessage.setText(model.getTextOfMessage());
        holder.userNameID.setText(model.getUserNameID());
        holder.dateOfMessage.setText(p.format(model.getDateOfMessage()));
        holder.wasGraded.setChecked(model.wasGraded());
        holder.messageID.setText(model.getMessageID());
    }


    @Override
    public int getItemViewType(int position) {

        if(getItem(position).getUserID().equals(user.getUid())) {
            if (getItem(position).wasGraded()) {
                return MSG_TYPE_RIGHT_GRADED;
            } else {
                return MSG_TYPE_RIGHT;
            }
        } else{
            if (getItem(position).wasGraded()) {
                return MSG_TYPE_LEFT_GRADED;
            } else {
                return MSG_TYPE_LEFT;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textOfMessage, messageID, userNameID, dateOfMessage;
        CheckBox wasGraded;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameID = itemView.findViewById(R.id.userNameID);
            textOfMessage = itemView.findViewById(R.id.textOfMessage);
            dateOfMessage = itemView.findViewById(R.id.dateOfMessage);
            wasGraded = itemView.findViewById(R.id.wasGraded);
            messageID = itemView.findViewById(R.id.messageID);


            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (getItemViewType() == MSG_TYPE_LEFT && !wasGraded.isChecked()){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && listener != null) {
                            int size = textOfMessage.getText().length();
                            String idMessage = messageID.getText().toString();
                            int points = (int) (size * 0.05);
                            if (points >= 10) {
                                points = 10;
                            }
                            Log.d("MESSAGEADPATELONG", String.valueOf(points));
                            listener.onLongItemClick(points, position, idMessage);
                        }
                        return true;

                    }
                    return false;
                }
            });

        }
    }

    // This methods below allow to send data from adapter to activity
    public interface onLongItemClickListener {
        void onLongItemClick(int points, int position, String messageID);
    }

    public void setOnLongItemCLickListener(onLongItemClickListener listener) {
        MessageInChatAdapter.listener = listener;
    }


}