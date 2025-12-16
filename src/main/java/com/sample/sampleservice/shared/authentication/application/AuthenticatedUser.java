package com.sample.sampleservice.shared.authentication.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.sample.sampleservice.shared.authentication.domain.Role;
import com.sample.sampleservice.shared.authentication.domain.Roles;
import com.sample.sampleservice.shared.authentication.domain.User;
import com.sample.sampleservice.shared.authentication.domain.Username;
import com.sample.sampleservice.shared.error.domain.Assert;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is a utility class to get authenticated user information
 */
@Slf4j
public final class AuthenticatedUser {

    public static final String PREFERRED_USERNAME = "preferred_username";

    public static final String API_KEY = "api_key";

    private AuthenticatedUser() {
    }

    /**
     * Get the authenticated user username
     *
     * @return The authenticated user username
     * @throws NotAuthenticatedUserException  if the user is not authenticated
     * @throws UnknownAuthenticationException if the user uses an unknown authentication scheme
     */
    public static Username username() {
        return optionalUsername().orElseThrow(NotAuthenticatedUserException::new);
    }

    
    /**
     * Checks if the authenticated user is a client registered from keycloak or just normal user
     * It is used for service to service communication
     *
     * @return Boolean true (Authenticated User is Service Account)
     */
    public static boolean isServiceAccount() {
        Map<String, Object> claims = attributes();
        return claims.containsKey("client_id") && 
            claims.get("client_id").equals(claims.get("azp"));
    }

    /**
     * Get the authenticated user username
     *
     * @return The authenticated user username or empty if the user is not authenticated
     * @throws UnknownAuthenticationException if the user uses an unknown authentication scheme
     */
    public static Optional<Username> optionalUsername() {
        return authentication().map(AuthenticatedUser::readPrincipal).flatMap(Username::of);
    }

    /**
     * Get the authenticated user username
     *
     * @return The authenticated user username
     * @throws NotAuthenticatedUserException  if the user is not authenticated
     * @throws UnknownAuthenticationException if the user uses an unknown authentication scheme
     */
    public static String apiKey() {
        return optionalApiKey().orElseThrow(NotAuthenticatedUserException::new);
    }

    /**
     * Get the authenticated user username
     *
     * @return The authenticated user username or empty if the user is not authenticated
     * @throws UnknownAuthenticationException if the user uses an unknown authentication scheme
     */
    public static Optional<String> optionalApiKey() {
        return authentication().map(AuthenticatedUser::readApiKey);
    }

    /**
     * Get the token
     *
     * @return The access token or empty if no access token
     * @throws UnknownAuthenticationException if the user uses an unknown authentication scheme
     */
    public static Optional<String> optionalToken() {
        return authentication().map(AuthenticatedUser::readToken);
    }

    /**
     * Read user principal from authentication
     *
     * @param authentication authentication to read the principal from
     * @return The user principal
     * @throws UnknownAuthenticationException if the authentication can't be read (unknown token type)
     */
    public static String readPrincipal(Authentication authentication) {
        Assert.notNull("authentication", authentication);

        if (authentication.getPrincipal() instanceof UserDetails details) {
            return details.getUsername();
        }

        if (authentication instanceof JwtAuthenticationToken token) {
            return (String) token.getToken().getClaims().get(PREFERRED_USERNAME);
        }

        if (authentication.getPrincipal() instanceof DefaultOidcUser oidcUser) {
            return (String) oidcUser.getAttributes().get(PREFERRED_USERNAME);
        }

        if (authentication.getPrincipal() instanceof String principal) {
            return principal;
        }

        throw new UnknownAuthenticationException();
    }

    /**
     * Read api key principal from authentication
     *
     * @param authentication authentication to read the principal from
     * @return The user principal
     * @throws UnknownAuthenticationException if the authentication can't be read (unknown token type)
     */
    public static String readApiKey(Authentication authentication) {
        Assert.notNull("authentication", authentication);

        if (authentication.getPrincipal() instanceof UserDetails details) {
            return details.getUsername();
        }

        if (authentication instanceof JwtAuthenticationToken token) {
            return (String) token.getToken().getClaims().get(API_KEY);
        }

        if (authentication.getPrincipal() instanceof DefaultOidcUser oidcUser) {
            return (String) oidcUser.getAttributes().get(API_KEY);
        }

        if (authentication.getPrincipal() instanceof String principal) {
            return principal;
        }

        throw new UnknownAuthenticationException();
    }

