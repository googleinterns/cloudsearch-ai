package com.google.cloudsearch.samples;

import com.google.cloud.language.v1.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import jdk.javadoc.internal.doclets.toolkit.util.Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class naturalLanguageFunctions {

    private static Logger log = Logger.getLogger(BasicRepository.class.getName());

    public static Multimap<Float, Float> naturalLanguageSentiment(String text) throws Exception {
        // Instantiates a client
        log.info("called");
        Multimap<Float, Float> result = ArrayListMultimap.create();
        try (LanguageServiceClient language = LanguageServiceClient.create()) {


            Document doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();

            Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();

            result.put(sentiment.getScore(), sentiment.getMagnitude());
        }
        return result;
    }

    public static Multimap<String, String> naturalLanguageEntity(String text) throws IOException {

        Multimap<String, String> entities= ArrayListMultimap.create();

        try (LanguageServiceClient language = LanguageServiceClient.create()) {
            Document doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
            AnalyzeEntitiesRequest request =
                    AnalyzeEntitiesRequest.newBuilder()
                            .setDocument(doc)
                            .setEncodingType(EncodingType.UTF16)
                            .build();

            AnalyzeEntitiesResponse response = language.analyzeEntities(request);
            FileWriter fw=new FileWriter("testout.txt");

            for (Entity entity : response.getEntitiesList()) {
                try{

                    fw.write(entity.getName()+entity.getType()+"\n");
                    entities.put(entity.getName(), String.valueOf(entity.getType()));



                }catch(Exception e){System.out.println(e);}
            }


            fw.close();
            return entities;

        }
    }

    public static Multimap<String, Float> naturalLanguageClassify(String text) throws IOException {

        Multimap<String, Float> categories = ArrayListMultimap.create();

        FileWriter fw=new FileWriter("testclassify.txt");
        try (LanguageServiceClient language = LanguageServiceClient.create()) {
            // set content to the text string
            Document doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
            ClassifyTextRequest request = ClassifyTextRequest.newBuilder().setDocument(doc).build();
            // detect categories in the given text
            ClassifyTextResponse response = language.classifyText(request);

            for (ClassificationCategory category : response.getCategoriesList()) {
                System.out.printf(
                        "Category name : %s, Confidence : %.3f\n",
                        category.getName(), category.getConfidence());
                fw.write(category.getName() + category.getConfidence()+"\n");
                categories.put(category.getName(), category.getConfidence());

            }
        }
        fw.close();
        return categories;
    }


    public static Multimap<String, Object> getNaturalLanguageFeatures(String content) throws Exception {

        Multimap<String, Object> structuredData = ArrayListMultimap.create();
        structuredData.put("sentiment", naturalLanguageSentiment(content));
        structuredData.put("entities", naturalLanguageEntity(content));
        structuredData.put("categories", naturalLanguageClassify(content));
        return structuredData;
    }
}
