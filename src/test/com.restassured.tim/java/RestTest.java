
import hooks.GlobalHooks;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import static io.restassured.module.jsv.JsonSchemaValidator.*;


public class RestTest {


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
    public void assertSchemaValidation() throws Throwable {

        //verify all books schema validation
        get("/book").then().assertThat().body(matchesJsonSchemaInClasspath("books-schema.json"));


        //verify book schema validation
        get("/book/4").then().assertThat().body(matchesJsonSchemaInClasspath("book-schema.json"));
    }





}
