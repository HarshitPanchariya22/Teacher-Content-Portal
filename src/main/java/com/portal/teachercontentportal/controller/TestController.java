package com.portal.teachercontentportal.controller;

import com.portal.teachercontentportal.service.Extraction;
import com.portal.teachercontentportal.service.HashService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/test")
public class TestController {
    private final Extraction extraction;
    private final HashService hashService;
    public TestController(Extraction extraction,HashService hashService)
    {
        this.extraction = extraction;
        this.hashService = hashService;
    }
    @PostMapping("/extract")
    public String testExtraction(@RequestParam("file")MultipartFile file)
    {
        String raw = extraction.extractAndNormlaize(file);
        String hash = hashService.generateHash(raw);
        return "hash\\ : "+hash;
    }
}
