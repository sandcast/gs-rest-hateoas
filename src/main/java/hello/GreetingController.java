package hello;

import com.patreon.API;
import com.patreon.OAuth;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class GreetingController {

    private static final String TEMPLATE = "Hello, %s!";
    private List<String> logs = new ArrayList<>();
    private JDA jda;
    String clientID = null;        // Replace with your data
    String clientSecret = null;    // Replace with your data
    String creatorID = null;       // Replace with your data
    String redirectURI = null;     // Replace with your data
    String code = null;            // get from inbound HTTP request

    public GreetingController() {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(System.getenv("DISCORD_TOKEN")).buildBlocking();
            jda.addEventListener(new Test());
            jda.getTextChannels().forEach(t -> {
                t.sendMessage("Hearst is loading").complete();
                Logger.getLogger(GreetingController.class.getName()).log(Level.SEVERE, t.getName());
            });
        } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException ex) {
            Logger.getLogger(GreetingController.class.getName()).log(Level.SEVERE, null, ex);
        }
        API apiClient = new API(System.getenv("PATREON_API_TOKEN"));
        JSONObject userResponse = apiClient.fetchCampaign();
        Logger.getLogger(GreetingController.class.getName()).log(Level.SEVERE, userResponse.toString(2));
        JSONArray included = userResponse.getJSONArray("included");
        String reward = null;
        if (included != null) {
            for (int i = 0; i < included.length(); i++) {
                JSONObject object = included.getJSONObject(i);
                try {
                    if (object.getString("type").equals("reward") && object.getJSONObject("attributes").getString("title").startsWith("Snitches")) {
                        reward = object.getJSONObject("attributes").getString("title");
                        break;
                    }
                } catch (Exception e) {
                    Logger.getLogger(GreetingController.class.getName()).log(Level.SEVERE, "parse nad no title");
                }
            }
            final String realReward = reward;
            jda.getTextChannels().forEach(t -> {
                t.sendMessage("Hearst found patreon campaign " + realReward).complete();
                Logger.getLogger(GreetingController.class.getName()).log(Level.SEVERE, t.getName());
            });
        }
    }

    @RequestMapping("/greeting")
    public HttpEntity<Greeting> greeting(
            @RequestParam(value = "name", required = false, defaultValue = "World") String name) {
        Greeting greeting = new Greeting(String.format(TEMPLATE, name));
        greeting.add(linkTo(methodOn(GreetingController.class).greeting(name)).withSelfRel());
        return new ResponseEntity<Greeting>(greeting, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/message")
    public HttpEntity<String> postMessage(@RequestBody String body) {
        logs.add(body);
        jda.getTextChannels().forEach(t -> {
            t.sendMessage(body).complete();
            Logger.getLogger(GreetingController.class.getName()).log(Level.SEVERE, body);
        });
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/message")
    public HttpEntity<String> getMessage() {
        return new ResponseEntity<>(String.join("/", logs), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/message")
    public HttpEntity<String> deleteMessage() {
        logs.clear();
        return new ResponseEntity<>(String.join("/", logs), HttpStatus.OK);
    }
}

class Test implements EventListener {

    @Override
    public void onEvent(Event event) {
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent e = (MessageReceivedEvent) event;
            System.out.println(e.getMessage().getContent());
            if (!e.getAuthor().isBot()) {
                e.getChannel().sendMessage("simon says " + e.getMessage().getContent()).complete();
            }
        }
    }
}
