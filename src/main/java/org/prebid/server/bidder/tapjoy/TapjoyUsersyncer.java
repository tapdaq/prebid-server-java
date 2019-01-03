package org.prebid.server.bidder.tapjoy;

import org.prebid.server.bidder.Usersyncer;
import org.prebid.server.proto.response.UsersyncInfo;

import java.util.Objects;

/**
 * Facebook {@link Usersyncer} implementation
 */
public class TapjoyUsersyncer implements Usersyncer {

    private final UsersyncInfo usersyncInfo;

    public TapjoyUsersyncer(String usersyncUrl) {
        usersyncInfo = createUsersyncInfo(Objects.requireNonNull(usersyncUrl));
    }

    /**
     * Creates {@link UsersyncInfo} from usersyncUrl
     */
    private static UsersyncInfo createUsersyncInfo(String usersyncUrl) {
        return UsersyncInfo.of(usersyncUrl, "redirect", false);
    }

    /**
     * Returns Tapjoy cookie family
     */
    @Override
    public String cookieFamilyName() {
        return "tapjoy";
    }

    /**
     * Returns Tapjoy {@link UsersyncInfo}
     */
    @Override
    public UsersyncInfo usersyncInfo() {
        return usersyncInfo;
    }
}
