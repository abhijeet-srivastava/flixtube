package com.twisha.flixtube.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class MediaStreamLoaderImpl implements  MediaStreamLoader{

    private static final String VIDEO_FILE_PATH = ClassLoader.getSystemClassLoader().getResource("videos").getFile();
    @Override
    public ResponseEntity<StreamingResponseBody> loadEntireMediaFile(String fileName) throws IOException {
        Path filePath = Paths.get(VIDEO_FILE_PATH, fileName);
        if (!filePath.toFile().exists()) {
            throw new FileNotFoundException("The media file does not exist.");
        }

        long fileSize = Files.size(filePath);
        long endPos = fileSize;
        if (fileSize > 0L) {
            endPos = fileSize - 1;
        } else  {
            endPos = 0L;
        }
        return loadPartialMediaFile(fileName, 0l, endPos);
    }

    @Override
    public ResponseEntity<StreamingResponseBody> loadPartialMediaFile(String fileName, String rangeValues) throws IOException {
        if(!StringUtils.hasText(rangeValues)) {
            return loadEntireMediaFile(fileName);
        }
        Path filePath = Paths.get(VIDEO_FILE_PATH, fileName);
        long fileSize = Files.size(filePath);
        log.info("Play file : {} for Range: {}", fileName, rangeValues);
        long[] partialRange = findRange(rangeValues, fileSize);
        log.info("Playing range : start {} End : {}", partialRange[0], partialRange[1]);
        return loadPartialMediaFile(fileName, partialRange[0], partialRange[1]);
    }

    private long[] findRange(String rangeValues, long fileSize) {
        long[] range = {0l, fileSize-1};
        String[] array = rangeValues.split("-");
        if(array.length == 1) {
            return range;
        }
        range[0] = safeParseStringValuetoLong(numericStringValue(array[0]), 0l);
        range[1] = safeParseStringValuetoLong(numericStringValue(array[1]), 0l);
        if(range[1] > fileSize) {
            range[1] = fileSize > 0 ?  fileSize-1l : 0l;
        }
        return range;
    }

    private String numericStringValue(String origVal) {
        String retVal = "";
        if (StringUtils.hasText(origVal)) {
            retVal = origVal.replaceAll("[^0-9]", "");
            System.out.println("Parsed Long Int Value: [" + retVal + "]");
        }
        return retVal;
    }

    private long safeParseStringValuetoLong(String valToParse, long defaultVal) {
        long retVal = defaultVal;
        if (StringUtils.hasText(valToParse)) {
            try  {
                retVal = Long.parseLong(valToParse);
            } catch (NumberFormatException ex) {
                // TODO: log the invalid long int val in text format.
                retVal = defaultVal;
            }
        }
        return retVal;
    }

    @Override
    public ResponseEntity<StreamingResponseBody> loadPartialMediaFile(String fileName, long fileStartPos, long fileEndPos) throws IOException {
        Path filePath = Paths.get(VIDEO_FILE_PATH, fileName);
        if(!filePath.toFile().exists()) {
            throw new FileNotFoundException("There is no media file by name :" +  fileName);
        }
        long fileSize = Files.size(filePath);
        if(fileStartPos < 0l) {
            fileStartPos = 0l;
        }
        if(fileSize > 0l) {
            if(fileStartPos >= fileSize) {
                fileStartPos = fileSize -1l;
            }
            if(fileEndPos >= fileSize) {
                fileEndPos = fileSize - 1l;
            }
        } else {
            fileStartPos = 0l;
            fileEndPos = 0l;
        }
        byte[] buffer = new byte[1024];
        String mimeType = Files.probeContentType(filePath);
        final HttpHeaders responseHeaders = new HttpHeaders();
        String contentLength = String.valueOf((fileEndPos - fileStartPos) + 1);
        responseHeaders.add("Content-Type", mimeType);
        responseHeaders.add("Content-Length", contentLength);
        responseHeaders.add("Accept-Ranges", "bytes");
        responseHeaders.add("Content-Range",
                String.format("bytes %d-%d/%d", fileStartPos, fileEndPos, fileSize));
        final long fileStartPos2 = fileStartPos;
        final long fileEndPos2 = fileEndPos;
        StreamingResponseBody responseStream = os -> {
            RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r");
            try (file) {
                long pos = fileStartPos2;
                file.seek(pos);
                while (pos < fileEndPos2) {
                    file.read(buffer);
                    os.write(buffer);
                    pos += buffer.length;
                }
                os.flush();
            }
            catch (Exception e) {}
        };
        return new ResponseEntity<StreamingResponseBody>
                (responseStream, responseHeaders, HttpStatus.PARTIAL_CONTENT);

    }
}
