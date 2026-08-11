package gr.aueb.cf.restbankapp.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards client-side routes of the React SPA to index.html so that a direct
 * hit or refresh on a route such as /login is served by the SPA instead of
 * returning 404. API calls (/api/**), static assets and /legacy are unaffected.
 */
@Controller
public class SpaForwardingController {

    @GetMapping({"/login"})
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
