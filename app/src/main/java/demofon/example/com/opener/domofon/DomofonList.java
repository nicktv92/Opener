package demofon.example.com.opener.domofon;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DomofonList {
    @SerializedName("ADDRESS")
    @Expose
    private String domofon;
    @SerializedName("RELAY_ID")
    @Expose
    private String domofon_id;

    public DomofonList(String domofon, String domofon_id) {
        this.domofon = domofon;
        this.domofon_id = domofon_id;
    }

    String getDomofon() {
        return domofon;
    }

    String getDomofon_id() {
        return domofon_id;
    }
}
