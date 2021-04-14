package pl.makorz.discussed.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pl.makorz.discussed.Adpaters.ImagesAdapterCardView;
import pl.makorz.discussed.BlindDateActivity;
import pl.makorz.discussed.Models.BlindDate;
import pl.makorz.discussed.R;

public class BlindDateFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RecyclerView blindDatesRecycler = (RecyclerView)inflater.inflate(R.layout.fragment_bilnddates,
                container, false);

        String[] blindDateMembersNames = new String[BlindDate.blindDates.length];
        for (int i = 0; i < blindDateMembersNames.length; i++) {
            blindDateMembersNames[i] = BlindDate.blindDates[i].getName();
        }

        int[] blindDateImages = new int[BlindDate.blindDates.length];
        for (int i = 0; i < blindDateImages.length; i++) {
            blindDateImages[i] = BlindDate.blindDates[i].getImageResourceId();
        }

        ImagesAdapterCardView adapter = new ImagesAdapterCardView(blindDateMembersNames, blindDateImages);
        blindDatesRecycler.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        blindDatesRecycler.setLayoutManager(layoutManager);
        adapter.setListener(new ImagesAdapterCardView.Listener() {
            public void onClick(int position) {
                Intent intent = new Intent(getActivity(), BlindDateActivity.class);
                //intent.putExtra(BlindDateActivity.EXTRA_BLINDDATE_INFO, position); --> info passed to blind date chat history activity
                getActivity().startActivity(intent);
            }
        });
        return blindDatesRecycler;
    }


}