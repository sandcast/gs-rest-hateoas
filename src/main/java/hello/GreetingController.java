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
import net.dv8tion.jda.core.exceptions.RateLimitedException;
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

    public void x() {
        try {
            JDA jda = new JDABuilder(AccountType.BOT).setToken("MjgwMzk5NDk5NDAxMzYzNDU3.C4JfLQ.WOiQETJ4j87yJdGfVkzvXoTgcy4").buildBlocking();
            jda.getTextChannels().forEach(t->{t.sendMessage("hi");});
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
