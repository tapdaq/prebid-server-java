package org.prebid.server.bidder.tapjoy;

import com.iab.openrtb.request.BidRequest;
import com.iab.openrtb.request.Imp;
import com.iab.openrtb.request.Video;
import org.junit.Before;
import org.junit.Test;
import org.prebid.server.VertxTest;
import org.prebid.server.bidder.model.BidderError;
import org.prebid.server.bidder.model.HttpRequest;
import org.prebid.server.bidder.model.Result;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class TapjoyBidderTest extends VertxTest {

  private static final String ENDPOINT_URL = "https://bid.tapjoy.com/bid";

  private TapjoyBidder tapjoyBidder;

  @Before
  public void setUp() {
    tapjoyBidder = new TapjoyBidder(ENDPOINT_URL);
  }

  @Test
  public void creationShouldFailOnNullArguments() {
    assertThatNullPointerException().isThrownBy(
        () -> new TapjoyBidder(null));
  }

  @Test
  public void makeHttpRequestsShouldReturnResultWithEmptyBidRequestsAndErrors() {
    // given
    final BidRequest bidRequest = BidRequest.builder()
        .imp(emptyList())
        .build();

    // when
    final Result<List<HttpRequest<BidRequest>>> result = tapjoyBidder.makeHttpRequests(bidRequest);

    // then
    assertThat(result.getValue()).isEmpty();
    assertThat(result.getErrors()).isEmpty();
  }

  @Test
  public void makeHttpRequestsShouldReturnResultWithErrorWhenAppExtOmitted() {
    // given
    final BidRequest bidRequest = BidRequest.builder()
        .imp(singletonList(
            Imp.builder()
                .id("impId")
                .video(Video.builder().build())
                .build()))
        .build();

    // when
    final Result<List<HttpRequest<BidRequest>>> result = tapjoyBidder.makeHttpRequests(bidRequest);

    // then
    assertThat(result.getValue()).hasSize(1)
        .extracting(httpRequest -> mapper.readValue(httpRequest.getBody(), BidRequest.class))
        .flatExtracting(BidRequest::getImp)
        .isEmpty();
    assertThat(result.getErrors()).hasSize(1)
        .containsExactly(BidderError.badInput("tapjoy parameters section is missing"));
  }

}
