package de.braintags.io.vertx.util.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertx.core.buffer.Buffer;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class AbstractPojoCodecTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for
   * {@link de.braintags.io.vertx.util.codec.AbstractPojoCodec#encodeToWire(io.vertx.core.buffer.Buffer, java.lang.Object)}
   * .
   */
  @Test
  public void testEncode() {
    UserObject uo = new UserObject();
    UserObjectCodec codec = new UserObjectCodec();
    Buffer buffer = Buffer.buffer();
    codec.encodeToWire(buffer, uo);
    UserObject uo2 = codec.decodeFromWire(0, buffer);

    assertNotSame(uo, uo2);
    assertEquals(uo, uo2);
    assertEquals(codec.systemCodecID(), -1);
    assertEquals(codec.name(), "UserObject");
  }

  public class UserObjectCodec extends AbstractPojoCodec<UserObject, UserObject> {

    @Override
    public UserObject transform(UserObject s) {
      return s;
    }

    @Override
    protected Class<UserObject> getInstanceClass() {
      return UserObject.class;
    }

  }

}
