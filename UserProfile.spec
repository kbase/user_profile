

module UserProfile {

    /* @range [0,1] */
    typedef int bool;

    typedef string username;
    typedef string realname;
    
    
    typedef structure {
        username username;
        realname realname;
        string thumbnail;
    } User;
    
    typedef structure {
        User user;
        UnspecifiedObject profile;
    } UserProfile;


    funcdef ver() returns (string);
    
    typedef structure {
        string filter;
    } FilterParams;
    
    /*
        Returns a list of users matching the filter.  If the 'filter' field
        is empty or null, then this will return all Users.  The filter will
        match substrings in usernames and realnames.
    */
    funcdef filter_users(FilterParams p) returns (list<User> users);
    
    /*
        Given a list of usernames, returns a list of UserProfiles in the same order.
        If no UserProfile was found for a username, the UserProfile at that position will
        be null.
    */
    funcdef get_user_profile(list <username> usernames) returns (list<UserProfile> profiles);
    
    
    typedef structure {
        UserProfile profile;
    } SetUserProfileParams;
    
    /*
        Set the UserProfile for the user indicated in the User field of the UserProfile
        object.  This operation can only be performed if authenticated as the user in
        the UserProfile or as the admin account of this service.
        
        If the profile does not exist, one will be created.  If it does already exist,
        then the entire user profile will be replaced with the new profile.
    */
    funcdef set_user_profile(SetUserProfileParams p) returns () authentication required;

	/*
        Update the UserProfile for the user indicated in the User field of the UserProfile
        object.  This operation can only be performed if authenticated as the user in
        the UserProfile or as the admin account of this service.
        
        If the profile does not exist, one will be created.  If it does already exist,
        then the specified top-level fields in profile will be updated.
        
        todo: add some way to remove fields.  Fields in profile can only be modified or added.
    */
    funcdef update_user_profile(SetUserProfileParams p) returns () authentication required;


	typedef structure {
		string email;
		string fullName;
		string userName;
	} GlobusUser;

	funcdef lookup_globus_user(list <username> usernames) returns (mapping <username,GlobusUser> users) authentication required;
};

