package com.udacity.webcrawler;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;
import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
/**
 * A {@link WebCrawler} that downloads and processes one page at a time.
 */
final class SequentialWebCrawler implements WebCrawler {
  private final Clock clock;
  private final PageParserFactory parserFactory;
  private final Duration timeout;
  private final int popularWordCount;
  private final int maxDepth;
  private final List<Pattern> ignoredUrls;
  @Inject
  SequentialWebCrawler(
          Clock clock,
          PageParserFactory parserFactory,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @MaxDepth int maxDepth,
          @IgnoredUrls List<Pattern> ignoredUrls) {
    this.clock = clock;
    this.parserFactory = parserFactory;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
  }
  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    Instant deadline = clock.instant().plus(timeout);//amount of time the crawl is allowed to crawl
    Map<String, Integer> counts = new HashMap<>(); //how many times a certain word was present
    Set<String> visitedUrls = new HashSet<>(); //set of visited Urls
    for (String url : startingUrls) {
      crawlInternal(url, deadline, maxDepth, counts, visitedUrls); //for every url in the List of urls go through CrawlInternal
    }
    if (counts.isEmpty()) {
      return new CrawlResult.Builder()
              .setWordCounts(counts)//if a word was not found in the crawl, set word count to the default,
              .setUrlsVisited(visitedUrls.size()) // set the set of urls visited to the size of urls that were visited during the crawl
              .build();  //Lastly, build the Crawl result
    }
    return new CrawlResult.Builder()
            .setWordCounts(WordCounts.sort(counts, popularWordCount)) //If the crawl did find words, add them to the CrawlResult, sorting them by popular words
            .setUrlsVisited(visitedUrls.size())//set the amount of urls that were visited
            .build();//Lastly, build the CrawlResult
  }
  //Below is where the magic takes place.
  private void crawlInternal(
          String url,
          Instant deadline,
          int maxDepth,
          Map<String, Integer> counts,
          Set<String> visitedUrls) {
    if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
      return; //stop crawling after a certain amount of time
    }
    for (Pattern pattern : ignoredUrls) {
      if (pattern.matcher(url).matches()) {
        return; //stops ignored urls from being added
      }
    }
    if (visitedUrls.contains(url)) {
      return; //stops duplicate urls from being added
    }
    visitedUrls.add(url); //If everything is good, add the url to visited urls set
    PageParser.Result result = parserFactory.get(url).parse(); //parses the url to the PageParser
    for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) { //Get the entry set for every word count
      if (counts.containsKey(e.getKey())) {
        counts.put(e.getKey(), e.getValue() + counts.get(e.getKey())); //if the counts collection contains the entry set, put the entry into the count collection and get the entry set
      } else {
        counts.put(e.getKey(), e.getValue()); //if the counts collection does not conatin the entry set put the entry into the counts
      }
    }
    for (String link : result.getLinks()) {
      crawlInternal(link, deadline, maxDepth - 1, counts, visitedUrls);
    }
  }
}