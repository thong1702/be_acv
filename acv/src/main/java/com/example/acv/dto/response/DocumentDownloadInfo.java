package com.example.acv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
public class DocumentDownloadInfo {
    private String filename;
    private String mimeType;
    private long contentLength;
    private Resource resource;
    private String redirectUrl;
}
