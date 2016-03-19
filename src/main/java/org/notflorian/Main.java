package org.notflorian;


import me.jhenrique.manager.TweetManager;
import me.jhenrique.manager.TwitterCriteria;
import me.jhenrique.model.Tweet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class Main {

    public static void main(String[] args) {
        try {
            List<String> englishStopWords = readFile("englishST.txt");
            List<String> frenchStopWords = readFile("frenchST.txt");
            List<String> twitterKeywords = readFile("twitterKeywords.txt");
            List<String> ignoredWords = readFile("ignored.txt");

            TwitterCriteria criteria = TwitterCriteria.create()
                    .setQuerySearch("Devoxx OR DevoxxFr")
                    .setMaxTweets(1000);


            List<Tweet> tweets = TweetManager.getTweets(criteria);

            Stream<String> filtredWords = tweets.stream()
                    .flatMap(tweet -> Arrays.asList(tweet.getText().split("\\s|\\!|\\,|\\;|\\(|\\)|\\'|\\’|\\\"|\\►|\\<|\\>|\\«|\\»|\\▶︎")).stream())

                    .filter(word -> !word.startsWith("http"))
                    .filter(word -> !word.startsWith("https"))

                    .flatMap(word -> Arrays.asList(word.split("\\.|\\:|\\/|\\&|\\=|\\?|\\%|\\+")).stream())

                    .map(word -> word.replace("#", "").toLowerCase().trim())
                    .filter(StringUtils::isNotEmpty)

                    .filter(word -> word.length() > 1)
                    .filter(word -> !word.startsWith("@"))
                    .filter(word -> !englishStopWords.contains(word))
                    .filter(word -> !frenchStopWords.contains(word))
                    .filter(word -> !twitterKeywords.contains(word))
                    .filter(word -> !StringUtils.startsWithAny(word, (CharSequence[]) ignoredWords.toArray()))
                    .filter(word -> !StringUtils.startsWithAny(word, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));

            Stream<Map.Entry<String, Long>> wordFrequency = filtredWords.collect(groupingBy(Function.identity(), counting()))
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() >= 3)
                    .sorted((a, b) -> a.getValue().equals(b.getValue()) ? a.getKey().compareTo(b.getKey()) : b.getValue().intValue() - a.getValue().intValue());

//        wordFrequency.forEach(System.out::println);

            List<String> words = wordFrequency.collect(Collectors.mapping(entry -> unGroupBy(entry.getKey(), entry.getValue()), Collectors.toList()));

            System.out.println(words.size());

            // https://www.jasondavies.com/wordcloud/
            FileUtils.write(new File("./words.txt"), words.stream().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String unGroupBy(String word, Long count) {
        StringBuilder words = new StringBuilder(word);
        for (int i = 1; i < count; i++) {
            words.append(' ').append(word);
        }

        return words.toString();
    }


    public static List<String> readFile(String file) throws IOException {
        return IOUtils.readLines(Main.class.getResourceAsStream("/" + file));
    }
}
