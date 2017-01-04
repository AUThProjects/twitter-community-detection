USE DATABASE twitterdb;

CREATE TABLE user_hashtag(
  id serial primary key,
  uid varchar(100) not null,
  hashtag varchar(50) not null
);

CREATE TABLE user_url(
  id serial primary key,
  uid varchar(100) not null,
  url varchar(255)
);

CREATE TABLE user_mention(
  id serial primary key,
  uid varchar(100) not null,
  mention_id varchar(100) not null
);

CREATE TABLE user_retweet(
  id serial primary key,
  uid varchar(100) not null,
  retweet_id varchar(100) not null
);
