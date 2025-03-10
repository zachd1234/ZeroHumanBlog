import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class ContentGenerationHandler implements RequestHandler<Object, String> {
    @Override
    public String handleRequest(Object input, Context context) {
        try {
            DatabaseService db = new DatabaseService();
            
            if (!db.isBlogActive()) {
                System.out.println("Blog is inactive. Skipping content generation.");
                return "Blog inactive - no action taken";
            }
            
            System.out.println("Blog is active. Generating content...");
            AutoContentWorkflowService service = new AutoContentWorkflowService();
            service.processNextKeyword();
            
            return "Content generation successful";
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
} 