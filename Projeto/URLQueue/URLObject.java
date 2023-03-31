package URLQueue;

import java.io.Serializable;

/**
 * Objeto URL
 */
public class URLObject implements Serializable {
    private final String url;
    private String citation;
    private String title;

    /**
     * Getter da citacao do website
     *
     * @return Citacao
     */
    public String getCitation() {
        return citation;
    }

    /**
     * Getter do titulo do website
     *
     * @return Titulo
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter da citacao do website
     *
     * @param citation Citacao
     */
    public void setCitation(String citation) {
        this.citation = citation;
    }

    /**
     * Setter titulo do website
     *
     * @param title Titulo
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Construtor do objeto URL
     *
     * @param url String do url
     */
    public URLObject(String url) {
        this.url = url;
    }

    /**
     * Getter String do url
     *
     * @return String url
     */
    public String getUrl() {
        return url;
    }


}
