use test;

db.createCollection("article");

oid = ObjectId();
db.article.insert({_id:oid, text:"Hello world!"});
