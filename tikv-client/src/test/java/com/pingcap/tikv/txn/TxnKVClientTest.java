package com.pingcap.tikv.txn;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;

import com.google.protobuf.ByteString;
import com.pingcap.tikv.exception.KeyException;
import com.pingcap.tikv.region.RegionStoreClient;
import com.pingcap.tikv.region.TiRegion;
import com.pingcap.tikv.txn.type.ClientRPCResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.tikv.kvproto.Kvrpcpb;

public class TxnKVClientTest {

  @Mock private RegionStoreClient.RegionStoreClientBuilder clientBuilder;

  @Mock private RegionStoreClient client;

  @Mock private TiRegion tiRegion;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    // mock RegionStoreClient
    Mockito.when(clientBuilder.build(tiRegion)).thenReturn(client);
    // mock KeyException with WriteConflict
    Kvrpcpb.KeyError.Builder errBuilder = Kvrpcpb.KeyError.newBuilder();
    errBuilder.setConflict(Kvrpcpb.WriteConflict.newBuilder());
    KeyException keyException = new KeyException(errBuilder.build(), "");
    doThrow(keyException)
        .when(client)
        .prewrite(null, ByteString.copyFromUtf8("writeConflict"), null, 0, 0);
  }

  @Test
  public void testPrewriteWithConflict() {
    TxnKVClient txnKVClient = new TxnKVClient(null, clientBuilder, null);
    ClientRPCResult result =
        txnKVClient.prewrite(null, null, ByteString.copyFromUtf8("writeConflict"), 0, 0, tiRegion);
    assertFalse(result.isRetry());
  }
}
