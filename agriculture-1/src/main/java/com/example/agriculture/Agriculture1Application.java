package com.example.agriculture;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@SpringBootApplication
public class Agriculture1Application {

	public static void main(String[] args) {
		SpringApplication.run(Agriculture1Application.class, args);
	}

}

@Configuration
class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")  // Allow requests from your web application origin
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // Allow specified methods
                .allowedHeaders("*");  // Allow all headers
    }
}

@RestController
class QueryController {
	
	 @PostMapping("/apply-query")
	    public List<String> applyQuery(@RequestBody String sparqlQuery) throws JsonMappingException, JsonProcessingException {
		 	List<String> queryResults = new ArrayList<>();
		 	
	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode rootNode = mapper.readTree(sparqlQuery);

	        // Get the value inside the sparqlQuery key
	        sparqlQuery = rootNode.get("sparqlQuery").asText();
	        
	        sparqlQuery = sparqlQuery.replace("&amp;", "&");
	        sparqlQuery = sparqlQuery.replace("&gt;", ">");
	        sparqlQuery = sparqlQuery.replace("&lt;", "<");
	        
	        String pre = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
	        		+ "            PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
	        		+ "            PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
	        		+ "            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
	        		+ "            PREFIX soil:  <http://dtree.com/SoilOntology#>";
	        sparqlQuery = pre + sparqlQuery;
	        
		 	System.out.println(sparqlQuery);
		 	
		 	String varName = "";
		 	if(sparqlQuery.contains("OrganicManure")){
		 		varName = "OrganicManure";
		 	} else {
		 		varName = "crop";
		 	}
	        // Load ontology file using Apache Jena
	        String ontologyFile = "agricultureontology.owl";
//		 	String ontologyFile = "/home/teja/Documents/workspace-spring-tool-suite-4-4.21.0.RELEASE/agriculture-1/src/main/resources/agricultureontology.owl";
	        try {
		        InputStream inputStream = new ClassPathResource(ontologyFile).getInputStream();
		        Dataset dataset = DatasetFactory.create();
		        dataset.begin(ReadWrite.WRITE);
		        dataset.getDefaultModel().read(inputStream, null, "RDF/XML");
		        dataset.commit();
		        QueryExecution qexec = QueryExecutionFactory.create(sparqlQuery, dataset);
	            ResultSet results = qexec.execSelect();
	            while (results.hasNext()) {
	                  QuerySolution soln = results.nextSolution();
	                  System.out.println(soln);
	                  RDFNode resultNode = soln.get(varName);
	                  if (resultNode != null) {
	                	  String result = resultNode.toString();
	                	  String lastString = result.substring(result.lastIndexOf("#") + 1);
	                	  System.out.println(lastString);
	                	  if(!lastString.equals("NamedIndividual"))
	                		  queryResults.add(lastString);
	                  }
	            }
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	        System.out.println("Result - " + queryResults);
	        return queryResults;
	    }
	   
}