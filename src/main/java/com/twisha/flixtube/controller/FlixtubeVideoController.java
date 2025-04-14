package com.twisha.flixtube.controller;

import com.twisha.flixtube.service.MediaStreamLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/videos")
@Slf4j
public class FlixtubeVideoController {

    private MediaStreamLoader streamLoaderService;

    @Autowired
    public FlixtubeVideoController(MediaStreamLoader streamLoaderService) {
        this.streamLoaderService = streamLoaderService;
    }

    @GetMapping(value = "/play/{vid_id}")
    public ResponseEntity<StreamingResponseBody> streamVideoByPath(
            @RequestHeader(value="Range", required = false) String rangeHeader,
            @PathVariable("vid_id") String videoId
    ) {
        try {
            ResponseEntity<StreamingResponseBody> retVal =
                    streamLoaderService.loadPartialMediaFile(videoId, rangeHeader);
            return retVal;
        } catch (FileNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
