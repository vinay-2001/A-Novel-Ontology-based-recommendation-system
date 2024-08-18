const express = require('express');
const bodyParser = require('body-parser');
const rdfParser = require('rdf-parse'); // Example RDF parser library, replace with actual library
const protegeApi = require('protege-api'); // Example Protege API library, replace with actual library

const app = express();
const port = 3000;

app.use(bodyParser.json());

// Read and parse the ontology file
const ontology = rdfParser.parse('C:\Users\aulug\OneDrive\Documents\agricultureontology.owl'); // Adjust the path to your ontology file

app.post('/generate-query', async (req, res) => {
    try {
        const { pH, salinity, EC } = req.body;

        // Generate SPARQL query based on user inputs and ontology information
        const sparqlQuery = `
            SELECT DISTINCT ?Crop
                WHERE { 
                BIND(${pH} AS ?pH1)
                BIND(${salinity} AS ?Salinity1)
                BIND(${EC} AS ?EC1)
                ?Crop rdfs:hasPh ?pH; rdfs:hasS ?Salinity; rdfs:hasEc ?EC.
                BIND(?pH - 0.902 AS ?minRange1)
                BIND(?pH + 0.902 AS ?maxRange1)
                BIND(?Salinity - 1.21 AS ?minRange2)
                BIND(?Salinity + 1.21 AS ?maxRange2)
                BIND(?EC - 0.304 AS ?minRange3)
                BIND(?EC + 0.304 AS ?maxRange3)
                FILTER (?pH >= ?minRange1 && ?pH <= ?maxRange1 && ?Salinity >= ?minRange2 && ?Salinity <= ?maxRange2 && ?EC >= ?minRange3 && ?EC <= ?maxRange3)
            }
            ORDER BY ABS(?pH - ?pH1) ABS(?Salinity - ?Salinity1) ABS(?EC - ?EC1);`

        res.json({ sparqlQuery });
    } catch (error) {
        res.status(500).json({ error: 'Internal server error' });
    }
});

app.post('/apply-query', async (req, res) => {
    try {
        const { sparqlQuery } = req.body;

        // Apply SPARQL query to ontology using Protege API
        const recommendations = await protegeApi.applySPARQLQuery(sparqlQuery, ontology);

        res.json({ recommendations });
    } catch (error) {
        res.status(500).json({ error: 'Internal server error' });
    }
});

app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});
