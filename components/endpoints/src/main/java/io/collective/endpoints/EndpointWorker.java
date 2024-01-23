package io.collective.endpoints;

import io.collective.articles.ArticleDataGateway;
import io.collective.rss.RSS;
import io.collective.restsupport.RestTemplate;
import io.collective.workflow.Worker;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class EndpointWorker implements Worker<EndpointTask> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RestTemplate template;
    private final ArticleDataGateway gateway;

    public EndpointWorker(RestTemplate template, ArticleDataGateway gateway) {
        this.template = template;
        this.gateway = gateway;
    }

    @NotNull
    @Override
    public String getName() {
        return "ready";
    }

    @Override
    public void execute(EndpointTask task) throws IOException {
        String response = template.get(task.getEndpoint(), task.getAccept());
        gateway.clear();

        // Parse the RSS response
        XmlMapper xmlMapper = new XmlMapper();
        RSS rss = xmlMapper.readValue(response, RSS.class);

        // Use the getItem method to get the RSS items and save titles to the gateway
        rss.getChannel().getItem().forEach(item -> gateway.save(item.getTitle()));
    }
}
