package com.twisha.flixtube.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;

/**
 * MediaStreamLoader: Stream Media from Object storage
 */
public interface MediaStreamLoader {
    ResponseEntity<StreamingResponseBody> loadEntireMediaFile(String fileName) throws IOException;
    ResponseEntity<StreamingResponseBody> loadPartialMediaFile(String fileName, String rangeValues) throws IOException;
    ResponseEntity<StreamingResponseBody> loadPartialMediaFile(String fileName, long fileStartPos, long fileEndPos) throws IOException;
}
