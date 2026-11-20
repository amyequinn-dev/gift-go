package com.elemental.tech.workitems.api;

import com.elemental.tech.workitems.service.IpService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/work-items")
public class IpController {
    private final IpService ipService;

    public IpController(IpService ipService) {
        this.ipService = ipService;
    }

    @GetMapping("/ip")
    public ResponseEntity<String> checkIp(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        return ipService.checkIp(ipAddress);
    }
}

