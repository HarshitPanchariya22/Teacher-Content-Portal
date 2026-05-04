package com.portal.teachercontentportal.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.stream.Collectors;

@Service
public class Extraction {
    public String extractFromPDF(MultipartFile file)
    {
        try(PDDocument document = PDDocument.load(file.getInputStream()))
        {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            throw  new RuntimeException("Failed to extract PDF text");
        }
    }
    public String extractFromDOCX(MultipartFile file)
    {
        try(XWPFDocument document = new XWPFDocument(file.getInputStream()))
        {
            return document.getParagraphs()
                    .stream()
                    .map(p -> p.getText())
                    .collect(Collectors.joining(" "));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to extract DOCX text");
        }
    }
    public String extractText(MultipartFile file)
    {
        String filename = file.getOriginalFilename();
        filename = filename.toLowerCase();
        if(filename == null)
        {
            throw  new RuntimeException("Invalid file");
        }
        if(filename.endsWith("pdf"))
        {
            return extractFromPDF(file);
        }
        else if(filename.endsWith("docx")){
            return extractFromDOCX(file);
        }
        else{
            throw new RuntimeException("File type not supported");
        }
    }
    public String normalize(String text)
    {
        return text.toLowerCase().replaceAll("[^a-z0-9\\s]"," ").replaceAll("\\s+"," ").trim();
    }
    public String extractAndNormlaize(MultipartFile file)
    {
        return normalize(extractText(file));
    }
}
