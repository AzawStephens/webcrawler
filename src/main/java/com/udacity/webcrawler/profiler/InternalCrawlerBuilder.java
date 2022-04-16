package com.udacity.webcrawler.profiler;


import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class InternalCrawlerBuilder {

    private final String url;
    private final Instant deadline;
    private final int maxDepth;
    private final Map<String, Integer> counts;
    private final Set<String> visitedUrls;


    private InternalCrawlerBuilder(String url, Instant deadline, int maxDepth, Map<String, Integer> counts, Set<String> visitedUrls) {
        this.url = url;
        this.deadline = deadline;
        this.maxDepth = maxDepth;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
    }

    public String getUrl()
    {
        return url;
    }
    public Instant getDeadline()
    {
        return deadline;
    }
    public int getMaxDepth()
    {
        return maxDepth;
    }
    public Map<String, Integer> getCounts()
    {
        return counts;
    }
    public Set<String> getVisitedUrls()
    {
        return visitedUrls;
    }


    public static final class Builder
    {
        private  String url;
        private  Instant deadline;
        private  int maxDepth;
        private  Map<String, Integer> counts;
        private  Set<String> visitedUrls;


        public Builder setUrl(String url)
        {
            this.url = url;
            return this;
        }
        public Builder setDeadLine(Instant deadLine)
        {
            this.deadline = deadLine;
            return this;
        }
        public Builder setMaxDepth(int maxDepth)
        {
            this.maxDepth = maxDepth;
            return this;
        }
        public Builder setCounts(Map<String, Integer> counts)
        {
            this.counts =counts;
            return this;
        }
        public Builder setVisitedUrls(Set<String> visitedUrls)
        {
            this.visitedUrls = visitedUrls;
            return this;
        }


        public InternalCrawlerBuilder build()
        {
            return new InternalCrawlerBuilder(url, deadline,maxDepth,counts,visitedUrls);
        }

    }
}