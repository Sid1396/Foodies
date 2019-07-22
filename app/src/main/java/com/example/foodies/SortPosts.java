package com.example.foodies;

public class SortPosts {

    String postimage;
    String description;



    public SortPosts()
    {}

    public SortPosts(String postimage, String description) {
        this.postimage = postimage;
        this.description = description;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
