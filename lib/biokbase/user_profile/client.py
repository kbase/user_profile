# -*- coding: utf-8 -*-
############################################################
#
# Autogenerated by the KBase type compiler -
# any changes made here will be overwritten
#
############################################################

from __future__ import print_function
# the following is a hack to get the baseclient to import whether we're in a
# package or not. This makes pep8 unhappy hence the annotations.
try:
    # baseclient and this client are in a package
    from .baseclient import BaseClient as _BaseClient  # @UnusedImport
except:
    # no they aren't
    from baseclient import BaseClient as _BaseClient  # @Reimport


class UserProfile(object):

    def __init__(
            self, url=None, timeout=30 * 60, user_id=None,
            password=None, token=None, ignore_authrc=False,
            trust_all_ssl_certificates=False,
            auth_svc='https://kbase.us/services/authorization/Sessions/Login'):
        if url is None:
            url = 'https://kbase.us/services/user_profile/rpc'
        self._service_ver = None
        self._client = _BaseClient(
            url, timeout=timeout, user_id=user_id, password=password,
            token=token, ignore_authrc=ignore_authrc,
            trust_all_ssl_certificates=trust_all_ssl_certificates,
            auth_svc=auth_svc)

    def ver(self, context=None):
        """
        :returns: instance of String
        """
        return self._client.call_method(
            'UserProfile.ver',
            [], self._service_ver, context)

    def filter_users(self, p, context=None):
        """
        Returns a list of users matching the filter.  If the 'filter' field
        is empty or null, then this will return all Users.  The filter will
        match substrings in usernames and realnames.
        :param p: instance of type "FilterParams" -> structure: parameter
           "filter" of String
        :returns: instance of list of type "User" -> structure: parameter
           "username" of type "username", parameter "realname" of type
           "realname", parameter "thumbnail" of String
        """
        return self._client.call_method(
            'UserProfile.filter_users',
            [p], self._service_ver, context)

    def get_user_profile(self, usernames, context=None):
        """
        Given a list of usernames, returns a list of UserProfiles in the same order.
        If no UserProfile was found for a username, the UserProfile at that position will
        be null.
        :param usernames: instance of list of type "username"
        :returns: instance of list of type "UserProfile" -> structure:
           parameter "user" of type "User" -> structure: parameter "username"
           of type "username", parameter "realname" of type "realname",
           parameter "thumbnail" of String, parameter "profile" of
           unspecified object
        """
        return self._client.call_method(
            'UserProfile.get_user_profile',
            [usernames], self._service_ver, context)

    def set_user_profile(self, p, context=None):
        """
        Set the UserProfile for the user indicated in the User field of the UserProfile
        object.  This operation can only be performed if authenticated as the user in
        the UserProfile or as the admin account of this service.
        If the profile does not exist, one will be created.  If it does already exist,
        then the entire user profile will be replaced with the new profile.
        :param p: instance of type "SetUserProfileParams" -> structure:
           parameter "profile" of type "UserProfile" -> structure: parameter
           "user" of type "User" -> structure: parameter "username" of type
           "username", parameter "realname" of type "realname", parameter
           "thumbnail" of String, parameter "profile" of unspecified object
        """
        return self._client.call_method(
            'UserProfile.set_user_profile',
            [p], self._service_ver, context)

    def update_user_profile(self, p, context=None):
        """
        Update the UserProfile for the user indicated in the User field of the UserProfile
        object.  This operation can only be performed if authenticated as the user in
        the UserProfile or as the admin account of this service.
        If the profile does not exist, one will be created.  If it does already exist,
        then the specified top-level fields in profile will be updated.
        todo: add some way to remove fields.  Fields in profile can only be modified or added.
        :param p: instance of type "SetUserProfileParams" -> structure:
           parameter "profile" of type "UserProfile" -> structure: parameter
           "user" of type "User" -> structure: parameter "username" of type
           "username", parameter "realname" of type "realname", parameter
           "thumbnail" of String, parameter "profile" of unspecified object
        """
        return self._client.call_method(
            'UserProfile.update_user_profile',
            [p], self._service_ver, context)

    def lookup_globus_user(self, usernames, context=None):
        """
        :param usernames: instance of list of type "username"
        :returns: instance of mapping from type "username" to type
           "GlobusUser" -> structure: parameter "email" of String, parameter
           "fullName" of String, parameter "userName" of String
        """
        return self._client.call_method(
            'UserProfile.lookup_globus_user',
            [usernames], self._service_ver, context)

    def status(self, context=None):
        return self._client.call_method('UserProfile.status',
                                        [], self._service_ver, context)
