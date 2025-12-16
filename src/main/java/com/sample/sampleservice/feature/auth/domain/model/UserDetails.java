package com.sample.sampleservice.feature.auth.domain.model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails implements Serializable {

    @Serial
    private static final long serialVersionUID = -1608009883525411542L;

    private String id;

    private long createdTimestamp;

    private String username;

    private boolean enabled;

    private boolean totp;

    private boolean emailVerified;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private List<String> requiredActions;

    private int notBefore;

    private Map<String, Boolean> access;

    private Map<String, List<String>> attributes;

    private UserProfileMetadata userProfileMetadata;

    private String self;

    private String origin;

    private String federationLink;

    private String serviceAccountClientId;

    private List<CredentialRepresentation> credentials;

    private List<String> disableableCredentialTypes;

    private List<FederatedIdentityRepresentation> federatedIdentities;

    private List<String> realmRoles;

    private Map<String, List<String>> clientRoles;

    private List<UserConsentRepresentation> clientConsents;

    private Map<String, List<String>> applicationRoles;

    private List<SocialLinkRepresentation> socialLinks;

    private List<String> groups;

    public enum RequiredAction {
        UPDATE_PASSWORD
    }

    @Data
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccessDto implements Serializable {

        @Serial
        private static final long serialVersionUID = 7416126919078029231L;

        private boolean manageGroupMembership;

        private boolean view;

        private boolean mapRoles;

        private boolean impersonate;

        private boolean manage;
    }
}