    /**
     * Read api key principal from authentication
     *
     * @param authentication authentication to read the principal from
     * @return The user principal
     * @throws UnknownAuthenticationException if the authentication can't be read (unknown token type)
     */
    public static String readToken(Authentication authentication) {
        Assert.notNull("authentication", authentication);

        if (authentication instanceof JwtAuthenticationToken token) {
            return token.getToken().toString();
        }

        if (authentication.getPrincipal() instanceof DefaultOidcUser oidcUser) {
            return oidcUser.getAccessTokenHash();
        }

        throw new UnknownAuthenticationException();
    }

    /**
     * Get the authenticated user roles
     *
     * @return The authenticated user roles or empty roles if the user is not authenticated
     */
    public static Roles roles() {
        return authentication().map(toRoles()).orElse(Roles.EMPTY);
    }

    private static Function<Authentication, Roles> toRoles() {
        return authentication ->
                new Roles(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).map(Role::from).collect(Collectors.toSet()));
    }

    public static void can(Role role) {
        if (!roles().hasRole(role)) {
            throw new AccessDeniedException("The user does not have the necessary authorization to perform this action.");
        }
    }

    /**
     * Get the authenticated user token attributes
     *
     * @return The authenticated user token attributes
     * @throws NotAuthenticatedUserException  if the user is not authenticated
     * @throws UnknownAuthenticationException if the authentication scheme is unknown
     */
    public static Map<String, Object> attributes() {
        Authentication token = authentication().orElseThrow(NotAuthenticatedUserException::new);

        if (token instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {
            return oAuth2AuthenticationToken.getPrincipal().getAttributes();
        }

        if (token instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getTokenAttributes();
        }

        throw new UnknownAuthenticationException();
    }

    public static User getUser() {
        Map<String, Object> details = attributes();

        Boolean activated = Boolean.TRUE;
        String sub = String.valueOf(details.get("sub"));
        String username = null;
        if (details.get("preferred_username") != null) {
            username = ((String) details.get("preferred_username")).toLowerCase();
        }
        // handle resource server JWT, where subclaim is email and uid is ID
        String userId;
        if (details.get("uid") != null) {
            userId = (String) details.get("uid");
            username = sub;
        } else {
            userId = sub;
        }

        String firstName = null;
        if (details.get("given_name") != null) {
            firstName = (String) details.get("given_name");
        } else if (details.get("name") != null) {
            firstName = (String) details.get("name");
        }
        String lastName = null;
        if (details.get("family_name") != null) {
            lastName = (String) details.get("family_name");
        }
        if (details.get("email_verified") != null) {
            activated = (Boolean) details.get("email_verified");
        }
        String email;
        if (details.get("email") != null) {
            email = ((String) details.get("email")).toLowerCase();
        } else if (sub.contains("|") && (username != null && username.contains("@"))) {
            // special handling for Auth0
            email = username;
        } else {
            email = sub;
        }

        String langKey;
        if (details.get("langKey") != null) {
            langKey = (String) details.get("langKey");
        } else if (details.get("locale") != null) {
            // trim off country code if it exists
            String locale = (String) details.get("locale");
            if (locale.contains("_")) {
                locale = locale.substring(0, locale.indexOf('_'));
            } else if (locale.contains("-")) {
                locale = locale.substring(0, locale.indexOf('-'));
            }
            langKey = locale.toLowerCase();
        } else {
            // set langKey to default if not specified by IdP
            langKey = "en";
        }

        String imageUrl = "";
        if (details.get("picture") != null) {
            imageUrl = (String) details.get("picture");
        }
        return new User(userId, firstName, lastName, username, email, langKey, imageUrl, activated);
    }

    private static Optional<Authentication> authentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }
}
