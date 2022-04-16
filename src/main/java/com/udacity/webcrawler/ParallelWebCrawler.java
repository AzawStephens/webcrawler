package com.udacity.webcrawler;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParserFactory;
import com.udacity.webcrawler.profiler.RecursiveWork;
import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;
/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler
{
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;
  private final List<Pattern> ignoredUrls;
  private final PageParserFactory parserFactory;
  private final int maxDepth;
  @Inject
  ParallelWebCrawler(
          Clock clock,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @TargetParallelism int threadCount,
          @IgnoredUrls List<Pattern> ignoredUrls,
          @MaxDepth int maxDepth,
          PageParserFactory parserFactory) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.maxDepth = maxDepth;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.ignoredUrls = ignoredUrls;
    this.parserFactory = parserFactory;
  }
  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    Instant deadline = clock.instant().plus(timeout);
    Map<String, Integer> counts = Collections.synchronizedMap(new HashMap<>());
    ConcurrentSkipListSet<String> visitedUrls = new ConcurrentSkipListSet<>();
    for (String url : startingUrls) {
      pool.invoke(new RecursiveWork(url,deadline,maxDepth,clock,parserFactory,ignoredUrls,counts,visitedUrls));
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
  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }
}
