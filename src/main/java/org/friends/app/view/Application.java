package org.friends.app.view;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import com.heroku.sdk.jdbc.DatabaseUrl;

public class Application {
	
	public final static String PORT = "PORT";
	
	public void start(@SuppressWarnings("unused") String [] args) {
		port(getPort());
	    staticFileLocation("/public");

	    get("/", (request, response) -> {
	    	return new ModelAndView(null, "index.ftl");
	    }, new FreeMarkerEngine());

	    
	    /* User managment */
	    get("/user/login", (req, res) -> {
	    	return new ModelAndView(null, "login.ftl");
	    }, new FreeMarkerEngine());
	    post("/user/login", (req, res) -> "A user tried to login");
	    
	    get("/user/new",  (req, res) -> {
	    	return new ModelAndView(null, "createUser.ftl");
	    }, new FreeMarkerEngine());
	    post("/user/new", (req, res) -> "A user tried to create his account");
	    
	    get("/user/forget", (req, res) -> {
	    	return new ModelAndView(null, "lostPwd.ftl");
	    }, new FreeMarkerEngine());
	    post("/user/forget", (req, res) -> "A user lost his password");
	    
	    
	    /* places booking */
	    get("/book/:placeId", (req, res) -> {
	        return "Are you looking for " + req.params(":placeId");
	    });

	    get("/sharePlace", new SharePlace(), new FreeMarkerEngine());  
	    
	    get("/sharePlaceValidation", (req, res) -> "TODO");
	    
	    
	    
	    get("/search", new SearchRoute(), new FreeMarkerEngine());
	    
	    get("/help", (req, res) -> "Nothing yet at help");

	    get("/db", (req, res) -> {
	      Connection connection = null;
	      Map<String, Object> attributes = new HashMap<>();
	      try {
	        connection = getConnection();

	        Statement stmt = connection.createStatement();
	        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
	        stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
	        ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

	        ArrayList<String> output = new ArrayList<String>();
	        while (rs.next()) {
	          output.add( "Read from DB: " + rs.getTimestamp("tick"));
	        }

	        attributes.put("results", output);
	        return new ModelAndView(attributes, "db.ftl");
	      } catch (Exception e) {
	        attributes.put("message", "There was an error: " + e);
	        return new ModelAndView(attributes, "error.ftl");
	      } finally {
	        if (connection != null) try{connection.close();} catch(SQLException e){}
	      }
	    }, new FreeMarkerEngine());
	}

	protected Connection getConnection() throws SQLException, URISyntaxException {
		return DatabaseUrl.extract().getConnection();
	}

	private static Integer getPort() {
		String port = System.getenv(PORT);
		if (port == null)
			port = System.getProperty(PORT);
		if (port == null)
			throw new RuntimeException("Port not defined");
		return Integer.valueOf(port);
	}
}
