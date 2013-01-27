/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.search;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.util.TestHarness;

import java.util.HashMap;


public class ComplexPhraseQParserPluginTest extends AbstractSolrTestCase {

  @Override
  public String getSchemaFile() {
    return "schema-c.xml";
  }

  @Override
  public String getSolrConfigFile() {
    return "solrconfig-c.xml";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }


  public void testDefaultField() {

    assertU(adoc("text", "john smith", "id", "1"));
    assertU(adoc("text", "johathon smith", "id", "2"));
    assertU(adoc("text", "john percival smith", "id", "3"));
    assertU(commit());
    assertU(optimize());

    assertQ(req("q", "{!complexphrase} \"john smith\"")
            , "//result[@numFound='1']"
            , "//doc[./int[@name='id']='1']"
    );

    assertQ(req("q", "{!complexphrase} \"j* smyth~\"")
            , "//result[@numFound='2']"
            , "//doc[./int[@name='id']='1']"
            , "//doc[./int[@name='id']='2']"
    );

    assertQ(req("q", "{!complexphrase} \"(jo* -john) smith\"")
            , "//result[@numFound='1']"
            , "//doc[./int[@name='id']='2']"
    );

    assertQ(req("q", "{!complexphrase} \"jo* smith\"~2")
            , "//result[@numFound='3']"
            , "//doc[./int[@name='id']='1']"
            , "//doc[./int[@name='id']='2']"
            , "//doc[./int[@name='id']='3']"
    );

    assertQ(req("q", "{!complexphrase} \"jo* [sma TO smz]\"")
            , "//result[@numFound='2']"
            , "//doc[./int[@name='id']='1']"
            , "//doc[./int[@name='id']='2']"
    );

    assertQ(req("q", "{!complexphrase} \"john\"")
            , "//result[@numFound='2']"
            , "//doc[./int[@name='id']='1']"
            , "//doc[./int[@name='id']='3']"
    );

    assertQ(req("q", "{!complexphrase} \"(john johathon) smith\"")
            , "//result[@numFound='2']"
            , "//doc[./int[@name='id']='1']"
            , "//doc[./int[@name='id']='2']"
    );

  }

  public void test() {
    HashMap<String, String> args = new HashMap<String, String>();

    args.put(QueryParsing.DEFTYPE, ComplexPhraseQParserPlugin.NAME);
    args.put(CommonParams.FL, "id");

    TestHarness.LocalRequestFactory sumLRF = h.getRequestFactory(
            "standard", 0, 200, args);

    assertU(adoc("name", "john smith", "id", "1"));
    assertU(adoc("name", "johathon smith", "id", "2"));
    assertU(adoc("name", "john percival smith", "id", "3"));
    assertU(commit());
    assertU(optimize());

    assertQ("Simple multi-term still works",
            sumLRF.makeRequest("name:\"john smith\""),
            "//doc[./int[@name='id']='1']",
            "//result[@numFound='1']"
    );

    assertQ(req("q", "{!complexphrase} name:\"john smith\""),
            "//doc[./int[@name='id']='1']",
            "//result[@numFound='1']"
    );


    assertQ("wildcards and fuzzies are OK in phrases",
            sumLRF.makeRequest("name:\"j* smyth~\""),
            "//doc[./int[@name='id']='1']",
            "//doc[./int[@name='id']='2']",
            "//result[@numFound='2']"
    );

    assertQ("boolean logic works",
            sumLRF.makeRequest("name:\"(jo* -john) smith\""),
            "//doc[./int[@name='id']='2']",
            "//result[@numFound='1']"
    );

    assertQ("position logic works",
            sumLRF.makeRequest("name:\"jo*  smith\"~2"),
            "//doc[./int[@name='id']='1']",
            "//doc[./int[@name='id']='2']",
            "//doc[./int[@name='id']='3']",
            "//result[@numFound='3']"
    );

    assertQ("range queries supported",
            sumLRF.makeRequest("name:\"jo* [sma TO smz]\""),
            "//doc[./int[@name='id']='1']",
            "//doc[./int[@name='id']='2']",
            "//result[@numFound='2']"
    );

    assertQ("Simple single-term still works",
            sumLRF.makeRequest("name:\"john\""),
            "//doc[./int[@name='id']='1']",
            "//doc[./int[@name='id']='3']",
            "//result[@numFound='2']"
    );

    assertQ("OR inside phrase works",
            sumLRF.makeRequest("name:\"(john johathon) smith\""),
            "//doc[./int[@name='id']='1']",
            "//doc[./int[@name='id']='2']",
            "//result[@numFound='2']"
    );

  }

