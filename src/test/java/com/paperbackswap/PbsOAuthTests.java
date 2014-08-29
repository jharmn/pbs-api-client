package com.paperbackswap;

import org.junit.Assert;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PbsOAuthTests {
    @Test
    public void creates_singleton() {
        PbsOauth pbsOauth = new PbsOauth("X", "Y");
        assertNotNull(pbsOauth);
        assertFalse(pbsOauth.isAuthorized());
    }

    @Test
    public void creates_authorized_singleton() {
        PbsOauth pbsOauth = new PbsOauth("X", "Y", "Z", "ZZ");
        assertNotNull(pbsOauth);
        assertTrue(pbsOauth.isAuthorized());
    }

    @Test
    public void is_signed() {
        //String testUrl = "http://xxyzzz.com/v1/x.html?oauth_consumer_key=6d1c3573504d7497186db11b992b523e&oauth_nonce=5342006803197489593&oauth_signature=16Y0ailVZgk%2BTmZnWxtBM8yCFqQ%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1408860112&oauth_token=1421be5443f688195d09a43c2efa8a6f&oauth_version=1.0&";
        String testUrl = "http://www.xxxxx.com/api/v2/index.php?oauth_consumer_key=6d1c3573504d7497186db11b992b523e&oauth_nonce=5342006803197489593&oauth_signature=16Y0ailVZgk%2BTmZnWxtBM8yCFqQ%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1408860112&oauth_token=1421be5443f688195d09a43c2efa8a6f&oauth_version=1.0&Limit=10&Offset=10";
        Assert.assertTrue(PbsOauth.isSigned(testUrl));
    }

}