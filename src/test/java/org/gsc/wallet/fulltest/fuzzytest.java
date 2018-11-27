package org.gsc.wallet.fulltest;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.math.BigInteger;

import lombok.extern.slf4j.Slf4j;
import org.gsc.api.WalletGrpc;
import org.gsc.crypto.ECKey;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.gsc.api.GrpcAPI;
import org.gsc.api.GrpcAPI.AssetIssueList;
import org.gsc.api.WalletExtensionGrpc;
import org.gsc.api.WalletSolidityGrpc;
import org.gsc.common.utils.ByteArray;
import org.gsc.common.utils.Utils;
import org.gsc.core.Wallet;
import org.gsc.db.Manager;
import org.gsc.protos.Contract;
import org.gsc.protos.Protocol;
import org.gsc.common.overlay.Configuration;
import org.gsc.common.overlay.Parameter;
import org.gsc.common.overlay.util.PublicMethed;
import org.gsc.common.overlay.util.TransactionUtils;

@Slf4j
public class fuzzytest {

  //testng001、testng002、testng003、testng004
  private final String testKey002 =
      "FC8BF0238748587B9617EB6D15D47A66C0E07C1A1959033CF249C6532DC29FE6";
  private final String testKey003 =
      "6815B367FDDE637E53E9ADC8E69424E07724333C9A2B973CFA469975E20753FC";

  private final byte[] fromAddress = PublicMethed.getFinalAddress(testKey002);
  private final byte[] toAddress   = PublicMethed.getFinalAddress(testKey003);


  private static long start;
  private static long end;
  private static  long now = System.currentTimeMillis();
  private static String name = "AssetIssue016_" + Long.toString(now);
  private static  long totalSupply = now;
  private static final long sendAmount = 10000000000L;
  private static final long netCostMeasure = 200L;

  Long freeAssetNetLimit = 30000L;
  Long publicFreeAssetNetLimit = 30000L;
  String description = "for case assetissue016";
  String url = "https://stest.assetissue016.url";

  private Manager dbManager;


  private ManagedChannel channelFull = null;
  private ManagedChannel channelSolidity = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;
  private WalletExtensionGrpc.WalletExtensionBlockingStub blockingStubExtension = null;

  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(0);
  private String soliditynode = Configuration.getByPath("testng.conf")
      .getStringList("solidityNode.ip.list").get(0);

  //get account
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] asset017Address = ecKey1.getAddress();
  String testKeyForAssetIssue017 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(Parameter.CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  @BeforeClass(enabled = true)
  public void beforeClass() {
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);

    channelSolidity = ManagedChannelBuilder.forTarget(soliditynode)
        .usePlaintext(true)
        .build();
    blockingStubSolidity = WalletSolidityGrpc.newBlockingStub(channelSolidity);
    blockingStubExtension = WalletExtensionGrpc.newBlockingStub(channelSolidity);

    AssetIssueList assetIssueList = blockingStubFull
        .getAssetIssueList(GrpcAPI.EmptyMessage.newBuilder().build());
    Assert.assertTrue(PublicMethed.freezeBalance(fromAddress,10000000, 3, testKey002,
        blockingStubFull));
    while (assetIssueList.getAssetIssueCount() <= 1) {
      //Sendcoin to this account
      Assert.assertTrue(PublicMethed
          .sendcoin(asset017Address, sendAmount, fromAddress, testKey002, blockingStubFull));
      start = System.currentTimeMillis() + 2000;
      end = System.currentTimeMillis() + 1000000000;
      now = System.currentTimeMillis();
      name = "AssetIssue017_" + Long.toString(now);
      totalSupply = now;
      Assert.assertTrue(createAssetIssue(asset017Address, name, totalSupply, 1, 1,
          start, end, 1, description, url, freeAssetNetLimit, publicFreeAssetNetLimit, 1L,
          1L, testKeyForAssetIssue017, blockingStubFull));

      assetIssueList = blockingStubFull
          .getAssetIssueList(GrpcAPI.EmptyMessage.newBuilder().build());

      ecKey1 = new ECKey(Utils.getRandom());
      asset017Address = ecKey1.getAddress();
      testKeyForAssetIssue017 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
    }
  }

  @Test(enabled = false,threadPoolSize = 5, invocationCount = 5)
  public void tooManyChannelFull() {
    Integer i = 0;
    while (i++ < 20000) {
      ManagedChannel channelFull = null;
      WalletGrpc.WalletBlockingStub blockingStubFull = null;
      channelFull = ManagedChannelBuilder.forTarget(fullnode)
          .usePlaintext(true)
          .build();
      blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
      GrpcAPI.NodeList nodeList = blockingStubFull
          .listNodes(GrpcAPI.EmptyMessage.newBuilder().build());
      if (i % 100 == 0) {
        logger.info(Integer.toString(i));
      }

    }



  }

  @AfterClass(enabled = true)
  public void shutdown() throws InterruptedException {
    /*    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelSolidity != null) {
      channelSolidity.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }*/
  }

  public static Boolean createAssetIssue(byte[] address, String name, Long totalSupply,
      Integer trxNum, Integer icoNum, Long startTime, Long endTime, Integer voteScore,
      String description, String url, Long freeAssetNetLimit, Long publicFreeAssetNetLimit,
      Long fronzenAmount, Long frozenDay, String priKey,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(Parameter.CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    ECKey ecKey = temKey;
    //Protocol.Account search = queryAccount(ecKey, blockingStubFull);
    try {
      Contract.AssetIssueContract.Builder builder = Contract.AssetIssueContract.newBuilder();
      builder.setOwnerAddress(ByteString.copyFrom(address));
      builder.setName(ByteString.copyFrom(name.getBytes()));
      builder.setTotalSupply(totalSupply);
      builder.setGscNum(trxNum);
      builder.setNum(icoNum);
      builder.setStartTime(startTime);
      builder.setEndTime(endTime);
      builder.setVoteScore(voteScore);
      builder.setDescription(ByteString.copyFrom(description.getBytes()));
      builder.setUrl(ByteString.copyFrom(url.getBytes()));
      builder.setFreeAssetNetLimit(freeAssetNetLimit);
      builder.setPublicFreeAssetNetLimit(publicFreeAssetNetLimit);
      Contract.AssetIssueContract.FrozenSupply.Builder frozenBuilder =
          Contract.AssetIssueContract.FrozenSupply.newBuilder();
      frozenBuilder.setFrozenAmount(fronzenAmount);
      frozenBuilder.setFrozenDays(frozenDay);
      builder.addFrozenSupply(0, frozenBuilder);

      Protocol.Transaction transaction = blockingStubFull.createAssetIssue(builder.build());
      if (transaction == null || transaction.getRawData().getContractCount() == 0) {
        logger.info("transaction == null");
        return false;
      }
      transaction = signTransaction(ecKey, transaction);

      GrpcAPI.Return response = blockingStubFull.broadcastTransaction(transaction);
      if (response.getResult() == false) {
        logger.info("failed reason is " + ByteArray.toStr(response.getMessage().toByteArray()));
        return false;
      } else {
        return true;
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  public static Protocol.Transaction signTransaction(ECKey ecKey,
      Protocol.Transaction transaction) {
    Wallet.setAddressPreFixByte(Parameter.CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    if (ecKey == null || ecKey.getPrivKey() == null) {
      //logger.warn("Warning: Can't sign,there is no private key !!");
      return null;
    }
    transaction = TransactionUtils.setTimestamp(transaction);
    return TransactionUtils.sign(transaction, ecKey);
  }
}


