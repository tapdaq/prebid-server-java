package org.prebid.server.bidder;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iab.openrtb.request.BidRequest;
import org.prebid.server.bidder.model.BidderBid;
import org.prebid.server.bidder.model.HttpCall;
import org.prebid.server.bidder.model.HttpRequest;
import org.prebid.server.bidder.model.Result;

import java.util.List;
import java.util.Map;

/**
 * Defines the contract needed to participate in an auction.
 */
public interface Bidder<T> {

    /**
     * Makes the HTTP requests which should be made to fetch bids.
     * <p>
     * The errors should contain a list of errors which explain why this bidder's bids will be "subpar" in some way.
     * For example: the request contained ad types which this bidder doesn't support.
     */
    Result<List<HttpRequest<T>>> makeHttpRequests(BidRequest request);

    /**
     * Unpacks the server's response into bids.
     * <p>
     * The errors should contain a list of errors which explain why this bidder's bids will be
     * "subpar" in some way. For example: the server response didn't have the expected format.
     */
    Result<List<BidderBid>> makeBids(HttpCall<T> httpCall, BidRequest bidRequest);

    /**
     * Extracts targeting from bidder-specific extension. It is safe to assume that {@code ext} is not null.
     */
    Map<String, String> extractTargeting(ObjectNode ext);
}
