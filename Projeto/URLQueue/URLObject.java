package URLQueue;

import java.io.Serializable;

public class URLObject implements Serializable {
    private String url;
    private String citation;
    private String title;

    public String getCitation() {
        return citation;
    }

    public String getTitle() {
        return title;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public URLObject(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }


}
