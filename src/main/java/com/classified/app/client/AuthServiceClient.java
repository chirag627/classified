package com.classified.app.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    private static final String MC_BASE = "https://cpaas.messagecentral.com";

    @Value("${app.message-central.token}")
    private String mcToken;

    @Value("${app.message-central.country-code:91}")
    private String defaultCountryCode;

    /**
     * Sends an OTP via MessageCentral and returns the verificationId.
     * Returns null on failure.
     */
    public String sendOtp(String countryCode, String mobileNumber) {
        try {
            String cc = (countryCode != null && !countryCode.isBlank()) ? countryCode : defaultCountryCode;
            String url = UriComponentsBuilder
                    .fromHttpUrl(MC_BASE + "/verification/v3/send")
                    .queryParam("countryCode", cc)
                    .queryParam("flowType", "SMS")
                    .queryParam("mobileNumber", mobileNumber)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("authToken", mcToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            log.info("MC sendOtp status={} body={}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
                if (data != null) {
                    Object vid = data.get("verificationId");
                    if (vid != null) {
                        log.info("OTP sent to {}{}, verificationId={}", cc, mobileNumber, vid);
                        return vid.toString();
                    }
                }
            }
            log.error("MC sendOtp response: {}", response.getBody());
            return null;
        } catch (Exception e) {
            log.error("Failed to send OTP via MessageCentral: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validates the OTP entered by the user against MessageCentral.
     */
    public boolean verifyOtp(String countryCode, String verificationId, String otpCode) {
        try {
            String cc = (countryCode != null && !countryCode.isBlank()) ? countryCode : defaultCountryCode;
            String url = UriComponentsBuilder
                    .fromHttpUrl(MC_BASE + "/verification/v3/validateOtp")
                    .queryParam("countryCode", cc)
                    .queryParam("verificationId", verificationId)
                    .queryParam("code", otpCode)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("authToken", mcToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            log.info("MC verifyOtp status={} body={}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
                if (data != null) {
                    Object verificationStatus = data.get("verificationStatus");
                    log.info("MC verificationStatus={}", verificationStatus);
                    return "VERIFICATION_COMPLETED".equals(verificationStatus);
                }
            }
            log.error("MC verifyOtp response: {}", response.getBody());
            return false;
        } catch (Exception e) {
            log.error("Failed to verify OTP via MessageCentral: {}", e.getMessage());
            return false;
        }
    }
}

