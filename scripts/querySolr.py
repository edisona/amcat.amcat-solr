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


#do a search
response = s.query('{!complexphrase}"premi* (sal* OR balk*)"~10', highlight=True, fields="*", hl_usePhraseHighlighter='true', hl_highlightMultiTerm='true')
#print response.__dict__
for hit in response.results:
    print hit
for hl in response.highlighting.iteritems():
    print hl
    
    
"""
# More like this:
# http://localhost:8983/solr/mlt?q=id:36816310&mlt.fl=body&mlt.mindf=1&mlt.mintf=1

# faceting:
#http://localhost:8983/solr/select?indent=on&q=projectid:291&fl=name&facet=true&facet.field=projectid&facet.field=mediumid&facet.query=projectid:291%20AND%20mediumid:7

<str name="facet.field">YOUR_TEXTFIELD</str>
<str name="facet.range">ID</str>        <--- ID=field woth the document ID
<str name="f.ID.facet.range.gap">1</str> <--- count ID in step of 1
<str name="f.ID.facet.range.start">0</str>   <--- start ID for faceted search
<str name="q">dell</str>                   <---string, "keyword" to look/count for
<str name="f.ID.facet.range.end">1000</str> <--- end ID for faceted search
<str name="facet">true</str>
<str name="facet.method">enum</str>
#http://localhost:8983/solr/select?indent=on&q=projectid:291%20AND%20des&facet=true&facet.field=text&facet.range=0&f.text.range.gap=1&f.text.range.start=0&f.text.range.end=99999999

# highligh
# not working, fieldmatch = true, http://localhost:8983/solr/select/?indent=on&q=des&fl=id,headline,body&hl=true&hl.fl=body,headline&hl.snippets=3&hl.mergeContiguous=true&hl.requireFieldMatch=true&hl.usePhraseHighlighter=true&hl.highlightMultiTerm=true
#  http://localhost:8983/solr/select/?indent=on&q=des&fl=id,headline,body&hl=true&hl.fl=body,headline&hl.snippets=3&hl.mergeContiguous=true&hl.usePhraseHighlighter=true&hl.highlightMultiTerm=true


hitcount:
http://wiki.apache.org/solr/TermVectorComponent  nee

ugly: http://localhost:8983/solr/select/?rows=100&indent=on&q=de&fl=id&hl=true&hl.fragsize=1&hl.fl=body,headline&hl.snippets=999&hl.mergeContiguous=false&hl.usePhraseHighlighter=true&hl.highlightMultiTerm=true&hl.maxAnalyzedChars=10000000

http://localhost:8983/solr/select/?q=man%20AND%20projectid:291^0&version=2.2&start=0&rows=10&indent=on&fl=*,score&debugQuery=true&hl=true&hl.fl=body,headline&hl.snippets=3&hl.mergeContiguous=true&hl.usePhraseHighlighter=true&hl.highlightMultiTerm=true

"""
