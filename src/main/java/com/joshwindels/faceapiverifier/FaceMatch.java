package com.joshwindels.faceapiverifier;

public class FaceMatch {
    private boolean match;
    private Float confidence;

    public void setMatch(boolean match) {
        this.match = match;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }
}
