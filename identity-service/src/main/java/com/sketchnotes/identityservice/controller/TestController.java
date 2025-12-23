package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.utils.PasswordEncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestController {
    
    private final PasswordEncryptionUtil passwordEncryptionUtil;
    
    @GetMapping("/encrypt")
    public Map<String, String> encryptPassword(@RequestParam String password) {
        String encrypted = passwordEncryptionUtil.encrypt(password);
        
        Map<String, String> result = new HashMap<>();
        result.put("password", password);
        result.put("encrypted", encrypted);
        
        return result;
    }
}
