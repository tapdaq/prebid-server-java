package org.prebid.server.bidder.tapjoy.proto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor(staticName = "of")
@Value
public class ExtImpTapjoy {

  @JsonProperty("sdk_key")
  String sdkKey;

  @JsonProperty("placement_name")
  String placementName;

  @JsonProperty("token")
  String token;

}
