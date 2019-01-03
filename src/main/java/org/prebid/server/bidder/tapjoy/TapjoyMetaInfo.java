package org.prebid.server.bidder.tapjoy;

import org.prebid.server.bidder.MetaInfo;
import org.prebid.server.proto.response.BidderInfo;

import java.util.Arrays;
import java.util.Collections;

public class TapjoyMetaInfo implements MetaInfo {

    private BidderInfo bidderInfo;

    public TapjoyMetaInfo(boolean enabled, boolean pbsEnforcesGdpr) {
        bidderInfo = BidderInfo.create(enabled, "info@tapjoy.com",
                Collections.emptyList(), Arrays.asList("video"),
                null, 0, pbsEnforcesGdpr);
    }

    @Override
    public BidderInfo info() {
        return bidderInfo;
    }
}
