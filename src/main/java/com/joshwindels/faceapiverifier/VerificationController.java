package com.joshwindels.faceapiverifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class VerificationController {

    @Autowired
    FaceIdService faceIdService;

    @RequestMapping(value = "/facialRecognition/match", method = RequestMethod.GET)
    @ResponseBody
    public FaceMatch faceMatchesUserIdStoredImage(String username, String imageUrl) {
        return faceIdService.isFacialMatch(username, imageUrl);
    }

    @RequestMapping(value = "/facialRecognition/data", method = RequestMethod.GET)
    @ResponseBody
    public List<Object> faceMatchesUserIdStoredImage(String imageUrl) {
        return faceIdService.getFaceDetectionJsonForUrl(imageUrl).toList();
    }

}
