package hello;

import java.util.ArrayList;
import java.util.List;
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
