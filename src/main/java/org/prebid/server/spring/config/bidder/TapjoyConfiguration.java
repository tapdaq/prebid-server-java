package org.prebid.server.spring.config.bidder;

import org.apache.commons.lang3.StringUtils;
import org.prebid.server.bidder.*;
import org.prebid.server.bidder.tapjoy.TapjoyBidder;
import org.prebid.server.bidder.tapjoy.TapjoyMetaInfo;
import org.prebid.server.bidder.tapjoy.TapjoyUsersyncer;
import org.prebid.server.spring.env.YamlPropertySourceFactory;
import org.prebid.server.vertx.http.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@PropertySource(value = "classpath:/bidder-config/tapjoy.yaml", factory = YamlPropertySourceFactory.class)
public class TapjoyConfiguration extends BidderConfiguration {

    private static final String BIDDER_NAME = "tapjoy";

    @Value("${external-url}")
    private String externalUrl;

    @Value("${adapters.tapjoy.enabled}")
    private boolean enabled;

    @Value("${adapters.tapjoy.endpoint}")
    private String endpoint;

    @Value("${adapters.facebook.usersync-url:#{null}}")
    private String usersyncUrl;

    @Value("${adapters.facebook.pbs-enforces-gdpr}")
    private boolean pbsEnforcesGdpr;

    @Value("${adapters.facebook.deprecated-names}")
    private List<String> deprecatedNames;

    @Value("${adapters.facebook.aliases}")
    private List<String> aliases;

    @Bean
    BidderDeps openxBidderDeps(HttpClient httpClient, HttpAdapterConnector httpAdapterConnector) {
        return bidderDeps(httpClient, httpAdapterConnector);
    }

    @Override
    protected String bidderName() {
        return BIDDER_NAME;
    }

    @Override
    protected List<String> deprecatedNames() {
        return deprecatedNames;
    }

    @Override
    protected List<String> aliases() {
        return aliases;
    }

    @Override
    protected MetaInfo createMetaInfo() {
        return new TapjoyMetaInfo(enabled, pbsEnforcesGdpr);
    }

    @Override
    protected Usersyncer createUsersyncer() {
        return new TapjoyUsersyncer(enabled && usersyncUrl != null ? usersyncUrl : StringUtils.EMPTY);
    }

    @Override
    protected Bidder<?> createBidder(MetaInfo metaInfo) {
        return new TapjoyBidder(endpoint);
    }

    @Override
    protected Adapter<?, ?> createAdapter(Usersyncer usersyncer) {
        return null;
    }

    @Override
    protected BidderRequester createBidderRequester(HttpClient httpClient, Bidder<?> bidder, Adapter<?, ?> adapter,
                                                    Usersyncer usersyncer, HttpAdapterConnector httpAdapterConnector) {
        return new HttpBidderRequester<>(bidder, httpClient);
    }
}
