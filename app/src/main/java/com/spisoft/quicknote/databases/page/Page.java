package com.spisoft.quicknote.databases.page;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alexandre on 17/10/16.
 */

public class Page {
    public static final int TYPE_HTML= 0;
    public static final int TYPE_IMG = 1;
    public int type;
    public String relativePath;
    public String thumbnail;


    private static final String JSON_TYPE = "type";
    private static final String JSON_RELATIVE_PATH = "relativePath";
    private static final String JSON_THUMBNAIL = "thumbnail";

    public Page(int type, String relativePath, String thumbnail) {
        this.type = type;
        this.relativePath = relativePath;
        this.thumbnail = thumbnail;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(JSONObject.quote(JSON_TYPE));
        sb.append(":");
        sb.append(type);
        sb.append(",");
        if(thumbnail!=null) {
            sb.append(JSONObject.quote(JSON_THUMBNAIL));
            sb.append(":");
            sb.append(thumbnail);
            sb.append(",");
        }
        sb.append(JSONObject.quote(JSON_RELATIVE_PATH));
        sb.append(":");
        sb.append(JSONObject.quote(relativePath));
        sb.append("}");
        return sb.toString();
    }

    public static Page fromJsonObject(JSONObject jsonObject) throws JSONException {
        Page page = new Page(jsonObject.getInt(JSON_TYPE), jsonObject.getString(JSON_RELATIVE_PATH), jsonObject.has(JSON_THUMBNAIL)?jsonObject.getString(JSON_THUMBNAIL):null);
        return page;
    }
}
