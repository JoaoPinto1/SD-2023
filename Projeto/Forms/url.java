package Forms;

import java.io.Serializable;

public class url implements Serializable {
    private String url;

    public url() {}

    public url(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

}