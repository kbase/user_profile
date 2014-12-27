package Bio::KBase::UserProfile::Client;

use JSON::RPC::Client;
use POSIX;
use strict;
use Data::Dumper;
use URI;
use Bio::KBase::Exceptions;
my $get_time = sub { time, 0 };
eval {
    require Time::HiRes;
    $get_time = sub { Time::HiRes::gettimeofday() };
};

use Bio::KBase::AuthToken;

# Client version should match Impl version
# This is a Semantic Version number,
# http://semver.org
our $VERSION = "0.1.0";

=head1 NAME

Bio::KBase::UserProfile::Client

=head1 DESCRIPTION





=cut

sub new
{
    my($class, $url, @args) = @_;
    
    if (!defined($url))
    {
	$url = 'https://kbase.us/services/user_profile/rpc';
    }

    my $self = {
	client => Bio::KBase::UserProfile::Client::RpcClient->new,
	url => $url,
	headers => [],
    };

    chomp($self->{hostname} = `hostname`);
    $self->{hostname} ||= 'unknown-host';

    #
    # Set up for propagating KBRPC_TAG and KBRPC_METADATA environment variables through
    # to invoked services. If these values are not set, we create a new tag
    # and a metadata field with basic information about the invoking script.
    #
    if ($ENV{KBRPC_TAG})
    {
	$self->{kbrpc_tag} = $ENV{KBRPC_TAG};
    }
    else
    {
	my ($t, $us) = &$get_time();
	$us = sprintf("%06d", $us);
	my $ts = strftime("%Y-%m-%dT%H:%M:%S.${us}Z", gmtime $t);
	$self->{kbrpc_tag} = "C:$0:$self->{hostname}:$$:$ts";
    }
    push(@{$self->{headers}}, 'Kbrpc-Tag', $self->{kbrpc_tag});

    if ($ENV{KBRPC_METADATA})
    {
	$self->{kbrpc_metadata} = $ENV{KBRPC_METADATA};
	push(@{$self->{headers}}, 'Kbrpc-Metadata', $self->{kbrpc_metadata});
    }

    if ($ENV{KBRPC_ERROR_DEST})
    {
	$self->{kbrpc_error_dest} = $ENV{KBRPC_ERROR_DEST};
	push(@{$self->{headers}}, 'Kbrpc-Errordest', $self->{kbrpc_error_dest});
    }

    #
    # This module requires authentication.
    #
    # We create an auth token, passing through the arguments that we were (hopefully) given.

    {
	my $token = Bio::KBase::AuthToken->new(@args);
	
	if (!$token->error_message)
	{
	    $self->{token} = $token->token;
	    $self->{client}->{token} = $token->token;
	}
    }

    my $ua = $self->{client}->ua;	 
    my $timeout = $ENV{CDMI_TIMEOUT} || (30 * 60);	 
    $ua->timeout($timeout);
    bless $self, $class;
    #    $self->_validate_version();
    return $self;
}




=head2 ver

  $return = $obj->ver()

=over 4

=item Parameter and return types

=begin html

<pre>
$return is a string

</pre>

=end html

=begin text

$return is a string


=end text

=item Description



=back

=cut

sub ver
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 0)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function ver (received $n, expecting 0)");
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "UserProfile.ver",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'ver',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method ver",
					    status_line => $self->{client}->status_line,
					    method_name => 'ver',
				       );
    }
}



=head2 filter_users

  $users = $obj->filter_users($p)

=over 4

=item Parameter and return types

=begin html

<pre>
$p is a UserProfile.FilterParams
$users is a reference to a list where each element is a UserProfile.User
FilterParams is a reference to a hash where the following keys are defined:
	filter has a value which is a string
User is a reference to a hash where the following keys are defined:
	username has a value which is a UserProfile.username
	realname has a value which is a UserProfile.realname
	thumbnail has a value which is a string
username is a string
realname is a string

</pre>

=end html

=begin text

