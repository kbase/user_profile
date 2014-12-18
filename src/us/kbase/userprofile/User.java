
package us.kbase.userprofile;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * <p>Original spec-file type: User</p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "username",
    "realname",
    "thumbnail"
})
public class User {

    @JsonProperty("username")
    private String username;
    @JsonProperty("realname")
    private String realname;
    @JsonProperty("thumbnail")
    private String thumbnail;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    public User withUsername(String username) {
        this.username = username;
        return this;
    }

    @JsonProperty("realname")
    public String getRealname() {
        return realname;
    }

    @JsonProperty("realname")
    public void setRealname(String realname) {
        this.realname = realname;
    }

    public User withRealname(String realname) {
        this.realname = realname;
        return this;
    }

    @JsonProperty("thumbnail")
    public String getThumbnail() {
        return thumbnail;
    }

    @JsonProperty("thumbnail")
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public User withThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return ((((((((("User"+" [username=")+ username)+", realname=")+ realname)+", thumbnail=")+ thumbnail)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
