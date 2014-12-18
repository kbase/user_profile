
package us.kbase.userprofile;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import us.kbase.common.service.UObject;


/**
 * <p>Original spec-file type: UserProfile</p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "user",
    "profile"
})
public class UserProfile {

    /**
     * <p>Original spec-file type: User</p>
     * 
     * 
     */
    @JsonProperty("user")
    private User user;
    @JsonProperty("profile")
    private UObject profile;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * <p>Original spec-file type: User</p>
     * 
     * 
     */
    @JsonProperty("user")
    public User getUser() {
        return user;
    }

    /**
     * <p>Original spec-file type: User</p>
     * 
     * 
     */
    @JsonProperty("user")
    public void setUser(User user) {
        this.user = user;
    }

    public UserProfile withUser(User user) {
        this.user = user;
        return this;
    }

    @JsonProperty("profile")
    public UObject getProfile() {
        return profile;
    }

    @JsonProperty("profile")
    public void setProfile(UObject profile) {
        this.profile = profile;
    }

    public UserProfile withProfile(UObject profile) {
        this.profile = profile;
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
        return ((((((("UserProfile"+" [user=")+ user)+", profile=")+ profile)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
