package com.sample.sampleservice.feature.auth.domain.model;

public record CredentialRepresentation(String id, String type, String userLabel, long createdDate, String secretData,
                                       String credentialData, int priority, String value, boolean temporary,
                                       String device, String hashedSaltedValue, String salt, int hashIterations,
                                       int counter, String algorithm, int digits, int period,
                                       MultivaluedHashMapStringString config, String federationLink) {
}
