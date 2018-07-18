
import hooks.GlobalHooks;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import static io.restassured.module.jsv.JsonSchemaValidator.*;


public class RestTest {

    private static String HOST = "https://jsonplaceholder.typicode.com";

    @Before
    public void disableWarnings() {
        GlobalHooks.disableWarning();
    }


    @Test
    public void getAllBooks() throws Throwable {
        get("/book").then().statusCode(200).and().body("", hasSize(4));

    }

    @Test
    public void getBook() throws Throwable {
        get("/book/4").then().statusCode(200)
                .and().body("id", is(4))
                .and().body("title", equalToIgnoringCase("tim"))
                .and().body("author", equalToIgnoringCase("God"));
    }


    @Test
    public void testSome() throws Throwable {
        get(HOST + "/posts").then().statusCode(200)
        .and().body("", hasSize(100));
    }

}
