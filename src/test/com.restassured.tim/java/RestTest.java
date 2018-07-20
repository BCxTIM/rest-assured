import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.Iterables;
import hooks.GlobalHooks;

import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.JsonPath;

import model.Book;
import org.junit.Before;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.fge.jsonschema.SchemaVersion.DRAFTV3;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidatorSettings.settings;
import static org.hamcrest.Matchers.*;

import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.junit.Assert.assertThat;


public class RestTest {


    @BeforeClass
    public void beforeExecution() {
        GlobalHooks.truncateTable();
        GlobalHooks.disableWarning();
    }


    @Test(priority = 1)
    public void postBook() throws Throwable {

//        JSONObject jsonObject = new JSONObject()
//                .put("title", "tiM")
//                .put("author", "GoD");

        Map<String, Object> jsonMapObject = new HashMap<>();
        jsonMapObject.put("title", "tiM");
        jsonMapObject.put("author", "GoD");

        given()
                .contentType(ContentType.JSON)
                .body(jsonMapObject)
                .when()
                .post("/book")
                .then().statusCode(200)
                .and()
                .contentType(ContentType.JSON);

    }

    @Test(priority = 2)
    public void getAllBooks() throws Throwable {
        this.postBook();
        this.postBook();
        this.postBook();
        get("/book").then().statusCode(200).and().body("", hasSize(4));

    }

    @Test (priority = 3)
    public void getBook() throws Throwable {
        get("/book/4").then().statusCode(200)
                .and().body("id", is(4))
                .and().body("title", equalToIgnoringCase("tim"))
                .and().body("author", equalToIgnoringCase("God"));
    }

    @Test(priority = 4)
    public void updateLastInsertedBook() throws Throwable {
//        JSONObject jsonObject = new JSONObject()
//                .put("title", "kira")
//                .put("author", "Mura");

        Map<String, Object> jsonMapObject = new HashMap<>();
        jsonMapObject.put("title", "kira");
        jsonMapObject.put("author", "Mura");

        List<Book> books = get("/book").then().extract().jsonPath().getList("", Book.class);

        int lastId = Iterables.getLast(books).getId();


        given()
                .contentType(ContentType.JSON)
                .body(jsonMapObject)
                .when()
                .put("/book/" + lastId)
                .then()
                .assertThat().body(containsString("Book has been updated successfully."));

    }

    @Test(priority = 5)
    public void deserialazeBook() throws Throwable {

        Book book = get("/book/4").as(Book.class);
        assertThat(book.getId(), is(4));
        assertThat(book.getTitle(), equalToIgnoringCase("kira"));
        assertThat(book.getAuthor(), equalToIgnoringCase("muRa"));
    }


    @Test(priority = 6)
    public void assertSchemaValidation() throws Throwable {

        //verify all books schema validation
        get("/book").then().assertThat().body(matchesJsonSchemaInClasspath("books-schema.json"));


        //verify book schema validation
        get("/book/4").then().assertThat().body(matchesJsonSchemaInClasspath("book-schema.json"));
    }


    @Test(priority = 7)
    public void testUnchekdSchemaValidation() throws Throwable {


        JsonSchemaValidator.settings = settings().with().jsonSchemaFactory(
                JsonSchemaFactory.newBuilder().setValidationConfiguration(ValidationConfiguration.newBuilder().setDefaultVersion(DRAFTV3).freeze()).freeze()).
                and().with().checkedValidation(false);


        get("/book/4").then().assertThat().body(matchesJsonSchemaInClasspath("failed-schema.json"));

    }


    @Test(priority = 8)
    public void testAdvanced() throws Throwable {
        get("/book").then().assertThat().body("title.findAll()", hasItems("tiM"));
    }



    @Test(priority = 9)
    public void deleteLastBook() throws Throwable {
        List<Book> books = get("/book").then().extract().jsonPath().getList("", Book.class);

        int lastId = Iterables.getLast(books).getId();


        delete("/book/" + lastId)
                .then()
                .statusCode(200)
                .and()
                .body(containsString("Book has been deleted successfully."));
    }


    @Test(priority = 10)
    public void sendObjectAsJSON() throws Throwable {
        Book book = new Book();
        book.setTitle("json title");
        book.setAuthor("json author");

        given().
                contentType(ContentType.JSON).
                body(book).
                when().
                post("/book").then().statusCode(200);
    }


    @Test(priority = 11)
    public void sendHashMapBody() throws Throwable {

        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put("title", "map title");
        jsonAsMap.put("author", "map author");

        given().
                contentType(ContentType.JSON).
                body(jsonAsMap).
                //log request
                log().all().
                when().
                post("/book").then().statusCode(200)
                //log response
                .log().all();


    }

    @Test(priority = 12)
    public void testJsonPath() throws Throwable {

        //get JSON
        String json = get("/book").getBody().asString();



        //get by name (with param)
        JsonPath.with(json).param("name", "tim").get("title.findAll() {title -> title ==name }");

        //get expected ids
        JsonPath.with(json).get("id.findAll() {id -> id >=2 }");

    }

}
