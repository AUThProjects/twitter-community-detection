# Twitter Community Detection

## Description

The goal of this project is to find communities of users with common traits and interests in Twitter, by taking into account the indirect relations between them.

## Steps

* Gather streaming data from [Twitter](https://twitter.com)
* Put data into [MongoDB](https://www.mongodb.com/)
* Data Analysis and Modelling with Java
* Save new data in DB backend
* Calculate user similarity
* Create user graph and calculate graph stats
* Find user communities in graph with Louvain algorithm

```bash
########################################################################################################
# > How to Run
########################################################################################################


# Before running, make sure you have MongoDB, PostgreSQL and Spark installed and configured properly.
# The tables to be created in PostgreSQL are included in the sql/*.sql files.

# Clone repository from Github
git clone https://github.com/authprojects/twitter-community-detection.git
cd twitter-community-detection/

# Build solution jar
sbt assembly

# Enter twitter data in src/main/resources/twitter4j.properties

# Gather tweets from Streaming API
spark-submit target/scala-2.11/twitter-community-detection-assembly-1.0.jar --class TwitterDataCollection

# Gather valuable data for processing from Mongo to Postgres
java -cp target/scala-2.11/twitter-community-detection-assembly-1.0.jar MongoToPostgres

# Compute and enter similarities in Postgres
java -cp target/scala-2.11/twitter-community-detection-assembly-1.0.jar SimilarityComputation

# Compute and print NMI
java -cp target/scala-2.11/twitter-community-detection-assembly-1.0.jar NMIComputation
```

## Team

| Name | Username |
| --- | --- |
| Stefanos Laskaridis | [@stevelaskaridis](https://github.com/stevelaskaridis) |
| Antonios Anagnostou | [@anagnoad](https://github.com/anagnoad) |
