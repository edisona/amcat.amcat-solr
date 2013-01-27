package nl.vu.amcat.similarity;


import  org.apache.lucene.search.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HitCountSimilarity extends Similarity {

    private static final Logger LOG = LoggerFactory.getLogger(HitCountSimilarity.class);


    public float computeNorm(String field, org.apache.lucene.index.FieldInvertState state){
        return 1.0f;
    }

    public float coord(int overlap, int maxOverlap) 
    {
        // Computes a score factor based on the fraction of all query terms that a document contains.
        //System.err.println("coord : " + overlap + " (max:" + maxOverlap+")");
       // LOG.info("queryNorm " + sumOfSquaredWeights);
        return 1.0f;
    }

    
    public float idf(Collection terms, Searcher searcher)
    {
        // Computes a score factor for a phrase.
        //System.err.println("--*-- "+terms.size());
        // System.err.println("idf1 : " + Toolkit.CollToStr(terms));
       // LOG.info("idf " + terms.toString() + " " + searcher.toString());
        return 1.0f;
    }

    public float idf(int docFreq, int numDocs)
    {
        // Computes a score factor based on a term's document frequency (the number of documents which contain the term).
        //System.err.println("--!-- "+docFreq+"/"+numDocs);
        // System.err.println("idf2 : " + docFreq);
       // LOG.info("idf docfreq numdocs " + docFreq + " " + numDocs);
        return 1.0f;
    }

    public float idf(org.apache.lucene.index.Term term, Searcher searcher)
    {
        // Computes a score factor for a simple term.
        //System.err.println("idf3 : " + term);
       // LOG.info("idf term searcher" + term.toString() + " " + searcher.toString());
        return 1.0f;
    }
    
    
    public org.apache.lucene.search.Explanation.IDFExplanation idfExplain(Collection<org.apache.lucene.index.Term> stats, Searcher searcher) {
        // final org.apache.lucene.search.Explanation.IDFExplanation exp = new org.apache.lucene.search.Explanation.IDFExplanation();
        // exp.setDescription("idf(), sum of:");
        // exp.setValue(1.0f);
        // return exp;
        return new org.apache.lucene.search.Explanation.IDFExplanation() {
             public float getIdf()
             {
                 return 1.0f;
             }

             public String explain()
             {
                 return null;
             }
         }; 
  }
    
  

   /* public float lengthNorm(String fieldName, int numTokens)
    {
        // Computes the normalization value for a field given the total number of terms contained in a field.
        // System.err.println("length : " + fieldName + " : " + numTokens);
        return 1.0f;
    }  */
    
    public float queryNorm(float sumOfSquaredWeights)
    {
        // Computes the normalization value for a query given the sum of the squared weights of each of the query terms.
        //System.out.println("queryNorm:" + sumOfSquaredWeights);
       // LOG.info("queryNorm " + sumOfSquaredWeights);
        return 1.0f;// / sumOfSquaredWeights;
    }

    public float sloppyFreq(int distance)
    {
       // LOG.info("sloppyFreq " + distance);
        return 1.0f;
    }

    public float tf(float freq) 
    {
        // Computes a score factor based on a term or phrase's frequency in a document.
        //System.out.println("tf1 : "+ freq);
        //LOG.info("tf " + freq);
        return freq;
    }
    
    public float tf(int freq)
    {
        // Computes a score factor based on a term or phrase's frequency in a document.
        //System.out.println("tf2 : "+ freq);
       // LOG.info("tf float " + freq);
        return freq;
    }
}
