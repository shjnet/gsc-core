package org.gsc.core.db;

import java.io.File;

import org.gsc.core.wrapper.TransactionInfoWrapper;
import org.gsc.db.TransactionHistoryStore;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.gsc.common.utils.ByteArray;
import org.gsc.common.utils.FileUtil;
import org.gsc.core.Constant;
import org.gsc.config.DefaultConfig;
import org.gsc.config.args.Args;
import org.gsc.core.exception.BadItemException;

public class TransactionHistoryTest {

  private static String dbPath = "output_TransactionHistoryStore_test";
  private static String dbDirectory = "db_TransactionHistoryStore_test";
  private static String indexDirectory = "index_TransactionHistoryStore_test";
  private static AnnotationConfigApplicationContext context;
  private static TransactionHistoryStore transactionHistoryStore;
  private static final byte[] transactionId = TransactionStoreTest.randomBytes(32);

  static {
    Args.setParam(
        new String[]{
            "--output-directory", dbPath,
            "--storage-db-directory", dbDirectory,
            "--storage-index-directory", indexDirectory
        },
        Constant.TEST_CONF
    );
    context = new AnnotationConfigApplicationContext(DefaultConfig.class);
  }

  @AfterClass
  public static void destroy() {
    Args.clearParam();
    FileUtil.deleteDir(new File(dbPath));
    context.destroy();
  }

  @BeforeClass
  public static void init() {
    transactionHistoryStore = context.getBean(TransactionHistoryStore.class);
    TransactionInfoWrapper transactionInfoWrapper = new TransactionInfoWrapper();

    transactionInfoWrapper.setId(transactionId);
    transactionInfoWrapper.setFee(1000L);
    transactionInfoWrapper.setBlockNumber(100L);
    transactionInfoWrapper.setBlockTimeStamp(200L);
    transactionHistoryStore.put(transactionId, transactionInfoWrapper);
  }

  @Test
  public void get() throws BadItemException {
    //test get and has Method
    TransactionInfoWrapper resultCapsule = transactionHistoryStore.get(transactionId);
    Assert.assertEquals(1000L, resultCapsule.getFee());
    Assert.assertEquals(100L, resultCapsule.getBlockNumber());
    Assert.assertEquals(200L, resultCapsule.getBlockTimeStamp());
    Assert.assertEquals(ByteArray.toHexString(transactionId),
        ByteArray.toHexString(resultCapsule.getId()));
  }
}