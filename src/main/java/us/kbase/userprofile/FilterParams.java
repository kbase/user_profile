
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
 * <p>Original spec-file type: FilterParams</p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "filter"
})
public class FilterParams {

    @JsonProperty("filter")
    private String filter;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("filter")
    public String getFilter() {
        return filter;
    }

    @JsonProperty("filter")
    public void setFilter(String filter) {
        this.filter = filter;
    }

    public FilterParams withFilter(String filter) {
        this.filter = filter;
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
        return ((((("FilterParams"+" [filter=")+ filter)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