$p is a UserProfile.FilterParams
$users is a reference to a list where each element is a UserProfile.User
FilterParams is a reference to a hash where the following keys are defined:
	filter has a value which is a string
User is a reference to a hash where the following keys are defined:
	username has a value which is a UserProfile.username
	realname has a value which is a UserProfile.realname
	thumbnail has a value which is a string
username is a string
realname is a string


=end text

=item Description

Returns a list of users matching the filter.  If the 'filter' field
is empty or null, then this will return all Users.  The filter will
match substrings in usernames and realnames.

=back

=cut

sub filter_users
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function filter_users (received $n, expecting 1)");
    }
    {
	my($p) = @args;

	my @_bad_arguments;
        (ref($p) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"p\" (value was \"$p\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to filter_users:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'filter_users');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "UserProfile.filter_users",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'filter_users',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method filter_users",
					    status_line => $self->{client}->status_line,
					    method_name => 'filter_users',
				       );
    }
}



=head2 get_user_profile

  $profiles = $obj->get_user_profile($usernames)

=over 4

=item Parameter and return types

=begin html

<pre>
$usernames is a reference to a list where each element is a UserProfile.username
$profiles is a reference to a list where each element is a UserProfile.UserProfile
username is a string
UserProfile is a reference to a hash where the following keys are defined:
	user has a value which is a UserProfile.User
	profile has a value which is an UnspecifiedObject, which can hold any non-null object
User is a reference to a hash where the following keys are defined:
	username has a value which is a UserProfile.username
	realname has a value which is a UserProfile.realname
	thumbnail has a value which is a string
realname is a string

</pre>

=end html

=begin text

$usernames is a reference to a list where each element is a UserProfile.username
$profiles is a reference to a list where each element is a UserProfile.UserProfile
username is a string
UserProfile is a reference to a hash where the following keys are defined:
	user has a value which is a UserProfile.User
	profile has a value which is an UnspecifiedObject, which can hold any non-null object
User is a reference to a hash where the following keys are defined:
	username has a value which is a UserProfile.username
	realname has a value which is a UserProfile.realname
	thumbnail has a value which is a string
realname is a string


=end text

=item Description

Given a list of usernames, returns a list of UserProfiles in the same order.
If no UserProfile was found for a username, the UserProfile at that position will
be null.

=back

=cut

sub get_user_profile
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function get_user_profile (received $n, expecting 1)");
    }
    {
	my($usernames) = @args;

	my @_bad_arguments;
        (ref($usernames) eq 'ARRAY') or push(@_bad_arguments, "Invalid type for argument 1 \"usernames\" (value was \"$usernames\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to get_user_profile:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'get_user_profile');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "UserProfile.get_user_profile",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'get_user_profile',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method get_user_profile",
					    status_line => $self->{client}->status_line,
					    method_name => 'get_user_profile',
				       );
    }
}



=head2 set_user_profile

  $obj->set_user_profile($p)

=over 4

=item Parameter and return types

=begin html

<pre>
$p is a UserProfile.SetUserProfileParams
SetUserProfileParams is a reference to a hash where the following keys are defined:
	profile has a value which is a UserProfile.UserProfile
UserProfile is a reference to a hash where the following keys are defined:
	user has a value which is a UserProfile.User
	profile has a value which is an UnspecifiedObject, which can hold any non-null object
User is a reference to a hash where the following keys are defined:
	username has a value which is a UserProfile.username
	realname has a value which is a UserProfile.realname
	thumbnail has a value which is a string
username is a string
realname is a string

</pre>

=end html

=begin text

$p is a UserProfile.SetUserProfileParams
SetUserProfileParams is a reference to a hash where the following keys are defined:
	profile has a value which is a UserProfile.UserProfile
UserProfile is a reference to a hash where the following keys are defined:
	user has a value which is a UserProfile.User
	profile has a value which is an UnspecifiedObject, which can hold any non-null object
User is a reference to a hash where the following keys are defined:
	username has a value which is a UserProfile.username
	realname has a value which is a UserProfile.realname
	thumbnail has a value which is a string
