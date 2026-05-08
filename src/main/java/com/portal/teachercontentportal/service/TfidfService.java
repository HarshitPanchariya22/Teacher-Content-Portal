package com.portal.teachercontentportal.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TfidfService {
    private static final Set<String> STOP_WORDS = Set.of(
            "the","is","a","an","and","of","to","in","on","for","with","by"
    );
    private List<String> tokenize(String text)
    {
        return Arrays.stream(text.split(" "))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .filter(word -> !STOP_WORDS.contains(word))
                .toList();
    }
    private Map<String,Double> computeTF(List<String>words)
    {
        Map<String,Double> tf= new HashMap<>();
        for(String word: words)
        {
            tf.put(word,tf.getOrDefault(word,0.0)+1);
        }
        int total = words.size();
        for(String word : tf.keySet())
        {
            tf.put(word,tf.get(word)/total);
        }
        return tf;
    }
    private Map<String , Double> computeIDF(List<List<String>> document)
    {
        Map<String,Double>idf = new HashMap<>();
        int totalDocs = document.size();
        List<Set<String>> docSet = new ArrayList<>();
        for(List<String>doc: document)
        {
            docSet.add(new HashSet<>(doc));
        }

        Set<String>vocabulary = new HashSet<>();
        for(Set<String>doc: docSet)
        {
            vocabulary.addAll(doc);
        }
        for(String word : vocabulary)
        {
            int count = 0;
            for(Set<String> doc : docSet)
            {
                if(doc.contains(word))
                {
                    count++;
                }
            }
            idf.put(word,Math.log((double)totalDocs/(1+count)));
        }
        return idf;
    }
    private Map<String,Double> computeTFIDF(List<String>words, Map<String, Double>idf)
    {
        Map<String,Double> tf = computeTF(words);
        Map<String,Double>tfidf= new HashMap<>();
        for(String word  : tf.keySet())
        {
            tfidf.put(word,tf.get(word) * idf.getOrDefault(word,0.0));
        }
        return tfidf;
    }
    public double cosineSimilarity(String text1, String text2,List<String>corpusRaw)
    {
        List<List<String>> corpus = new ArrayList<>();
        for(String doc : corpusRaw)
        {
            corpus.add(tokenize(doc));
        }
        List<String>words1 = tokenize(text1);
        List<String> words2 = tokenize(text2);
        Map<String,Double>idf= computeIDF(corpus);

        Map<String, Double>vec1 = computeTFIDF(words1,idf);
        Map<String,Double>vec2 = computeTFIDF(words2,idf);

        Set<String>allWords = new HashSet<>();
        allWords.addAll(vec1.keySet());
        allWords.addAll(vec2.keySet());
        double dot =0;
        double norm1=0,norm2=0;

        for(String words: allWords)
        {
            double v1= vec1.getOrDefault(words,0.0);
            double v2 = vec2.getOrDefault(words,0.0);
            dot+=v1*v2;
            norm1 += v1*v1;
            norm2 +=v2*v2;
        }
        if(norm2 == 0|| norm1 == 0)return 0.0;
        return  dot/(Math.sqrt(norm1)*Math.sqrt(norm2));
    }

}
