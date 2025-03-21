package com.zho.services;

import com.zho.model.BlogRequest;
import com.zho.api.wordpress.WordPressMediaClient;
import com.zho.api.NounProjectClient;
import com.zho.api.OpenAIClient;
import org.json.JSONObject;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.hc.core5.http.ParseException;
import java.io.File;

public class LogoAndFaviconService {
    private final WordPressMediaClient mediaClient;
    private final NounProjectClient nounClient;
    private final OpenAIClient openAIClient;

    public LogoAndFaviconService(WordPressMediaClient mediaClient) {
        this.mediaClient = mediaClient;
        this.nounClient = new NounProjectClient();
        this.openAIClient = new OpenAIClient();
    }

    private String generateIconSearchTerm(BlogRequest request) throws IOException {
        String prompt = String.format(
            "Given a blog topic about '%s', suggest ONE simple, iconic symbol that would work well as a logo. " +
            "Requirements:\n" +
            "1. Must be a common, widely-recognized symbol that definitely exists in icon libraries\n" +
            "2. Should be simple enough to work as a small favicon\n" +
            "3. If the topic is very specific, suggest a more general but related symbol\n" +
            "4. Avoid complex or compound concepts\n\n" +
            "Examples:\n" +
            "- 'Advanced Quantum Computing' → 'atom'\n" +
            "- 'Mediterranean Cooking Tips' → 'chef-hat'\n" +
            "- 'Professional Dog Training' → 'paw'\n" +
            "- 'Beginner JavaScript Tutorials' → 'code'\n\n" +
            "Respond with ONLY the search term, nothing else.",
            request.getTopic()
        );

        return openAIClient.callOpenAI(prompt, 0.7).toLowerCase().trim();
    }

    public void generateAndUploadBranding(BlogRequest request) throws IOException, ParseException {
        // Generate smart search term
        String searchTerm = generateIconSearchTerm(request);
        System.out.println("Generated icon search term: " + searchTerm);

        try {
            BufferedImage iconImage = fetchIconFromNounProject(searchTerm);
            mediaClient.updateFavicon(iconImage);
            System.out.println("Successfully updated favicon with icon for: " + searchTerm);
        } catch (IOException e) {
            // If first attempt fails, try a fallback term
            String fallbackTerm = generateIconSearchTerm(new BlogRequest("general " + request.getTopic(), "general " + request.getDescription()));
            System.out.println("Trying fallback search term: " + fallbackTerm);
            
            BufferedImage iconImage = fetchIconFromNounProject(fallbackTerm);
            mediaClient.updateFavicon(iconImage);
            System.out.println("Successfully updated favicon with fallback icon for: " + fallbackTerm);
        }
    }

    private BufferedImage generateLogo(BufferedImage icon, BlogRequest request) throws IOException {
        // Generate blog name
        String blogName = openAIClient.callOpenAI(
            "Generate a short, catchy blog name for a blog about " + request.getTopic() + 
            ". Response should be ONLY the blog name, nothing else.", 0.7);
        System.out.println("Generated blog name: " + blogName);
        
        // Create combined logo
        int width = 1000;
        int height = 300;
        BufferedImage logo = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = logo.createGraphics();
        
        // Set up rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw white background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        // Draw icon in teal color (#049F82)
        g2d.setColor(new Color(0x04, 0x9F, 0x82));  // Teal color
        g2d.drawImage(icon, 20, 20, 240, 240, null);
        
        // Draw text in dark gray (#222222)
        g2d.setColor(new Color(0x22, 0x22, 0x22));  // Dark gray
        g2d.setFont(new Font("Arial", Font.BOLD, 72));
        FontMetrics fm = g2d.getFontMetrics();
        int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(blogName, 220, textY);
        
        g2d.dispose();
        return logo;
    }

    private BufferedImage fetchIconFromNounProject(String searchTerm) throws IOException {
        JSONObject searchResult = nounClient.searchIcons(searchTerm);
        if (searchResult.has("icons") && searchResult.getJSONArray("icons").length() > 0) {
            String iconUrl = searchResult.getJSONArray("icons")
                .getJSONObject(0)
                .getString("thumbnail_url");
            byte[] iconData = nounClient.downloadIcon(iconUrl);
            return ImageIO.read(new ByteArrayInputStream(iconData));
        }
        throw new IOException("No icons found for search term: " + searchTerm);
    }

    public static void main(String[] args) {
        try {
            // Create test blog requests
            BlogRequest[] testRequests = {
                new BlogRequest("Mediterranean Cooking Tips", "Authentic Mediterranean recipes and techniques"),
            };

            // Initialize service
            WordPressMediaClient mediaClient = new WordPressMediaClient();
            LogoAndFaviconService service = new LogoAndFaviconService(mediaClient);

            // Test each request
            for (BlogRequest request : testRequests) {
                System.out.println("\n=== Testing with topic: " + request.getTopic() + " ===");
                try {
                    // Get icon first
                    String searchTerm = service.generateIconSearchTerm(request);
                    System.out.println("Generated search term: " + searchTerm);
                    BufferedImage icon = service.fetchIconFromNounProject(searchTerm);
                    
                    // Generate logo
                    System.out.println("Generating logo...");
                    BufferedImage logo = service.generateLogo(icon, request);
                    
                    // Save logo to desktop
                    String fileName = request.getTopic().replaceAll("\\s+", "-").toLowerCase() + "-logo.png";
                    String desktopPath = System.getProperty("user.home") + "/Desktop/" + fileName;
                    ImageIO.write(logo, "PNG", new File(desktopPath));
                    System.out.println("✓ Success: Logo saved to " + desktopPath);
                    
                    // Optional: Still do the branding upload if needed
                    // service.generateAndUploadBranding(request);
                    
                } catch (Exception e) {
                    System.err.println("✗ Failed: " + e.getMessage());
                    e.printStackTrace();
                }
                Thread.sleep(2000);
            }

        } catch (Exception e) {
            System.err.println("Error during testing: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 