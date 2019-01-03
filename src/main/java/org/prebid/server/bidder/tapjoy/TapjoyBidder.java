package org.prebid.server.bidder.tapjoy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iab.openrtb.request.*;
import com.iab.openrtb.response.BidResponse;
import com.iab.openrtb.response.SeatBid;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.prebid.server.bidder.Bidder;
import org.prebid.server.bidder.BidderUtil;
import org.prebid.server.bidder.model.*;
import org.prebid.server.bidder.tapjoy.proto.ExtImpTapjoy;
import org.prebid.server.exception.PreBidException;
import org.prebid.server.proto.openrtb.ext.ExtPrebid;
import org.prebid.server.proto.openrtb.ext.request.facebook.ExtImpFacebook;
import org.prebid.server.proto.openrtb.ext.response.BidType;
import org.prebid.server.util.HttpUtil;

import java.util.*;
import java.util.stream.Collectors;

import static org.prebid.server.bidder.BidderUtil.APPLICATION_JSON;

/**
 * Facebook {@link Bidder} implementation.
 */
public class TapjoyBidder implements Bidder<BidRequest> {

    private static final TypeReference<ExtPrebid<?, ExtImpTapjoy>> TAPJOY_EXT_TYPE_REFERENCE =
            new TypeReference<ExtPrebid<?, ExtImpTapjoy>>() {
            };

    private final String endpointUrl;

    public TapjoyBidder(String endpointUrl) {
        this.endpointUrl = HttpUtil.validateUrl(Objects.requireNonNull(endpointUrl));
    }

    @Override
    public Result<List<HttpRequest<BidRequest>>> makeHttpRequests(BidRequest bidRequest) {
        if (CollectionUtils.isEmpty(bidRequest.getImp())) {
            return Result.of(Collections.emptyList(), Collections.emptyList());
        }

        final List<BidderError> errors = new ArrayList<>();
        final List<Imp> processedImps = new ArrayList<>();

        ExtImpTapjoy extImpTapjoy = null;
        try {
            extImpTapjoy = parseExtImpTapjoy(bidRequest.getImp().get(0));

            for (final Imp imp : bidRequest.getImp()) {
                processedImps.add(makeImp(imp));
            }
        } catch (PreBidException e) {
            errors.add(BidderError.badInput(e.getMessage()));
        }

        final BidRequest outgoingRequest = bidRequest.toBuilder()
                .imp(processedImps)
                .device(makeDevice(bidRequest))
                .app(makeApp(bidRequest, extImpTapjoy))
                .build();
        final String body = Json.encode(outgoingRequest);

        MultiMap outgoingHeaders = BidderUtil.headers();
        outgoingHeaders.set(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON + "; name=tapdaq");

        return Result.of(Collections.singletonList(
                HttpRequest.<BidRequest>builder()
                        .method(HttpMethod.POST)
                        .uri(endpointUrl)
                        .body(body)
                        .headers(outgoingHeaders)
                        .payload(outgoingRequest)
                        .build()),
                errors);
    }

    @Override
    public Result<List<BidderBid>> makeBids(HttpCall httpCall, BidRequest bidRequest) {
        try {
            final BidResponse bidResponse = Json.decodeValue(httpCall.getResponse().getBody(), BidResponse.class);
            return Result.of(extractBids(bidResponse), Collections.emptyList());
        } catch (DecodeException e) {
            return Result.emptyWithError(BidderError.badServerResponse(e.getMessage()));
        }
    }

    @Override
    public Map<String, String> extractTargeting(ObjectNode ext) {
        return Collections.emptyMap();
    }

    private ExtImpTapjoy parseExtImpTapjoy(Imp imp) {
        if (imp.getExt() == null) {
            throw new PreBidException("tapjoy parameters section is missing");
        }

        try {
            return Json.mapper.<ExtPrebid<?, ExtImpTapjoy>>convertValue(imp.getExt(), TAPJOY_EXT_TYPE_REFERENCE)
                    .getBidder();
        } catch (IllegalArgumentException e) {
            throw new PreBidException(e.getMessage(), e);
        }
    }

    private Imp makeImp(Imp imp) {
        return imp.toBuilder()
                .displaymanager("tapdaq")
                .video(makeVideo(imp))
                .build();
    }

    private static Device makeDevice(BidRequest bidRequest) {
        final Device device = bidRequest.getDevice();
        if (device == null) {
            return null;
        }

        return device.toBuilder()
                .build();
    }

    private static App makeApp(BidRequest bidRequest, ExtImpTapjoy extImpTapjoy) {
        final App app = bidRequest.getApp();
        if (app == null || extImpTapjoy == null) {
            return null;
        }

        return app.toBuilder()
                .ext(Json.mapper.valueToTree(extImpTapjoy))
                .build();
    }

    private static List<BidderBid> extractBids(BidResponse bidResponse) {
        return bidResponse == null || bidResponse.getSeatbid() == null
                ? Collections.emptyList()
                : bidsFromResponse(bidResponse);
    }

    private static Video makeVideo(Imp imp) {
        final Video video = imp.getVideo();
        if (video == null) {
            return null;
        }

        return video;
    }

    private static List<BidderBid> bidsFromResponse(BidResponse bidResponse) {

        return bidResponse.getSeatbid().stream()
                .filter(Objects::nonNull)
                .map(SeatBid::getBid)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(bid -> BidderBid.of(bid, BidType.video, null))
                .collect(Collectors.toList());
    }
}
