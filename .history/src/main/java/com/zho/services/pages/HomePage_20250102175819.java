package com.zho.services.pages;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import com.zho.model.BlogRequest;
import com.zho.api.OpenAIClient;
import com.zho.api.wordpress.WordPressBlockClient;

public class HomePage implements StaticPage {
    private final WordPressBlockClient blockClient;
    private final OpenAIClient openAIClient;
    private final int pageId = 609;

    public HomePage(WordPressBlockClient blockClient, OpenAIClient openAIClient) {
        this.blockClient = blockClient;
        this.openAIClient = openAIClient;
    }

    @Override
    public void updateStaticContent(BlogRequest request) throws IOException, ParseException {
        updateHeadingAndSubheading(request);
        updateMissionParagraph(request);
    }

    @Override
    public String getPageName() {
        return "Home";
    }

    @Override
    public int getPageId() {
        return pageId;
    }

    private String capitalizeWords(String text) {
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    private void updateHeadingAndSubheading(BlogRequest request) throws IOException, ParseException {
        // Generate content
        System.out.println("\n=== Generating Heading ===");
        String heading = generateHeading(request);
        System.out.println("Generated heading: " + heading);

        System.out.println("\n=== Generating Subheading ===");
        String subheading = generateSubheading(request);
        System.out.println("Generated subheading: " + subheading);

        // Update heading
        JSONObject headingProps = new JSONObject()
            .put("level", 1)
            .put("uniqueID", "609_a8d80a-ca")
            .put("align", "center")
            .put("color", "#ffffff")
            .put("typography", "Jost")
            .put("googleFont", true)
            .put("fontSubset", "latin")
            .put("fontVariant", "700")
            .put("fontWeight", "700")
            .put("textTransform", "none")
            .put("fontSize", new int[]{60, 0, 40})
            .put("fontHeight", new int[]{68, 0, 48})
            .put("fontHeightType", "px");

        // Update subheading
        JSONObject subheadingProps = new JSONObject()
            .put("uniqueID", "609_e56131-4a")
            .put("htmlTag", "h2")
            .put("color", "#ffffff")
            .put("align", "center")
            .put("fontSize", new String[]{"xl", "", ""});

        blockClient.updateBlock(getPageId(), WordPressBlockClient.BlockType.KADENCE_HEADING, "609_a8d80a-ca", heading, headingProps);
        blockClient.updateBlock(getPageId(), WordPressBlockClient.BlockType.KADENCE_HEADING, "609_e56131-4a", subheading, subheadingProps);
    }

    private void updateMissionParagraph(BlogRequest request) throws IOException, ParseException {
        System.out.println("\n=== Generating Mission ===");
        String missionPrompt = String.format(
            "Write a clever, memorable 2-3 sentence mission statement for a blog about %s. " +
            "First sentence must start with 'Our mission is to' followed by a creative way to say we help people. " +
            "Second sentence should explain how we do it. Optional third sentence can add personality. " +
            "Examples: " +
            "'Our mission is to turn coffee-fueled developers into coding ninjas. We blend cutting-edge tutorials with " +
            "real-world wisdom, serving up bite-sized lessons that stick. Think of us as your digital dojo, where " +
            "every bug is just another chance to level up.' " +
            "Make it fun but professional, specific to this topic: '%s'.",
            request.getDescription(),
            request.getTopic()
        );
        String mission = openAIClient.callOpenAI(missionPrompt, 0.9);
        System.out.println("Generated mission: " + mission);
        System.out.println("WordPress update completed");

        // Update WordPress content
        JSONObject missionProps = new JSONObject()
            .put("uniqueID", "609_29304f-69")
            .put("markBorder", "")
            .put("htmlTag", "p");

        blockClient.updateBlock(
            getPageId(),
            WordPressBlockClient.BlockType.KADENCE_HEADING,
            "609_29304f-69",
            mission,
            missionProps
        );
    }

    public static void main(String[] args) {
        try {
            // Initialize dependencies
            WordPressBlockClient blockClient = new WordPressBlockClient();
            OpenAIClient openAIClient = new OpenAIClient();
            
            // Create HomePage instance
            HomePage homePage = new HomePage(blockClient, openAIClient);
            
            // Create test BlogRequest
            BlogRequest testRequest = new BlogRequest(
                "tennis blog",
                "tennis for beginners"
            );
            
            // Test updateHeadingAndSubheading
            System.out.println("Testing heading and subheading update...");
            homePage.updateStaticContent(testRequest);
            System.out.println("Heading and subheading updated successfully!");
            
        } catch (Exception e) {
            System.err.println("Error during testing: " + e.getMessage());
            e.printStackTrace();
        }
    }

} 