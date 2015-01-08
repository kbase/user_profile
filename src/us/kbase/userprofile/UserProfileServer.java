package us.kbase.userprofile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import us.kbase.common.service.JsonServerMethod;
import us.kbase.common.service.JsonServerServlet;





//BEGIN_HEADER
import java.util.ArrayList;

import us.kbase.auth.AuthService;
import us.kbase.auth.AuthToken;
import us.kbase.auth.UserDetail;

import org.ini4j.Ini;

import java.io.File;
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
    
    public static final String SYS_PROP_KB_DEPLOYMENT_CONFIG = "KB_DEPLOYMENT_CONFIG";
    public static final String SERVICE_DEPLOYMENT_NAME = "UserProfile";
    
    public static final String   CFG_MONGO_HOST = "mongodb-host";
    public static final String     CFG_MONGO_DB = "mongodb-database";
    public static final String   CFG_MONGO_USER = "mongodb-user";
    public static final String   CFG_MONGO_PSWD = "mongodb-pwd";
    public static final String  CFG_MONGO_RETRY = "mongodb-retry";
    public static final String        CFG_ADMIN = "admin";
    
    private static Throwable configError = null;
    private static Map<String, String> config = null;

	public static Map<String, String> config() {
		if (config != null)
			return config;
		if (configError != null)
			throw new IllegalStateException("There was an error while loading configuration", configError);
		String configPath = System.getProperty(SYS_PROP_KB_DEPLOYMENT_CONFIG);
		if (configPath == null)
			configPath = System.getenv(SYS_PROP_KB_DEPLOYMENT_CONFIG);
		if (configPath == null) {
			configError = new IllegalStateException("Configuration file was not defined");
		} else {
			System.out.println(UserProfileServer.class.getName() + ": Deployment config path was defined: " + configPath);
			try {
				config = new Ini(new File(configPath)).get(SERVICE_DEPLOYMENT_NAME);
			} catch (Throwable ex) {
				System.out.println(UserProfileServer.class.getName() + ": Error loading deployment config-file: " + ex.getMessage());
				configError = ex;
			}
		}
		if (config == null)
			throw new IllegalStateException("There was unknown error in service initialization when checking"
					+ "the configuration: is the ["+SERVICE_DEPLOYMENT_NAME+"] config group defined?");
		return config;
	}
	private String getConfig(String configName) {
    	String ret = config().get(configName);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + configName + " is not defined in configuration");
    	return ret;
    }

	private final MongoController db;
	
	
    //END_CLASS_HEADER

    public UserProfileServer() throws Exception {
        super("UserProfile");
        //BEGIN_CONSTRUCTOR
        
        System.out.println(UserProfileServer.class.getName() + ": " + CFG_MONGO_HOST +" = " + getConfig(CFG_MONGO_HOST));
        System.out.println(UserProfileServer.class.getName() + ": " + CFG_MONGO_DB +" = " + getConfig(CFG_MONGO_DB));
        System.out.println(UserProfileServer.class.getName() + ": " + CFG_MONGO_RETRY +" = " + getConfig(CFG_MONGO_RETRY));
        System.out.println(UserProfileServer.class.getName() + ": " + CFG_ADMIN +" = " + getConfig(CFG_ADMIN));
        
        String mongoUser = ""; boolean useMongoAuth = true;
        try{
        	mongoUser = getConfig(CFG_MONGO_USER);
        } catch (Exception e) {
        	useMongoAuth = false;
        }
        
        if(useMongoAuth) {
        	db = new MongoController(
            		getConfig(CFG_MONGO_HOST),
            		getConfig(CFG_MONGO_DB),
            		Integer.parseInt(getConfig(CFG_MONGO_RETRY)),
            		mongoUser,
            		getConfig(CFG_MONGO_PSWD)
            		);
        } else {
        	db = new MongoController(
        		getConfig(CFG_MONGO_HOST),
        		getConfig(CFG_MONGO_DB),
        		Integer.parseInt(getConfig(CFG_MONGO_RETRY)));
        }
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
        returnVal = db.filterUsers(p.getFilter());
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
        	// todo: make this one single batch query
            returnVal.add(db.getProfile(usernames.get(k)));
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
     * If the profile does not exist, one will be created.  If it does already exist,
     * then the entire user profile will be replaced with the new profile.
     * </pre>
     * @param   p   instance of type {@link us.kbase.userprofile.SetUserProfileParams SetUserProfileParams}
     */
    @JsonServerMethod(rpc = "UserProfile.set_user_profile")
    public void setUserProfile(SetUserProfileParams p, AuthToken authPart) throws Exception {
        //BEGIN set_user_profile
    	if(p.getProfile()==null)
    		throw new Exception("'profile' field must be set.");
    	if(p.getProfile().getUser()==null)
    		throw new Exception("'profile.user' field must be set.");
    	if(p.getProfile().getUser().getUsername()==null)
    		throw new Exception("'profile.user.username' field must be set.");
    	if(!authPart.getUserName().equals(p.getProfile().getUser().getUsername()) &&
    		!authPart.getUserName().equals(getConfig(CFG_ADMIN))) {
    		throw new Exception("only the user '"+p.getProfile().getUser().getUsername()+
    				"'or an admin can update this profile");
    	}
    	
    	db.setProfile(p.getProfile());
    	
        //END set_user_profile
    }

    /**
     * <p>Original spec-file function name: update_user_profile</p>
     * <pre>
     * Update the UserProfile for the user indicated in the User field of the UserProfile
     * object.  This operation can only be performed if authenticated as the user in
     * the UserProfile or as the admin account of this service.
     * If the profile does not exist, one will be created.  If it does already exist,
     * then the specified top-level fields in profile will be updated.
     * todo: add some way to remove fields.  Fields in profile can only be modified or added.
     * </pre>
     * @param   p   instance of type {@link us.kbase.userprofile.SetUserProfileParams SetUserProfileParams}
     */
    @JsonServerMethod(rpc = "UserProfile.update_user_profile")
    public void updateUserProfile(SetUserProfileParams p, AuthToken authPart) throws Exception {
        //BEGIN update_user_profile
    	if(p.getProfile()==null)
    		throw new Exception("'profile' field must be set.");
    	if(p.getProfile().getUser()==null)
    		throw new Exception("'profile.user' field must be set.");
    	if(p.getProfile().getUser().getUsername()==null)
    		throw new Exception("'profile.user.username' field must be set.");
    	if(!authPart.getUserName().equals(p.getProfile().getUser().getUsername()) &&
    		!authPart.getUserName().equals(getConfig(CFG_ADMIN))) {
    		throw new Exception("only the user '"+p.getProfile().getUser().getUsername()+
    				"'or an admin can update this profile");
    	}
    	
    	db.updateProfile(p.getProfile());
        //END update_user_profile
    }

    /**
     * <p>Original spec-file function name: lookup_globus_user</p>
     * <pre>
     * </pre>
     * @param   usernames   instance of list of original type "username"
     * @return   parameter "users" of mapping from original type "username" to type {@link us.kbase.userprofile.GlobusUser GlobusUser}
     */
    @JsonServerMethod(rpc = "UserProfile.lookup_globus_user")
    public Map<String,GlobusUser> lookupGlobusUser(List<String> usernames, AuthToken authPart) throws Exception {
        Map<String,GlobusUser> returnVal = null;
        //BEGIN lookup_globus_user
    	Map<String, UserDetail> data = AuthService.fetchUserDetail(usernames, authPart);
    	returnVal = new HashMap<String, GlobusUser>(data.size());

    	for (UserDetail ud : data.values()) {
    		if(ud!=null) {
	    		GlobusUser gu = new GlobusUser().withUserName(ud.getUserName());
	    		if(ud.getEmail()!=null) { gu.setEmail(ud.getEmail()); }
	    		if(ud.getFullName()!=null) { gu.setFullName(ud.getFullName()); }
	    		returnVal.put(ud.getUserName(), gu);
    		}
    	}
        //END lookup_globus_user
        return returnVal;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: <program> <server_port>");
            return;
        }
        new UserProfileServer().startupServer(Integer.parseInt(args[0]));
    }
}
