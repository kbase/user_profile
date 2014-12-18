package us.kbase.userprofile;

import java.util.List;
import us.kbase.auth.AuthToken;
import us.kbase.common.service.JsonServerMethod;
import us.kbase.common.service.JsonServerServlet;

//BEGIN_HEADER
import java.util.ArrayList;
//END_HEADER

/**
 * <p>Original spec-file module name: UserProfile</p>
 * <pre>
 * </pre>
 */
public class UserProfileServer extends JsonServerServlet {
    private static final long serialVersionUID = 1L;

    //BEGIN_CLASS_HEADER
    public static final String VERSION = "0.1.0";
    //END_CLASS_HEADER

    public UserProfileServer() throws Exception {
        super("UserProfile");
        //BEGIN_CONSTRUCTOR
        //END_CONSTRUCTOR
    }

    /**
     * <p>Original spec-file function name: ver</p>
     * <pre>
     * </pre>
     * @return   instance of String
     */
    @JsonServerMethod(rpc = "UserProfile.ver")
    public String ver() throws Exception {
        String returnVal = null;
        //BEGIN ver
        returnVal = VERSION;
        //END ver
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: filter_users</p>
     * <pre>
     * Returns a list of users matching the filter.  If the 'filter' field
     * is empty or null, then this will return all Users.  The filter will
     * match substrings in usernames and realnames.
     * </pre>
     * @param   p   instance of type {@link us.kbase.userprofile.FilterParams FilterParams}
     * @return   parameter "users" of list of type {@link us.kbase.userprofile.User User}
     */
    @JsonServerMethod(rpc = "UserProfile.filter_users")
    public List<User> filterUsers(FilterParams p) throws Exception {
        List<User> returnVal = null;
        //BEGIN filter_users
        returnVal = new ArrayList<User>();
        //END filter_users
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: get_user_profile</p>
     * <pre>
     * Given a list of usernames, returns a list of UserProfiles in the same order.
     * If no UserProfile was found for a username, the UserProfile at that position will
     * be null.
     * </pre>
     * @param   usernames   instance of list of original type "username"
     * @return   parameter "profiles" of list of type {@link us.kbase.userprofile.UserProfile UserProfile}
     */
    @JsonServerMethod(rpc = "UserProfile.get_user_profile")
    public List<UserProfile> getUserProfile(List<String> usernames) throws Exception {
        List<UserProfile> returnVal = null;
        //BEGIN get_user_profile
        returnVal = new ArrayList<UserProfile>();
        for(int k=0; k<usernames.size(); k++) {
            returnVal.add(null);
        }
        //END get_user_profile
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: set_user_profile</p>
     * <pre>
     * Set the UserProfile for the user indicated in the User field of the UserProfile
     * object.  This operation can only be performed if authenticated as the user in
     * the UserProfile or as the admin account of this service.
     * In the parameters, if replace is set to 1, then the entire profile will be
     * replaced by the new profile.  If replace is set to 0, then only the fields specified
     * in the new UserProfile will be updated.
     * </pre>
     * @param   arg1   instance of type {@link us.kbase.userprofile.SetUserProfileParams SetUserProfileParams}
     */
    @JsonServerMethod(rpc = "UserProfile.set_user_profile")
    public void setUserProfile(SetUserProfileParams arg1, AuthToken authPart) throws Exception {
        //BEGIN set_user_profile
        //END set_user_profile
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: <program> <server_port>");
            return;
        }
        new UserProfileServer().startupServer(Integer.parseInt(args[0]));
    }
}
