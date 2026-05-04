package com.portal.teachercontentportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CsvImportResult {
    private int successCount;
    private List<String> errors;
}
