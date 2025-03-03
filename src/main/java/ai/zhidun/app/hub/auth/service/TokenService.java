package ai.zhidun.app.hub.auth.service;

import ai.zhidun.app.hub.auth.config.JwtProperties;
import ai.zhidun.app.hub.common.BizError;
import ai.zhidun.app.hub.common.BizException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.client.util.CommonUtils;
import org.apereo.cas.client.validation.*;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.spec.InvalidKeySpecException;

@Service
@Slf4j
public class TokenService {

    private record JwtSerde(JsonWebSignature jws, JwtConsumer consumer) {

    }

    private final ThreadLocal<JwtSerde> holder;

    private final PublicJsonWebKey key;

    private final JwtProperties properties;

    public TokenService(JwtProperties properties, CasUserDetailsService detailsService) {
        this.detailsService = detailsService;
        this.holder = ThreadLocal.withInitial(this::newSerde);
        this.properties = properties;
        try {
            key = init(properties);
        } catch (IOException | InvalidKeySpecException | JoseException e) {
            throw new RuntimeException(e);
        }
    }

    private static PublicJsonWebKey init(JwtProperties properties)
            throws IOException, JoseException, InvalidKeySpecException {
        Path path = properties.jwtKey();
        if (path.toFile().exists()) {
            // recover from file
            return (PublicJsonWebKey) JsonWebKey.Factory.newJwk(Files.readString(path));
        } else {
            // new key and save to file
            EllipticCurveJsonWebKey curveJsonWebKey = EcJwkGenerator.generateJwk(EllipticCurves.P256);
            Files.writeString(path,
                    curveJsonWebKey.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
            curveJsonWebKey.setKeyId(properties.keyId());
            return curveJsonWebKey;
        }
    }

    private JwtSerde newSerde() {
        JsonWebSignature jws = new JsonWebSignature();

        jws.setKey(key.getPrivateKey());
        jws.setKeyIdHeaderValue(key.getKeyId());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);

        JwtConsumer consumer = new JwtConsumerBuilder()
                // the JWT must have an expiration time
                .setRequireExpirationTime()
                // allow some leeway in validating time based claims to account for clock skew
                .setAllowedClockSkewInSeconds(30)
                // the JWT must have a subject claim
                .setRequireSubject()
                // whom the JWT needs to have been issued by
                .setExpectedIssuer(properties.issuer())
                // to whom the JWT is intended for
                .setExpectedAudience(properties.audience())
                // verify the signature with the public key
                .setVerificationKey(key.getPublicKey())
                // only allow the expected signature algorithm(s) in the given context
                // which is only ECDSA_USING_P256_CURVE_AND_SHA256 here
                .setJwsAlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT,
                        AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256)
                // create the JwtConsumer instance
                .build();

        return new JwtSerde(jws, consumer);
    }

    public String encode(YsUserDetail info) {
        // Create the Claims, which will be the content of the JWT
        JwtClaims claims = new JwtClaims();
        // who creates the token and signs it
        claims.setIssuer(properties.issuer());
        // to whom the token is intended to be sent
        claims.setAudience(properties.audience());
        // time when the token will expire (10 minutes from now)
        claims.setExpirationTimeMinutesInTheFuture(properties.expiration().toMinutes());
        // a unique identifier for the token
        claims.setGeneratedJwtId();
        // when the token was issued/created (now)
        claims.setIssuedAtToNow();
        // time before which the token is not yet valid (2 minutes ago)
        claims.setNotBeforeMinutesInThePast(2);
        // the subject/principal is whom the token is about
        claims.setSubject(info.userName());
        claims.setClaim("userId", info.userId());
        claims.setClaim("st", info.st());
        claims.setClaim("permit", info.permit());
        JwtSerde jwtSerde = holder.get();
        jwtSerde.jws.setPayload(claims.toJson());
        try {
            return jwtSerde.jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR, BizError.error("jwt token generate failed"), e);
        }
    }

    public record AuthedClaimInfo(String name, String userId, int permit, String st, NumericDate expirationTime) {

    }

    public AuthedClaimInfo decode(String jwt) throws InvalidJwtException {
        try {
            JwtClaims claims = holder.get().consumer().processToClaims(jwt);
            String subject = claims.getSubject();
            String userId = claims.getClaimValue("userId", String.class);
            String st = claims.getClaimValue("st", String.class);
            int permit = Math.toIntExact(claims.getClaimValue("permit", Long.class));
            return new AuthedClaimInfo(subject, userId, permit, st, claims.getExpirationTime());
        } catch (MalformedClaimException e) {
            throw new RuntimeException(e);
        }
    }

    private final CasUserDetailsService detailsService;

    @Value("${cas.base.url}")
    private String casBaseUrl;

    @Value("${cas.login.url}")
    private String casLoginUrl;

    @Value("${cas.service:http://localhost:${server.port}/api/auth/token}")
    private String service;

    private TicketValidator ticketValidator;

    @Value("${cas.validate.url}")
    private String casValidateUrl;

    @Value("${cas.validate.type}")
    private String casValidateType;

    @PostConstruct
    public void init() {
        if ("cas3".equalsIgnoreCase(casValidateType)) {
            this.ticketValidator = new Cas30ServiceTicketValidator(casValidateUrl);
        } else if ("cas2".equalsIgnoreCase(casValidateType)) {
            this.ticketValidator = new Cas20ServiceTicketValidator(casValidateUrl);
        } else if ("cas1".equalsIgnoreCase(casValidateType)) {
            this.ticketValidator = new Cas10TicketValidator(casValidateUrl);
        } else {
            log.warn("unknown cas validate type: {} use Cas3.0", casValidateType);
            this.ticketValidator = new Cas30ServiceTicketValidator(casValidateUrl);
        }
    }

    public record TokenResult(String token, YsUserDetail detail) {

    }

    public TokenResult token(String serverTicket, String service) {
        try {
            service = service == null ? this.service : service;
            Assertion assertion = ticketValidator.validate(serverTicket, service);
            YsUserDetail detail = detailsService.loadUserDetails(new CasAssertionAuthenticationToken(assertion, serverTicket));
            return new TokenResult(encode(detail), detail);
        } catch (TicketValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Value("${cas.login.url}")
    private String loginUrl;

    @Bean
    public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(this.casLoginUrl);
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
    }

    private ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(service);
        return serviceProperties;
    }

    private String createRedirectUrl() {
        ServiceProperties serviceProperties = serviceProperties();
        return CommonUtils.constructRedirectUrl(
                this.loginUrl,  serviceProperties.getServiceParameter(), serviceProperties.getService(),
                serviceProperties.isSendRenew(), false);
    }

    public String loginUrl() {
        return createRedirectUrl();
    }
}
