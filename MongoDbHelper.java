public class MongoDbHelper {

    private static MongoDbHelper sInstance = null;

    public static void init() {
        sInstance = new MongoDbHelper();
    }

    public static MongoDbHelper getInstance() {
        return sInstance;
    }

    private MongoClientURI URI = new MongoClientURI("mongodb://localhost:27017/test");
    private MongoClient mClient;
    private MongoDatabase mDb;
    private MongoCollection<Document> mCollectionArticle;

    private MongoDbHelper() {
        mClient = new MongoClient(URI);
        mDb = mClient.getDatabase("test");
        mCollectionArticle = mDb.getCollection("article");
    }



    // === Article ===

    public List<Article> getHomeArticle() {
        List<Article> list = new ArrayList<>();
        FindIterable<Document> docs = null;
        docs = mCollectionArticle.find(Filters.and(Filters.gte(Article.F_PRIORITY, 100),
                Filters.eq(Article.F_STATUS, Article.V_STATUS_PUBLISHED)));

        MongoCursor<Document> cursor = docs.iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            list.add(new Gson().fromJson(doc.toJson(), Article.class));
        }
        cursor.close();

        return list;
    }

    public Article getAndIncreaseReadCount(String id) {
        Document doc = mCollectionArticle.findOneAndUpdate(Filters.eq(Article.F_ID, new ObjectId(id)),
                new Document("$inc", new Document(Article.F_READ_COUNT, 1)));

        return new Gson().fromJson(doc.toJson(), Article.class);
    }

    public List<Article> getArticle(String id, String folderId, String authorId, Integer status) {
        List<Article> list = new ArrayList<>();
        FindIterable<Document> docs = null;

        if (id != null) {
            if (status == null) {
                docs = mCollectionArticle.find(Filters.eq(Article.F_ID, new ObjectId(id)));
            } else {
                docs = mCollectionArticle.find(Filters.and(Filters.eq(Article.F_ID, new ObjectId(id)),
                        Filters.eq(Article.F_STATUS, status)));
            }
        } else if (folderId != null) {
            if (status == null) {
                docs = mCollectionArticle.find(Filters.eq(Article.F_FOLDER_ID, new ObjectId(folderId)));
            } else {
                docs = mCollectionArticle.find(Filters.and(Filters.eq(Article.F_FOLDER_ID, new ObjectId(folderId)),
                        Filters.eq(Article.F_STATUS, status)));
            }
        } else if (authorId != null) {
            if (status == null) {
                docs = mCollectionArticle.find(Filters.eq(Article.F_AUTHOR_ID, new ObjectId(authorId)));
            } else {
                docs = mCollectionArticle.find(Filters.and(Filters.eq(Article.F_AUTHOR_ID, new ObjectId(authorId)),
                        Filters.eq(Article.F_STATUS, status)));
            }
        } else {
            if (status == null) {
                docs = mCollectionArticle.find();
            } else {
                docs = mCollectionArticle.find(Filters.eq(Article.F_STATUS, status));
            }
        }

        MongoCursor<Document> cursor = docs.iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            list.add(new Gson().fromJson(doc.toJson(), Article.class));
        }
        cursor.close();

        return list;
    }

    public void insertArticle(Article article) {
        String json = new Gson().toJson(article);
        LogUtil.d(TAG, "insertArticle() json = "+ json);
        mCollectionArticle.insertOne(Document.parse(json));
    }

    public void updateArticle(Article where, Article article) {
        mCollectionArticle.updateOne(Filters.eq(Article.F_ID, where.getObjectId()),
                new Document("$set", Document.parse(new Gson().toJson(article))));
    }

    public void deleteArticle(String[] ids) {
        for (int i = 0; i < ids.length; i++) {
            ObjectId articleId = new ObjectId(ids[i]);
            mCollectionArticle.deleteOne(Filters.eq(Article.F_ID, articleId));
            mCollectionArticleHistory.deleteMany(Filters.eq(Article.F_BELONG_TO_ID, articleId));
        }
    }

}
