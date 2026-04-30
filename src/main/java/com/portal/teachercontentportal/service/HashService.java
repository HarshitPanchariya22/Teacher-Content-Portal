package com.portal.teachercontentportal.service;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;

@Service
public class HashService {
    public String generateHash(String text)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] HashByte = md.digest(text.getBytes());
            StringBuilder hex = new StringBuilder();
            for(byte b : HashByte)
            {
                hex.append(String.format("%02x",b));
            }
            return hex.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Hash generation failed", e);
        }

    }
}
