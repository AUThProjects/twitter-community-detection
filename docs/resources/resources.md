# Twitter Community Detection

## Links

### Database
* [Mongo Java Driver](https://docs.mongodb.com/ecosystem/drivers/java/)

### Spark Streaming Examples
* [Spark Twitter Streaming - Databricks](https://databricks.gitbooks.io/databricks-spark-reference-applications/content/twitter_classifier/collect.html)
* [Spark Twitter Streaming - Example](https://github.com/stefanobaghino/spark-twitter-stream-example)

### Twitter API Documentation
* [Twitter REST API](https://dev.twitter.com/rest/public)
  * [Trending topics](https://dev.twitter.com/rest/reference/get/trends/place)
* [Twitter Streaming API](https://dev.twitter.com/streaming/public)

## mongodb

Mongo db aggregate query:

```
aggregate { aggregate: "tweets", pipeline: [ { $match: { retweetedStatus: { $exists: true, $ne: null } } }, { $project: { tweetId: "$retweetedStatus.id", userId: "$user.id" } }, { $sort: { tweetId: -1.0 } } ], cursor: {} }
```
