package com.joshwindels.faceapiverifier;

import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Service
public class FaceIdService {

    private static String BASE_URL = "https://[location].api.cognitive.microsoft.com/face/v1.0/";

    private static String DETECT_ENDPOINT = "detect";
    private static String VERIFY_ENDPOINT = "verify";

    private static String FACE_ID_KEY = "faceId";
    private static String FACE_MATCH_KEY = "isIdentical";
    private static String CONFIDENCE_KEY = "confidence";

    private static String ATTRIBUTES_TO_RETURN = "age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion,accessories,blur,exposure,noise";

    private static final String subscriptionKey = "[subscription key]";

    private static final Map<String, String> storedUsernameToImageUrlMap = ImmutableMap.of(
            "username1", "https://ewedit.files.wordpress.com/2017/05/gettyimages-624931876.jpg?w=2000",
            "username2", "https://www.etonline.com/sites/default/files/styles/max_970x546/public/images/2018-04/angievidpic.jpg?itok=Mj66dH79&h=c673cd1c"
            );

    public FaceMatch isFacialMatch(String username, String imageUrl) {
        return getFaceMatchFromResponse(getFaceIdForImageUrl(storedUsernameToImageUrlMap.get(username)), getFaceIdForImageUrl(imageUrl));

    }

    private String getFaceIdForImageUrl(String imageUrl) {
        return getFaceDetectionJsonForUrl(imageUrl).getJSONObject(0).getString(FACE_ID_KEY);
    }

    public JSONArray getFaceDetectionJsonForUrl(String imageUrl) {
        try {
            HttpEntity entity = getHttpEntityForDetection(imageUrl);
            if (entity != null) {
                String jsonString = EntityUtils.toString(entity).trim();
                if (jsonString.charAt(0) == '[') {
                    return new JSONArray(jsonString);
                } else {
                    throw new RuntimeException("too many faces recognised in picture");
                }
            } else {
                throw new RuntimeException("error processing image: " + imageUrl);
            }
        } catch (Exception e) {
            throw new RuntimeException("error processing uploaded image");
        }
    }

    private HttpEntity getHttpEntityForDetection(String imageUrl) throws URISyntaxException, IOException {
        HttpClient httpclient = new DefaultHttpClient();
        URIBuilder builder = new URIBuilder(BASE_URL + DETECT_ENDPOINT);

        builder.setParameter("returnFaceId", "true");
        builder.setParameter("returnFaceLandmarks", "false");
        builder.setParameter("returnFaceAttributes", ATTRIBUTES_TO_RETURN);

        URI uri = builder.build();
        HttpPost request = new HttpPost(uri);

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

        StringEntity reqEntity = new StringEntity("{\"url\":\"" + imageUrl + "\"}");
        request.setEntity(reqEntity);

        HttpResponse response = httpclient.execute(request);
        return response.getEntity();
    }

    private FaceMatch getFaceMatchFromResponse(String storedFaceId, String newFaceId) {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            URIBuilder builder = new URIBuilder(BASE_URL + VERIFY_ENDPOINT);
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);

            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

            StringEntity reqEntity = new StringEntity("{\"faceId1\":\"" + storedFaceId + "\", \"faceId2\":\"" + newFaceId + "\"}");
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            FaceMatch faceMatch = new FaceMatch();
            if (entity != null) {
                String jsonString = EntityUtils.toString(entity).trim();
                if (jsonString.charAt(0) == '{') {
                    JSONObject jsonResponse = new JSONObject(jsonString);
                    faceMatch.setMatch(jsonResponse.getBoolean(FACE_MATCH_KEY));
                    faceMatch.setConfidence(jsonResponse.getFloat(CONFIDENCE_KEY));
                    return faceMatch;
                } else {
                    throw new RuntimeException("too many faces recognised in picture");
                }
            }
            return faceMatch;
        } catch (Exception e) {
            System.out.print(e);
            throw new RuntimeException("error processing uploaded image");
        }
    }

}
