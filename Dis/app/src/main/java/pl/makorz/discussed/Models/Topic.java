package pl.makorz.discussed.Models;

public class Topic {

    private String topicTitle;
    private boolean isFavorite;


    public Topic() {
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
    }


}
