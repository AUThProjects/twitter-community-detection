USE DATABASE twitterdb;

CREATE TABLE cosine_similarity_hashtag (
  id serial primary key,
  uid_r varchar(100) not null,
  uid_c varchar(100) not null,
  similarity double precision not null
);

CREATE TABLE cosine_similarity_url (
  id serial primary key,
  uid_r varchar(100) not null,
  uid_c varchar(100) not null,
  similarity double precision not null
);

CREATE TABLE cosine_similarity_mention (
  id serial primary key,
  uid_r varchar(100) not null,
  uid_c varchar(100) not null,
  similarity double precision not null
);

CREATE TABLE cosine_similarity_retweet (
  id serial primary key,
  uid_r varchar(100) not null,
  uid_c varchar(100) not null,
  similarity double precision not null
);

CREATE TABLE jaccard_similarity (
  id serial primary key,
  uid_r varchar(100) not null,
  uid_c varchar(100) not null,
  similarity double precision not null
);

CREATE TABLE jaccard_similarity_hashtag (
  id serial primary key,
  uid_r varchar(100) not null,
  uid_c varchar(100) not null,
  similarity double precision not null
);
CREATE TABLE jaccard_similarity_url (
  id serial primary key,
  uid_r varchar(100) not null,
  uid_c varchar(100) not null,
  similarity double precision not null
);
CREATE TABLE jaccard_similarity_mention (
  id serial primary key,
  uid_r varchar(100) not null,
  uid_c varchar(100) not null,
  similarity double precision not null
);
CREATE TABLE jaccard_similarity_retweet (
  id serial primary key,
  uid_r varchar(100) not null,
  uid_c varchar(100) not null,
  similarity double precision not null
);

CREATE TABLE frequency_similarity (
  id serial primary key,
  uid_r varchar(100) not null,
  uid_c varchar(100) not null,
  similarity double precision not null
);
