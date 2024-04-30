
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
 * <p>Original spec-file type: SetUserProfileParams</p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "profile"
})
public class SetUserProfileParams {

    /**
     * <p>Original spec-file type: UserProfile</p>
     * 
     * 
     */
    @JsonProperty("profile")
    private UserProfile profile;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * <p>Original spec-file type: UserProfile</p>
     * 
     * 
     */
    @JsonProperty("profile")
    public UserProfile getProfile() {
        return profile;
    }

    /**
     * <p>Original spec-file type: UserProfile</p>
     * 
     * 
     */
    @JsonProperty("profile")
    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public SetUserProfileParams withProfile(UserProfile profile) {
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
        return ((((("SetUserProfileParams"+" [profile=")+ profile)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
