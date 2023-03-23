package URLQueue;

import java.io.Serializable;

public class URLObject implements Serializable {
    private String url;

    public URLObject(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }


}
