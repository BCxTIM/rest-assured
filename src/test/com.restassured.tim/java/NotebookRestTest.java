import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.Iterables;
import hooks.GlobalHooks;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.JsonPath;
import model.Book;
import model.Notebook;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.fge.jsonschema.SchemaVersion.DRAFTV3;
import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.module.jsv.JsonSchemaValidatorSettings.settings;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


public class NotebookRestTest {


    @BeforeClass
    public void beforeExecution() throws InterruptedException {
        GlobalHooks.truncateTable();
        GlobalHooks.disableWarning();
    }


    @Test(priority = 1)
    public void postNotebook() throws Throwable {

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
                .post("/notebook")
                .then().statusCode(200)
                .and()
                .contentType(ContentType.JSON);

    }

    @Test(priority = 2)
    public void getAllnotebooks() throws Throwable {
        this.postNotebook();
        this.postNotebook();
        this.postNotebook();
        get("/notebook").then().statusCode(200).and().body("", hasSize(4));

    }

    @Test (priority = 3)
    public void getNotebook() throws Throwable {
        get("/notebook/4").then().statusCode(200)
                .and().body("id", is(4))
                .and().body("title", equalToIgnoringCase("tim"))
                .and().body("author", equalToIgnoringCase("God"));
    }

    @Test(priority = 4)
    public void updateLastInsertedNotebook() throws Throwable {
//        JSONObject jsonObject = new JSONObject()
//                .put("title", "kira")
//                .put("author", "Mura");

        Map<String, Object> jsonMapObject = new HashMap<>();
        jsonMapObject.put("title", "kira");
        jsonMapObject.put("author", "Mura");

        List<Notebook> notebooks = get("/notebook").then().extract().jsonPath().getList("", Notebook.class);

        int lastId = Iterables.getLast(notebooks).getId();


        given()
                .contentType(ContentType.JSON)
                .body(jsonMapObject)
                .when()
                .put("/notebook/" + lastId)
                .then()
                .assertThat().body(containsString("Notebook has been updated successfully."));

    }

    @Test(priority = 5)
    public void deserialazeNotebook() throws Throwable {

        Notebook notebook = get("/notebook/4").as(Notebook.class);
        assertThat(notebook.getId(), is(4));
        assertThat(notebook.getTitle(), equalToIgnoringCase("kira"));
        assertThat(notebook.getAuthor(), equalToIgnoringCase("muRa"));
    }


    @Test(priority = 6)
    public void assertSchemaValidation() throws Throwable {

        //verify all notebooks schema validation
        get("/notebook").then().assertThat().body(matchesJsonSchemaInClasspath("notebooks-schema.json"));


        //verify notebook schema validation
        get("/notebook/4").then().assertThat().body(matchesJsonSchemaInClasspath("notebook-schema.json"));
    }


    @Test(priority = 7)
    public void testUnchekdSchemaValidation() throws Throwable {


        JsonSchemaValidator.settings = settings().with().jsonSchemaFactory(
                JsonSchemaFactory.newBuilder().setValidationConfiguration(ValidationConfiguration.newBuilder().setDefaultVersion(DRAFTV3).freeze()).freeze()).
                and().with().checkedValidation(false);


        get("/notebook/4").then().assertThat().body(matchesJsonSchemaInClasspath("failed-schema.json"));

    }


    @Test(priority = 8)
    public void testAdvanced() throws Throwable {
        get("/notebook").then().assertThat().body("title.findAll()", hasItems("tiM"));
    }



    @Test(priority = 9)
    public void deleteLastNotebook() throws Throwable {
        List<Notebook> notebooks = get("/notebook").then().extract().jsonPath().getList("", Notebook.class);

        int lastId = Iterables.getLast(notebooks).getId();


        delete("/notebook/" + lastId)
                .then()
                .statusCode(200)
                .and()
                .body(containsString("Notebook has been deleted successfully."));
    }


    @Test(priority = 10)
    public void sendObjectAsJSON() throws Throwable {
        Notebook notebook = new Notebook();
        notebook.setTitle("json title");
        notebook.setAuthor("json author");

        given().
                contentType(ContentType.JSON).
                body(notebook).
                when().
                post("/notebook").then().statusCode(200);
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
                post("/notebook").then().statusCode(200)
                //log response
                .log().all();


    }

    @Test(priority = 12)
    public void testJsonPath() throws Throwable {

        //get JSON
        String json = get("/notebook").getBody().asString();



        //get by name (with param)
        JsonPath.with(json).param("name", "tim").get("title.findAll() {title -> title ==name }");

        //get expected ids
        JsonPath.with(json).get("id.findAll() {id -> id >=2 }");

    }

}
