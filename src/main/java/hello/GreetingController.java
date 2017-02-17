package hello;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
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

    public GreetingController() {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken("MjgwMzk5NDk5NDAxMzYzNDU3.C4JfLQ.WOiQETJ4j87yJdGfVkzvXoTgcy4").buildBlocking();
            jda.addEventListener(new Test());
            jda.getTextChannels().forEach(t -> {
                t.sendMessage("Hearst is loading").complete();
                Logger.getLogger(GreetingController.class.getName()).log(Level.SEVERE, t.getName());
            });
        } catch (LoginException ex) {
            Logger.getLogger(GreetingController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(GreetingController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(GreetingController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RateLimitedException ex) {
            Logger.getLogger(GreetingController.class.getName()).log(Level.SEVERE, null, ex);
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
        return new ResponseEntity<String>(body, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/message")
    public HttpEntity<String> getMessage() {
        return new ResponseEntity<String>(String.join("/", logs), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/message")
    public HttpEntity<String> deleteMessage() {
        logs.clear();
        return new ResponseEntity<String>(String.join("/", logs), HttpStatus.OK);
    }
}

class Test implements EventListener {

    @Override
    public void onEvent(Event event) {
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent e = (MessageReceivedEvent) event;
            System.out.println(e.getMessage().getContent());
//            if (!e.getAuthor().isBot()) {
                e.getChannel().sendMessage("simon says " + e.getMessage().getContent());
//            }
        }
    }
}
