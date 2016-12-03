name := "twitter-community-detection"

version := "1.0"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.2",
  "org.apache.spark" %% "spark-sql" % "1.6.2",
  "org.apache.spark" %% "spark-streaming" % "1.6.2",
  "org.apache.spark" %% "spark-streaming-twitter",
  "org.twitter4j" % "twitter4j-stream" % "4.0.5",
  "org.mongodb" % "mongodb-driver" % "3.4.0",
  "net.liftweb" %% "lift-json" % "2.6.3",
  "com.github.nscala-time" %% "nscala-time" % "2.12.0"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.last
}
