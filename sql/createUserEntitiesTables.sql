CREATE DATABASE twitterDB;

USE DATABASE twitterDB;

CREATE TABLE user_hashtag(
  id serial primary key,
  uid int not null,
  hashtag varchar(50) not null
)

CREATE TABLE user_url(
  id serial primary key,
  uid int not null,
  url varchar(255)
)

CREATE TABLE user_mention(
  id serial primary key,
  uid int not null,
  mention_uid int not null
)

CREATE TABLE user_retweet(
  id serial primary key,
  uid int not null,
  retweet_uid int not null
)