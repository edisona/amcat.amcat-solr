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

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.request.SolrQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Parse Solr's variant on the Lucene {@link org.apache.lucene.queryParser.complexPhrase.ComplexPhraseQueryParser} syntax.
 * <p/>
 * Modified from {@link org.apache.solr.search.LuceneQParserPlugin}
 */
public class ComplexPhraseQParserPlugin extends QParserPlugin {

  public static String NAME = "complexphrase";

  private boolean inOrder = true;

  public void init(NamedList args) {
    if (args != null) {
      Object val = args.get("inOrder");
      if (val != null) {
        inOrder = StrUtils.parseBool(val.toString());
      }
    }
  }

  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {

    ComplexPhraseQParser qParser = new ComplexPhraseQParser(qstr, localParams, params, req);
    qParser.setInOrder(inOrder);
    return qParser;
  }
}

/**
 * Modified from {@link org.apache.solr.search.LuceneQParser}
 */
class ComplexPhraseQParser extends QParser {
  String sortStr;
  SolrComplexPhraseQueryParser lparser;

  boolean inOrder = true;

  /**
   * When <code>inOrder</code> is true, the search terms must
   * exists in the documents as the same order as in query.
   *
   * @param inOrder parameter to choose between ordered or un-ordered proximity search
   */
  public void setInOrder(final boolean inOrder) {
    this.inOrder = inOrder;
  }

  private static final Logger LOG = LoggerFactory.getLogger(ComplexPhraseQParser.class);

  public ComplexPhraseQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    super(qstr, localParams, params, req);
  }

  public Query parse() throws ParseException {
    String qstr = getString();

    String defaultField = getParam(CommonParams.DF);
    if (defaultField == null) {
      defaultField = getReq().getSchema().getDefaultSearchFieldName();
    }
    lparser = new SolrComplexPhraseQueryParser(this, defaultField);
    lparser.setInOrder(inOrder);

    // these could either be checked & set here, or in the SolrQueryParser constructor
    String opParam = getParam(QueryParsing.OP);
    if (opParam != null) {
      lparser.setDefaultOperator("AND".equals(opParam) ? QueryParser.Operator.AND : QueryParser.Operator.OR);
    } else {
      // try to get default operator from schema
      QueryParser.Operator operator = getReq().getSchema().getSolrQueryParser(null).getDefaultOperator();
      lparser.setDefaultOperator(null == operator ? QueryParser.Operator.OR : operator);
    }

    return lparser.parse(qstr);
  }


  public String[] getDefaultHighlightFields() {
    return new String[]{lparser.getField()};
  }

  /**
   * Highlighter does not recognize ComplexPhraseQuery.
   * It must be rewritten in its most primitive form to enable highlighting.
   */
  public Query getHighlightQuery() throws ParseException {

    Query rewritedQuery;

    try {
      rewritedQuery = getQuery().rewrite(getReq().getSearcher().getReader());
    } catch (IOException ioe) {
      rewritedQuery = null;
      LOG.error("query.rewrite() failed", ioe);
    }

    if (rewritedQuery == null)
      return getQuery();
    else
      return rewritedQuery;
  }
}

