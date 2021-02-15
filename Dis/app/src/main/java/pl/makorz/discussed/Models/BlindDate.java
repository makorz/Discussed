package pl.makorz.discussed.Models;

import pl.makorz.discussed.R;

public class BlindDate {
    private String name;
    private int imageResourceId;

    public static final BlindDate[] blindDates = {
            new BlindDate("Mateusz, Alfred, Muhamed", R.drawable.blind_date_logo),
            new BlindDate("Kasia, Emilia, Agnieszka", R.drawable.blind_date_logo)
    };

    private BlindDate(String name, int imageResourceId) {
        this.name = name;
        this.imageResourceId = imageResourceId;
    }

    public String getName() {
        return name;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}