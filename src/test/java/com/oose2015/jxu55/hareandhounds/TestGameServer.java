package com.oose2015.jxu55.hareandhounds;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sqlite.SQLiteDataSource;
import spark.Spark;
import spark.utils.IOUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.*;

import org.junit.*;
import static org.junit.Assert.*;

public class TestGameServer {

    //------------------------------------------------------------------------//
    // Setup
    //------------------------------------------------------------------------//

    @Before
    public void setup() throws Exception {
        //Clear the database and then start the server
        //clearDB();

        //Start the main server
        Bootstrap.main(null);
        Spark.awaitInitialization();
    }

    @After
    public void tearDown() {
        //Stop the server
        //clearDB();
        Spark.stop();
    }

    @Test
    public void testCreateGame() throws Exception {

    }

    @Test
    public void testJoinGame() throws Exception {

    }

    @Test
    public void testWinningConditions(){

    }


    //------------------------------------------------------------------------//
    // Generic Helper Methods and classes
    //------------------------------------------------------------------------//

    private Response request(String method, String path, Object content) {
        try {
            URL url = new URL("http", Bootstrap.IP_ADDRESS, Bootstrap.PORT, path);
            System.out.println(url);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoInput(true);
            if (content != null) {
                String contentAsJson = new Gson().toJson(content);
                http.setDoOutput(true);
                http.setRequestProperty("Content-Type", "application/json");
                OutputStreamWriter output = new OutputStreamWriter(http.getOutputStream());
                output.write(contentAsJson);
                output.flush();
                output.close();
            }

            String responseBody = IOUtils.toString(http.getInputStream());
            return new Response(http.getResponseCode(), responseBody);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Sending request failed: " + e.getMessage());
            return null;
        }
    }


    private static class Response {

        public String content;

        public int httpStatus;

        public Response(int httpStatus, String content) {
            this.content = content;
            this.httpStatus = httpStatus;
        }

        public <T> T getContentAsObject(Type type) {
            return new Gson().fromJson(content, type);
        }
    }

    //------------------------------------------------------------------------//
    // GameApp Specific Helper Methods and classes
    //------------------------------------------------------------------------//

    /*private void clearDB() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:game.db");

        Sql2o db = new Sql2o(dataSource);

        try (Connection conn = db.open()) {
            String sql = "DROP TABLE IF EXISTS item" ;
            conn.createQuery(sql).executeUpdate();
        }
    }*/

}