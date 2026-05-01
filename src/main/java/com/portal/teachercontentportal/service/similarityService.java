package com.portal.teachercontentportal.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class similarityService {
    public double CalculateSimilarity(String text1, String tex2)
    {
        if(text1 == null || tex2 == null) return 0.0;

        Set<String> s1 = new HashSet<>(Arrays.asList(text1.split(" ")));
        Set<String> s2 = new HashSet<>(Arrays.asList(tex2.split(" ")));
        if(s1.isEmpty() || s2.isEmpty()) return  0.0;
        Set<String> intersection = new HashSet<>(s1);
        intersection.retainAll(s2); // all the common words
        Set<String> union = new HashSet<>(s1);
        union.addAll(s2); // all the unique words
        return (double) intersection.size()/union.size(); // this is similarity common/unique
    }
}
