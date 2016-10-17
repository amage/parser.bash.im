package org.playstat.parsers.bashim;

public class BashItem {
    private final String id;
    private String text;
    private Long rate;

    public BashItem(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getRate() {
        return rate;
    }

    public void setRate(Long rate) {
        this.rate = rate;
    }
    
    public String getId() {
        return id;
    }
}
