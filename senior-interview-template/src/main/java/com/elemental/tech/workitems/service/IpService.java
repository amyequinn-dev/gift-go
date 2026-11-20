package com.elemental.tech.workitems.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class IpService {

    public ResponseEntity<String> checkIp(String ipAddress) {

        Map response = checkIpAddress(ipAddress);

        String country = (String) response.get("countryCode");
        String isp = (String) response.get("isp");
        String org = (String) response.get("org");

        if (isBlockedCountry(country)) {
            return ResponseEntity.status(403)
                    .body("Blocked country: " + country);
        }

        if (isBlockedISP(isp, org)) {
            return ResponseEntity.status(403)
                    .body("Blocked ISP: " + isp);
        }

        return ResponseEntity.ok("IP allowed: " + ipAddress);
    }

    private Map checkIpAddress(String ipAddress) {

        RestClient restClient = RestClient.create();

        return restClient.get()
                .uri("http://ip-api.com/json/" + ipAddress)
                .retrieve()
                .body(Map.class);
    }

    private boolean isBlockedCountry(String country) {
        return ("CN").equals(country) || ("ES").equals(country) || ("US").equals(country);

    }

    private boolean isBlockedISP(String isp, String org) {
        if (isp == null || org == null) {
            return false;
        }
        return (isp.contains("Amazon") && org.contains("AWS"))
                || (isp.contains("Google") && org.contains("Google Cloud"))
                || (isp.contains("Microsoft") && org.contains("Azure"));

    }


}
