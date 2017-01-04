select uid1, uid2, hashtag, user1_hashtag_count, user2_hashtag_count
from (
  select uh1.uid as uid1, uh2.uid as uid2, uh1.hashtag as hashtag, uh1.count as user1_hashtag_count, uh2.count as user2_hashtag_count
  from (
    select uh1.uid, uh1.hashtag, count(uh1.hashtag)
    from user_hashtag as uh1
    group by uh1.uid, uh1.hashtag  
  ) as uh1
  inner join (
  select uh2.uid, uh2.hashtag, count(uh2.hashtag)
  from user_hashtag as uh2 
  group by uh2.uid, uh2.hashtag
  ) as uh2
  on uh1.hashtag=uh2.hashtag 
  where uh1.uid < uh2.uid
  order by uh1.uid asc, uh2.uid asc
) as q

