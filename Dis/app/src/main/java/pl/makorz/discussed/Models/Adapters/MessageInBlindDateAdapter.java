package pl.makorz.discussed.Models.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.ocpsoft.prettytime.PrettyTime;

import pl.makorz.discussed.Models.MessageInBlindDate;
import pl.makorz.discussed.R;

public class MessageInBlindDateAdapter extends FirestoreRecyclerAdapter<MessageInBlindDate,MessageInBlindDateAdapter.ViewHolder> {

    public static final int MSG_TYPE_OWN = 100;
    public static final int MSG_TYPE_0 = 0;
    public static final int MSG_TYPE_ALIEN_1 = 1;
    public static final int MSG_TYPE_ALIEN_2 = 2;
    public static final int MSG_TYPE_ALIEN_3 = 3;
    public static final int MSG_TYPE_HOST = 10;

    PrettyTime p = new PrettyTime();
    FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();

    public MessageInBlindDateAdapter(@NonNull FirestoreRecyclerOptions<MessageInBlindDate> options) {
        super(options);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blind_chat_left_1, parent, false);
                break;
            case 2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blind_chat_left_2, parent, false);
                break;
            case 3:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blind_chat_left_3, parent, false);
                break;
            case 10:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blind_chat_host_middle, parent, false);
                break;
            case 100:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blind_chat_right_own, parent, false);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blind_chat_left_0, parent, false);
                break;
        }

        return new ViewHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull MessageInBlindDate model) {
        holder.textOfMessage.setText(model.getTextOfMessage());
        holder.userName.setText(model.getUserName());
        holder.dateOfMessage.setText(p.format(model.getDateOfMessage()));
        holder.messageID.setText(model.getMessageID());
    }


    @Override
    public int getItemViewType(int position) {

        if(getItem(position).getUserID().equals(user.getUid())) {
            return MSG_TYPE_OWN;
        } else {
            switch (getItem(position).getTypeOfMessage()) {
                case 1:
                    return MSG_TYPE_ALIEN_1;
                case 2:
                    return MSG_TYPE_ALIEN_2;
                case 3:
                    return MSG_TYPE_ALIEN_3;
                case 10:
                    return MSG_TYPE_HOST;
                default:
                    return MSG_TYPE_0;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textOfMessage, messageID, userName, dateOfMessage;
        ImageView gradedSymbol;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userNameID);
            textOfMessage = itemView.findViewById(R.id.textOfMessage);
            dateOfMessage = itemView.findViewById(R.id.dateOfMessage);
            messageID = itemView.findViewById(R.id.messageID);
            gradedSymbol = itemView.findViewById(R.id.is_message_grade_image);

        }
    }

}