username is a string
realname is a string


=end text

=item Description

Set the UserProfile for the user indicated in the User field of the UserProfile
object.  This operation can only be performed if authenticated as the user in
the UserProfile or as the admin account of this service.

If the profile does not exist, one will be created.  If it does already exist,
then the entire user profile will be replaced with the new profile.

=back

=cut

sub set_user_profile
{
    my($self, @args) = @_;

# Authentication: required

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function set_user_profile (received $n, expecting 1)");
    }
    {
	my($p) = @args;

	my @_bad_arguments;
        (ref($p) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"p\" (value was \"$p\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to set_user_profile:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'set_user_profile');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "UserProfile.set_user_profile",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'set_user_profile',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return;
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method set_user_profile",
					    status_line => $self->{client}->status_line,
					    method_name => 'set_user_profile',
				       );
    }
}



=head2 update_user_profile

  $obj->update_user_profile($p)

=over 4

=item Parameter and return types

=begin html

<pre>
$p is a UserProfile.SetUserProfileParams
SetUserProfileParams is a reference to a hash where the following keys are defined:
	profile has a value which is a UserProfile.UserProfile
UserProfile is a reference to a hash where the following keys are defined:
	user has a value which is a UserProfile.User
	profile has a value which is an UnspecifiedObject, which can hold any non-null object
User is a reference to a hash where the following keys are defined:
	username has a value which is a UserProfile.username
	realname has a value which is a UserProfile.realname
	thumbnail has a value which is a string
username is a string
realname is a string

</pre>

=end html

=begin text

$p is a UserProfile.SetUserProfileParams
SetUserProfileParams is a reference to a hash where the following keys are defined:
	profile has a value which is a UserProfile.UserProfile
UserProfile is a reference to a hash where the following keys are defined:
	user has a value which is a UserProfile.User
	profile has a value which is an UnspecifiedObject, which can hold any non-null object
User is a reference to a hash where the following keys are defined:
	username has a value which is a UserProfile.username
	realname has a value which is a UserProfile.realname
	thumbnail has a value which is a string
username is a string
realname is a string


=end text

=item Description

Update the UserProfile for the user indicated in the User field of the UserProfile
object.  This operation can only be performed if authenticated as the user in
the UserProfile or as the admin account of this service.

If the profile does not exist, one will be created.  If it does already exist,
then the specified top-level fields in profile will be updated.

todo: add some way to remove fields.  Fields in profile can only be modified or added.

=back

=cut

sub update_user_profile
{
    my($self, @args) = @_;

# Authentication: required

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function update_user_profile (received $n, expecting 1)");
    }
    {
	my($p) = @args;

	my @_bad_arguments;
        (ref($p) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"p\" (value was \"$p\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to update_user_profile:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'update_user_profile');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "UserProfile.update_user_profile",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'update_user_profile',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return;
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method update_user_profile",
					    status_line => $self->{client}->status_line,
					    method_name => 'update_user_profile',
				       );
    }
}



sub version {
    my ($self) = @_;
    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
        method => "UserProfile.version",
        params => [],
    });
    if ($result) {
        if ($result->is_error) {
            Bio::KBase::Exceptions::JSONRPC->throw(
                error => $result->error_message,
                code => $result->content->{code},
                method_name => 'update_user_profile',
            );
        } else {
            return wantarray ? @{$result->result} : $result->result->[0];
        }
    } else {
        Bio::KBase::Exceptions::HTTP->throw(
            error => "Error invoking method update_user_profile",
            status_line => $self->{client}->status_line,
            method_name => 'update_user_profile',
        );
    }
}

