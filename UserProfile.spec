

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
        bool replace;
    } SetUserProfileParams;
    
    /*
        Set the UserProfile for the user indicated in the User field of the UserProfile
        object.  This operation can only be performed if authenticated as the user in
        the UserProfile or as the admin account of this service.
        
        In the parameters, if replace is set to 1, then the entire profile will be
        replaced by the new profile.  If replace is set to 0, then only the fields specified
        in the new UserProfile will be updated.
    */
    funcdef set_user_profile(SetUserProfileParams) returns () authentication required;

};