  public void testPhraseHighlighter() {
    HashMap<String, String> args = new HashMap<String, String>();

    args.put(QueryParsing.DEFTYPE, ComplexPhraseQParserPlugin.NAME);
    args.put(CommonParams.FL, "id");
    args.put(HighlightParams.HIGHLIGHT, Boolean.TRUE.toString());
    args.put(HighlightParams.USE_PHRASE_HIGHLIGHTER, Boolean.TRUE.toString());
    args.put(HighlightParams.FIELD_MATCH, Boolean.FALSE.toString());

    args.put(HighlightParams.FRAGSIZE, String.valueOf(0));
    args.put(HighlightParams.FIELDS, "name");


    TestHarness.LocalRequestFactory sumLRF = h.getRequestFactory(
            "standard", 0, 200, args);

    assertU(adoc("name", "john smith smith john", "id", "1"));
    assertU(adoc("name", "johathon smith smith johathon", "id", "2"));
    assertU(adoc("name", "john percival smith", "id", "3"));
    assertU(commit());
    assertU(optimize());

    assertQ("range queries supported",
            sumLRF.makeRequest("name:[sma TO smz]"),
            "//doc[./int[@name='id']='1']",
            "//doc[./int[@name='id']='2']",
            "//doc[./int[@name='id']='3']",
            "//result[@numFound='3']"
    );


    sumLRF = h.getRequestFactory("standard", 0, 200, args);
    assertQ("PhraseHighlighter=true Test",
            sumLRF.makeRequest("name:\"(john johathon) smith\""),
            "//lst[@name='highlighting']/lst[@name='1']",
            "//lst[@name='1']/arr[@name='name']/str[.='<em>john</em> <em>smith</em> smith john']",
            "//lst[@name='highlighting']/lst[@name='2']",
            "//lst[@name='2']/arr[@name='name']/str[.='<em>johathon</em> <em>smith</em> smith johathon']"
    );


    args.put(HighlightParams.USE_PHRASE_HIGHLIGHTER, Boolean.FALSE.toString());
    sumLRF = h.getRequestFactory("standard", 0, 200, args);
    assertQ("PhraseHighlighter=false Test",
            sumLRF.makeRequest("name:\"(john johathon) smith\""),
            "//lst[@name='highlighting']/lst[@name='1']",
            "//lst[@name='1']/arr[@name='name']/str[.='<em>john</em> <em>smith</em> <em>smith</em> <em>john</em>']",
            "//lst[@name='highlighting']/lst[@name='2']",
            "//lst[@name='2']/arr[@name='name']/str[.='<em>johathon</em> <em>smith</em> <em>smith</em> <em>johathon</em>']"
    );

    assertQ("Highlight Plain Prefix Query Test",
            sumLRF.makeRequest("name:jo*"),
            "//lst[@name='highlighting']/lst[@name='1']",
            "//lst[@name='1']/arr[@name='name']/str[.='<em>john</em> smith smith <em>john</em>']",
            "//lst[@name='highlighting']/lst[@name='2']",
            "//lst[@name='2']/arr[@name='name']/str[.='<em>johathon</em> smith smith <em>johathon</em>']",
            "//lst[@name='highlighting']/lst[@name='3']",
            "//lst[@name='3']/arr[@name='name']/str[.='<em>john</em> percival smith']"
    );

  }

  public void testUnorderedPhraseQuery() {

    assertU(adoc("text", "protein digest", "id", "1"));
    assertU(adoc("text", "digest protein", "id", "2"));

    assertU(adoc("name", "protein digest", "id", "3"));
    assertU(adoc("name", "digest protein", "id", "4"));

    assertU(commit());
    assertU(optimize());

    /**
     * ordered phrase query return only fist document
     */
    assertQ(req("q", "{!complexphrase} \"protein digest\"")
            , "//result[@numFound='1']"
            , "//doc[./int[@name='id']='1']"
    );

    assertQ(req("q", "{!complexphrase} \"pro* di*\"")
            , "//result[@numFound='1']"
            , "//doc[./int[@name='id']='1']"
    );

    assertQ(req("q", "{!complexphrase} name:\"protein digest\"")
            , "//result[@numFound='1']"
            , "//doc[./int[@name='id']='3']"
    );

    assertQ(req("q", "{!complexphrase} name:\"pro* di*\"")
            , "//result[@numFound='1']"
            , "//doc[./int[@name='id']='3']"
    );

    /**
     * unordered phrase query returns two documents.
     */
    assertQ(req("q", "{!unorderedcomplexphrase} \"digest protein\"")
            , "//result[@numFound='2']"
            , "//doc[./int[@name='id']='1']"
            , "//doc[./int[@name='id']='2']"
    );

    assertQ(req("q", "{!unorderedcomplexphrase} \"di* pro*\"")
            , "//result[@numFound='2']"
            , "//doc[./int[@name='id']='1']"
            , "//doc[./int[@name='id']='2']"
    );

    assertQ(req("q", "{!unorderedcomplexphrase} name:\"digest protein\"")
            , "//result[@numFound='2']"
            , "//doc[./int[@name='id']='3']"
            , "//doc[./int[@name='id']='4']"
    );

    assertQ(req("q", "{!unorderedcomplexphrase} name:\"di* pro*\"")
            , "//result[@numFound='2']"
            , "//doc[./int[@name='id']='3']"
            , "//doc[./int[@name='id']='4']"
    );

  }
  /**
   * the query "sulfur-reducing bacteria" was crashing due to the dash inside the phrase.
   */
  public void testHyphenInPhrase() {

    assertU(adoc("text", "sulfur-reducing bacteria", "id", "1"));
    assertU(adoc("text", "sulfur reducing bacteria", "id", "2"));

    assertU(adoc("name", "sulfur-reducing bacteria", "id", "3"));
    assertU(adoc("name", "sulfur reducing bacteria", "id", "4"));

    assertU(commit());
    assertU(optimize());

    assertQ(req("q", "{!complexphrase} \"sulfur-reducing bacteria\"")
            , "//result[@numFound='2']"
            , "//doc[./int[@name='id']='1']"
            , "//doc[./int[@name='id']='2']"
    );

    assertQ(req("q", "{!complexphrase} name:\"sulfur-reducing bacteria\"")
            , "//result[@numFound='2']"
            , "//doc[./int[@name='id']='3']"
            , "//doc[./int[@name='id']='4']"
    );
  }
}