sub _validate_version {
    my ($self) = @_;
    my $svr_version = $self->version();
    my $client_version = $VERSION;
    my ($cMajor, $cMinor) = split(/\./, $client_version);
    my ($sMajor, $sMinor) = split(/\./, $svr_version);
    if ($sMajor != $cMajor) {
        Bio::KBase::Exceptions::ClientServerIncompatible->throw(
            error => "Major version numbers differ.",
            server_version => $svr_version,
            client_version => $client_version
        );
    }
    if ($sMinor < $cMinor) {
        Bio::KBase::Exceptions::ClientServerIncompatible->throw(
            error => "Client minor version greater than Server minor version.",
            server_version => $svr_version,
            client_version => $client_version
        );
    }
    if ($sMinor > $cMinor) {
        warn "New client version available for Bio::KBase::UserProfile::Client\n";
    }
    if ($sMajor == 0) {
        warn "Bio::KBase::UserProfile::Client version is $svr_version. API subject to change.\n";
    }
}

=head1 TYPES



=head2 bool

=over 4



=item Description

@range [0,1]


=item Definition

=begin html

<pre>
an int
</pre>

=end html

=begin text

an int

=end text

=back



=head2 username

=over 4



=item Definition

=begin html

<pre>
a string
</pre>

=end html

=begin text

a string

=end text

=back



=head2 realname

=over 4



=item Definition

=begin html

<pre>
a string
</pre>

=end html

=begin text

a string

=end text

=back



=head2 User

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
username has a value which is a UserProfile.username
realname has a value which is a UserProfile.realname
thumbnail has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
username has a value which is a UserProfile.username
realname has a value which is a UserProfile.realname
thumbnail has a value which is a string


=end text

=back



=head2 UserProfile

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
user has a value which is a UserProfile.User
profile has a value which is an UnspecifiedObject, which can hold any non-null object

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
user has a value which is a UserProfile.User
profile has a value which is an UnspecifiedObject, which can hold any non-null object


=end text

=back



=head2 FilterParams

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
filter has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
filter has a value which is a string


=end text

=back



=head2 SetUserProfileParams

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
profile has a value which is a UserProfile.UserProfile

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
profile has a value which is a UserProfile.UserProfile


=end text

=back



=cut

package Bio::KBase::UserProfile::Client::RpcClient;
use base 'JSON::RPC::Client';
use POSIX;
use strict;

#
# Override JSON::RPC::Client::call because it doesn't handle error returns properly.
#

sub call {
    my ($self, $uri, $headers, $obj) = @_;
    my $result;


    {
	if ($uri =~ /\?/) {
	    $result = $self->_get($uri);
	}
	else {
	    Carp::croak "not hashref." unless (ref $obj eq 'HASH');
	    $result = $self->_post($uri, $headers, $obj);
	}

    }

    my $service = $obj->{method} =~ /^system\./ if ( $obj );

    $self->status_line($result->status_line);

    if ($result->is_success) {

        return unless($result->content); # notification?

        if ($service) {
            return JSON::RPC::ServiceObject->new($result, $self->json);
        }

        return JSON::RPC::ReturnObject->new($result, $self->json);
    }
    elsif ($result->content_type eq 'application/json')
    {
        return JSON::RPC::ReturnObject->new($result, $self->json);
    }
    else {
        return;
    }
}


sub _post {
    my ($self, $uri, $headers, $obj) = @_;
    my $json = $self->json;

    $obj->{version} ||= $self->{version} || '1.1';

    if ($obj->{version} eq '1.0') {
        delete $obj->{version};
        if (exists $obj->{id}) {
            $self->id($obj->{id}) if ($obj->{id}); # if undef, it is notification.
        }
        else {
            $obj->{id} = $self->id || ($self->id('JSON::RPC::Client'));
        }
    }
    else {
        # $obj->{id} = $self->id if (defined $self->id);
	# Assign a random number to the id if one hasn't been set
	$obj->{id} = (defined $self->id) ? $self->id : substr(rand(),2);
    }

    my $content = $json->encode($obj);

    $self->ua->post(
        $uri,
        Content_Type   => $self->{content_type},
        Content        => $content,
        Accept         => 'application/json',
	@$headers,
	($self->{token} ? (Authorization => $self->{token}) : ()),
    );
}



1;
