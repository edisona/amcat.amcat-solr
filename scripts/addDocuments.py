import solr
import datetime, time
from amcat.model import article
from django.db import connection

class GMT1(datetime.tzinfo):
    """very basic timezone object, needed for solrpy library.."""
    def utcoffset(self,dt):
        return datetime.timedelta(hours=1)
    def tzname(self,dt):
        return "GMT +1"
    def dst(self,dt):
        return datetime.timedelta(0) 

        
starttime = time.time()
        
# create a connection to a solr server
s = solr.SolrConnection('http://localhost:8983/solr')

# add a document to the index
#s.add(id=1, headline='Test 1', body="sdf se34 #hash a.n.d. 'sdf", byline='byline sdf', section='news', mediumid=3, date=datetime.datetime(2010, 1, 1, tzinfo=GMT1()), sets=(1,2,3))
i = 0

def addArticles(start=0, end=3000):
    global i
    
    articlesDicts = []
    print "selecting articles"
    articles = article.Article.objects.all()[start:end]

    print "finding sets"
    cursor = connection.cursor()
    cursor.execute("SELECT set_id, article_id FROM sets_articles WHERE article_id in(%s)" % ','.join(map(str, [a.id for a in articles])))
    rows = cursor.fetchall()
    print "sets found", len(rows)
    setsDict = {}
    for row in rows:
        articleid = row[1]
        setid = row[0]
        if not articleid in setsDict:
            setsDict[articleid] = []
        setsDict[articleid].append(setid)

    print "creating article dict"
        
    for a in articles:
        articlesDicts.append(dict(id=a.id, headline=a.headline, body=a.text, byline=a.byline, section=a.section, projectid=a.project_id,
                                mediumid=a.medium_id, date=a.date.replace(tzinfo=GMT1()),
                                sets=setsDict.get(a.id))
                            )
        #print "retrieved", i
        i += 1
         
    print "adding"
    s.add_many(articlesDicts)
    
count = article.Article.objects.count()
print "total number of articles", count

stepsize = 5000
for c in range(0, count, stepsize):
    print "start", c
    addArticles(c, c+stepsize)
    
print "committing"
s.commit()
print "optimizing"
s.optimize()

endtime = time.time()

print endtime-starttime
print "number of documents", i
print "docs per second", i / max(int(endtime-starttime), 1)


# print "queries"
# for q in connection.queries:
    # print q

# do a search
# response = s.query('en', highlight=True, fields="body,headline,byline,text",hl_formatter='html')
# for hit in response.results:
    # print hit
    
    
    
# 500 in 15 sec = 2000 / minuut, 20 dagen...
# 15mb voor 4000 docs

# 60.000.000 docs: 500 uur, 220gb


# < 20 sec voor 5000 docs = 15.000 per minuut
# 53mb voor 10.000 docs


# 285 seconds
# number of documents 64993
# docs per second 228
# 272mb

# totaal 60mil = 250gb, 73 uur

