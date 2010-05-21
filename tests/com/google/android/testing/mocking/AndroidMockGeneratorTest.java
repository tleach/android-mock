/*
 *  Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.testing.mocking;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Tests for the AndroidMockGenerator class.
 *
 * @author swoodward@google.com (Stephen Woodward)
 */
public class AndroidMockGeneratorTest extends TestCase {
  private AndroidMockGenerator getAndroidMockGenerator() {
    return new AndroidMockGenerator();
  }

  private NoFileAndroidMockGenerator getNoFileMockGenerator() {
    return new NoFileAndroidMockGenerator();
  }

  private void cleanupGeneratedClasses(CtClass... classes) {
    for (CtClass clazz : classes) {
      clazz.detach();
    }
  }

  private <T> void assertUnorderedContentsSame(Iterable<T> expected, Iterable<T> actual) {
    List<T> missingItems = new ArrayList<T>();
    List<T> extraItems = new ArrayList<T>();
    for (T item : expected) {
      missingItems.add(item);
    }
    for (T item : actual) {
      missingItems.remove(item);
      extraItems.add(item);
    }
    for (T item : expected) {
      extraItems.remove(item);
    }
    if (missingItems.size() + extraItems.size() != 0) {
      String errorMessage =
          "Contents were different. Missing: " + Arrays.toString(missingItems.toArray())
              + " Extra: " + Arrays.toString(extraItems.toArray());
      fail(errorMessage);
    }
  }

  private List<String> getExpectedNamesForNumberClass() {
    return getExpectedNamesForNumberClass(false);
  }

  private List<String> getExpectedNamesForObjectClass() {
    List<String> expectedNames = new ArrayList<String>();
    expectedNames.addAll(Arrays.asList(new String[] {"clone", "finalize"}));
    return expectedNames;
  }

  private List<String> getExpectedNamesForNumberClass(boolean includeDelegateMethods) {
    List<String> expectedNames = getExpectedNamesForObjectClass();
    expectedNames.addAll(Arrays.asList(new String[] {"byteValue", "doubleValue", "floatValue",
        "intValue", "longValue", "shortValue"}));
    if (includeDelegateMethods) {
      expectedNames.addAll(Arrays.asList(new String[] {"getDelegate___AndroidMock",
          "setDelegate___AndroidMock"}));
    }
    return expectedNames;
  }

  private List<String> getExpectedNamesForBigIntegerClass() {
    List<String> expectedNames = getExpectedNamesForNumberClass();
    expectedNames.addAll(Arrays.asList(new String[] {"abs", "add", "and", "andNot", "bitCount",
        "bitLength", "clearBit", "compareTo", "divide", "divideAndRemainder", "flipBit", "gcd",
        "getLowestSetBit", "isProbablePrime", "max", "min", "mod", "modInverse", "modPow",
        "multiply", "negate", "nextProbablePrime", "not", "or", "pow", "remainder", "setBit",
        "shiftLeft", "shiftRight", "signum", "subtract", "testBit", "toByteArray", "toString",
        "xor"}));
    return expectedNames;
  }

  private List<String> getMethodNames(CtMethod[] methods) {
    List<String> methodNames = new ArrayList<String>();
    for (CtMethod method : methods) {
      methodNames.add(method.getName());
    }
    return methodNames;
  }

  private List<String> getClassNames(List<CtClass> classes) {
    List<String> classNames = new ArrayList<String>();
    for (CtClass clazz : classes) {
      classNames.add(clazz.getName());
    }
    return classNames;
  }

  private List<String> getExpectedSignaturesForBigIntegerClass() {
    List<String> expectedNames = new ArrayList<String>();
    expectedNames.addAll(Arrays.asList(new String[] {
        "public int java.math.BigInteger.getLowestSetBit()",
        "private static java.math.BigInteger java.math.BigInteger.largePrime(int,int," +
            "java.util.Random)",
        "public int java.math.BigInteger.hashCode()",
        "public final void java.lang.Object.wait() throws java.lang.InterruptedException",
        "boolean java.math.BigInteger.primeToCertainty(int,java.util.Random)",
        "public java.math.BigInteger java.math.BigInteger.abs()",
        "protected void java.lang.Object.finalize() throws java.lang.Throwable",
        "private void java.math.BigInteger.writeObject(java.io.ObjectOutputStream) throws " +
            "java.io.IOException",
        "public java.math.BigInteger java.math.BigInteger.modPow(java.math.BigInteger," +
            "java.math.BigInteger)",
        "private static native void java.lang.Object.registerNatives()",
        "protected native java.lang.Object java.lang.Object.clone() throws " +
            "java.lang.CloneNotSupportedException",
        "public java.math.BigInteger java.math.BigInteger.setBit(int)",
        "private int java.math.BigInteger.parseInt(char[],int,int)",
        "static int java.math.BigInteger.bitCnt(int)",
        "public java.math.BigInteger java.math.BigInteger.shiftRight(int)",
        "public int java.math.BigInteger.bitLength()",
        "public static java.math.BigInteger java.math.BigInteger.valueOf(long)",
        "public java.math.BigInteger java.math.BigInteger.not()",
        "static int java.math.BigInteger.addOne(int[],int,int,int)",
        "public java.math.BigInteger java.math.BigInteger.subtract(java.math.BigInteger)",
        "public java.math.BigInteger java.math.BigInteger.flipBit(int)",
        "public final native void java.lang.Object.wait(long) throws " +
            "java.lang.InterruptedException",
        "public boolean java.math.BigInteger.isProbablePrime(int)",
        "public java.math.BigInteger java.math.BigInteger.add(java.math.BigInteger)",
        "public java.math.BigInteger java.math.BigInteger.modInverse(java.math.BigInteger)",
        "public java.math.BigInteger java.math.BigInteger.clearBit(int)",
        "private static int java.math.BigInteger.intArrayCmpToLen(int[],int[],int)",
        "private boolean java.math.BigInteger.passesMillerRabin(int,java.util.Random)",
        "static void java.math.BigInteger.primitiveLeftShift(int[],int,int)",
        "public int java.math.BigInteger.compareTo(java.lang.Object)",
        "private int java.math.BigInteger.intLength()",
        "private boolean java.math.BigInteger.passesLucasLehmer()",
        "public final native java.lang.Class<?> java.lang.Object.getClass()",
        "public java.math.BigInteger java.math.BigInteger.multiply(java.math.BigInteger)",
        "private static int[] java.math.BigInteger.makePositive(byte[])",
        "private static java.util.Random java.math.BigInteger.getSecureRandom()",
        "public byte java.lang.Number.byteValue()",
        "private static int[] java.math.BigInteger.stripLeadingZeroInts(int[])",
        "public java.math.BigInteger java.math.BigInteger.gcd(java.math.BigInteger)",
        "public float java.math.BigInteger.floatValue()",
        "private java.math.BigInteger java.math.BigInteger.square()",
        "private void java.math.BigInteger.readObject(java.io.ObjectInputStream) throws " +
            "java.io.IOException,java.lang.ClassNotFoundException",
        "private static int[] java.math.BigInteger.montReduce(int[],int[],int,int)",
        "private static int[] java.math.BigInteger.leftShift(int[],int,int)",
        "private static java.math.BigInteger java.math.BigInteger.lucasLehmerSequence(" +
            "int,java.math.BigInteger,java.math.BigInteger)",
        "private static void java.math.BigInteger.destructiveMulAdd(int[],int,int)",
        "private static int java.math.BigInteger.jacobiSymbol(int,java.math.BigInteger)",
        "private static java.math.BigInteger java.math.BigInteger.smallPrime(int,int," +
            "java.util.Random)",
        "private static int[] java.math.BigInteger.makePositive(int[])",
        "public java.lang.String java.math.BigInteger.toString(int)",
        "private static int java.math.BigInteger.bitLength(int[],int)",
        "public java.math.BigInteger java.math.BigInteger.min(java.math.BigInteger)",
        "public int java.math.BigInteger.intValue()",
        "public final native void java.lang.Object.notifyAll()",
        "public java.math.BigInteger java.math.BigInteger.or(java.math.BigInteger)",
        "public java.math.BigInteger java.math.BigInteger.remainder(java.math.BigInteger)",
        "public java.math.BigInteger java.math.BigInteger.divide(java.math.BigInteger)",
        "public boolean java.math.BigInteger.equals(java.lang.Object)",
        "private byte[] java.math.BigInteger.magSerializedForm()",
        "public java.math.BigInteger java.math.BigInteger.xor(java.math.BigInteger)",
        "private static int java.math.BigInteger.intArrayCmp(int[],int[])",
        "private static byte[] java.math.BigInteger.randomBits(int,java.util.Random)",
        "public java.math.BigInteger java.math.BigInteger.and(java.math.BigInteger)",
        "public int java.math.BigInteger.signum()",
        "private static int java.math.BigInteger.subN(int[],int[],int)",
        "public java.math.BigInteger[] java.math.BigInteger.divideAndRemainder(" +
            "java.math.BigInteger)",
        "public java.lang.String java.math.BigInteger.toString()",
        "private int[] java.math.BigInteger.multiplyToLen(int[],int,int[],int,int[])",
        "public java.math.BigInteger java.math.BigInteger.max(java.math.BigInteger)",
        "public java.math.BigInteger java.math.BigInteger.shiftLeft(int)",
        "public double java.math.BigInteger.doubleValue()",
        "public static java.math.BigInteger java.math.BigInteger.probablePrime(int," +
            "java.util.Random)",
        "private static int[] java.math.BigInteger.trustedStripLeadingZeroInts(int[])",
        "int[] java.math.BigInteger.javaIncrement(int[])",
        "public java.math.BigInteger java.math.BigInteger.pow(int)",
        "public short java.lang.Number.shortValue()",
        "public java.math.BigInteger java.math.BigInteger.andNot(java.math.BigInteger)",
        "public byte[] java.math.BigInteger.toByteArray()",
        "private static int[] java.math.BigInteger.stripLeadingZeroBytes(byte[])",
        "public final native void java.lang.Object.notify()",
        "private static int[] java.math.BigInteger.subtract(int[],int[])",
        "private static final int[] java.math.BigInteger.squareToLen(int[],int,int[])",
        "public java.math.BigInteger java.math.BigInteger.negate()",
        "private int java.math.BigInteger.getInt(int)",
        "static int java.math.BigInteger.trailingZeroCnt(int)",
        "private int java.math.BigInteger.signInt()",
        "private int java.math.BigInteger.signBit()",
        "public int java.math.BigInteger.compareTo(java.math.BigInteger)",
        "private int java.math.BigInteger.firstNonzeroIntNum()",
        "public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException",
        "public boolean java.math.BigInteger.testBit(int)",
        "public int java.math.BigInteger.bitCount()",
        "static void java.math.BigInteger.primitiveRightShift(int[],int,int)",
        "public long java.math.BigInteger.longValue()",
        "public java.math.BigInteger java.math.BigInteger.mod(java.math.BigInteger)",
        "static int java.math.BigInteger.mulAdd(int[],int[],int,int,int)",
        "private java.math.BigInteger java.math.BigInteger.mod2(int)",
        "private static java.math.BigInteger java.math.BigInteger.valueOf(int[])",
        "static int java.math.BigInteger.bitLen(int)",
        "public java.math.BigInteger java.math.BigInteger.nextProbablePrime()",
        "private java.math.BigInteger java.math.BigInteger.modPow2(java.math.BigInteger,int)",
        "private java.math.BigInteger java.math.BigInteger.oddModPow(" +
            "java.math.BigInteger,java.math.BigInteger)",
        "private static int[] java.math.BigInteger.add(int[],int[])"}));
    return expectedNames;
  }

  private List<String> getMethodSignatures(Method[] methods) {
    List<String> methodSignatures = new ArrayList<String>();
    for (Method method : methods) {
      methodSignatures.add(method.toGenericString());
    }
    return methodSignatures;
  }

  public void testIsSupportedType() {
    Class<?>[] unsupportedClasses =
        new Class[] {ClassIsAnnotation.class, ClassIsEnum.class, ClassIsFinal.class,
            ClassIsInterface.class};
    Class<?>[] supportedClasses = new Class[] {Object.class};

    for (Class<?> clazz : unsupportedClasses) {
      assertFalse(getAndroidMockGenerator().classIsSupportedType(clazz));
    }
    for (Class<?> clazz : supportedClasses) {
      assertTrue(getAndroidMockGenerator().classIsSupportedType(clazz));
    }
  }

  public void testGetDelegateFieldName() {
    assertEquals("delegateMockObject", getAndroidMockGenerator().getDelegateFieldName());
  }

  public void testGetInterfaceMethodSource() throws SecurityException, NoSuchMethodException {
    Method method = Object.class.getMethod("equals", Object.class);
    assertEquals("public boolean equals(java.lang.Object arg0);", getAndroidMockGenerator()
        .getInterfaceMethodSource(method));
  }

  public void testGetInterfaceMethodSourceMultipleExceptions() throws SecurityException,
      NoSuchMethodException {
    Method method = Class.class.getDeclaredMethod("newInstance");
    assertEquals("public java.lang.Object newInstance() throws java.lang.InstantiationException,"
        + "java.lang.IllegalAccessException;",
        getAndroidMockGenerator().getInterfaceMethodSource(method));
  }
  
  public void testGetInterfaceMethodSourceProtectedMethod() throws SecurityException,
      NoSuchMethodException {
    Method method = Object.class.getDeclaredMethod("finalize");
    assertEquals("public void finalize() throws java.lang.Throwable;",
        getAndroidMockGenerator().getInterfaceMethodSource(method));
  }

  public void testGetInterfaceMethodSourceNoParams() throws SecurityException,
      NoSuchMethodException {
    Method method = Object.class.getMethod("toString");
    assertEquals("public java.lang.String toString();", getAndroidMockGenerator()
        .getInterfaceMethodSource(method));
  }

  public void testGetInterfaceMethodSourceVoidReturn() throws SecurityException,
      NoSuchMethodException {
    Method method = Thread.class.getMethod("run");
    assertEquals("public void run();", getAndroidMockGenerator().getInterfaceMethodSource(method));
  }

  public void testGetInterfaceMethodSourceFinal() throws SecurityException, NoSuchMethodException {
    Method method = Object.class.getMethod("notify");
    try {
      getAndroidMockGenerator().getInterfaceMethodSource(method);
      fail("Exception not thrown on a final method");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  public void testGetInterfaceMethodSourceStatic() throws SecurityException, NoSuchMethodException {
    Method method = Thread.class.getMethod("currentThread");
    try {
      getAndroidMockGenerator().getInterfaceMethodSource(method);
      fail("Exception not thrown on a static method");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  public void testGetInterfaceName() {
    AndroidMockGenerator r = getAndroidMockGenerator();
    assertEquals("genmocks.java.lang.ObjectDelegateInterface", AndroidMock
        .getInterfaceNameFor(Object.class));
  }

  public void testGetSubclassName() {
    AndroidMockGenerator r = getAndroidMockGenerator();
    assertEquals("genmocks.java.lang.ObjectDelegateSubclass", AndroidMock
        .getSubclassNameFor(Object.class));
  }

  public void testGetDelegateMethodSource() throws SecurityException, NoSuchMethodException {
    Method method = Object.class.getMethod("equals", Object.class);
    assertEquals("public boolean equals(java.lang.Object arg0){if(this.delegateMockObject==null){"
        + "return false;}return this.delegateMockObject.equals(arg0);}",
        getAndroidMockGenerator().getDelegateMethodSource(method));
  }

  public void testGetDelegateMethodSourceAllTypes() throws SecurityException,
      NoSuchMethodException {
    String[] returnTypes = new String[] {"boolean", "byte", "short", "int", "long", "char",
        "float", "double"
    };
    String[] castTypes = new String[] {"false", "(byte)0", "(short)0", "(int)0", "(long)0",
        "(char)0", "(float)0", "(double)0"};
    for (int i = 0; i < returnTypes.length; ++i) {
      Method method = AllTypes.class.getMethod(returnTypes[i] + "Foo");
      assertEquals("public " + returnTypes[i] + " " + returnTypes[i]
          + "Foo(){if(this.delegateMockObject==null){return " + castTypes[i]
          + ";}return this.delegateMockObject." + returnTypes[i] + "Foo();}",
          getAndroidMockGenerator().getDelegateMethodSource(method));
    }
    Method method = AllTypes.class.getMethod("objectFoo");
    assertEquals("public java.lang.Object objectFoo(){if(this.delegateMockObject==null){return "
        + "null;}return this.delegateMockObject.objectFoo();}",
        getAndroidMockGenerator().getDelegateMethodSource(method));
    method = AllTypes.class.getMethod("voidFoo");
    assertEquals("public void voidFoo(){if(this.delegateMockObject==null){return ;"
        + "}this.delegateMockObject.voidFoo();}",
        getAndroidMockGenerator().getDelegateMethodSource(method));
  }
  
  private class AllTypes {
    @SuppressWarnings("unused")
    public void voidFoo() {}
    @SuppressWarnings("unused")
    public boolean booleanFoo() {return false;}
    @SuppressWarnings("unused")
    public byte byteFoo() {return 0;}
    @SuppressWarnings("unused")
    public short shortFoo() {return 0;}
    @SuppressWarnings("unused")
    public int intFoo() {return 0;}
    @SuppressWarnings("unused")
    public long longFoo() {return 0;}
    @SuppressWarnings("unused")
    public char charFoo() {return 0;}
    @SuppressWarnings("unused")
    public float floatFoo() {return 0;}
    @SuppressWarnings("unused")
    public double doubleFoo() {return 0;}
    @SuppressWarnings("unused")
    public Object objectFoo() {return null;}
  }

  public void testGetDelegateMethodSourceMultipleExceptions() throws SecurityException,
      NoSuchMethodException {
    Method method = Class.class.getDeclaredMethod("newInstance");
    assertEquals("public java.lang.Object newInstance() throws java.lang.InstantiationException,"
        + "java.lang.IllegalAccessException{if(this.delegateMockObject==null){return null;}return "
        + "this.delegateMockObject.newInstance();}",
    getAndroidMockGenerator().getDelegateMethodSource(method));
  }
  
  public void testGetDelegateMethodSourceProtectedMethod() throws SecurityException,
      NoSuchMethodException {
    Method method = Object.class.getDeclaredMethod("finalize");
    assertEquals("public void finalize() throws java.lang.Throwable{if(this.delegateMockObject=="
        + "null){return ;}this.delegateMockObject.finalize();}",
        getAndroidMockGenerator().getDelegateMethodSource(method));
  }

  public void testGetDelegateMethodSourceMultiParams() throws SecurityException,
      NoSuchMethodException {
    Method method =
        String.class.getMethod("getChars", Integer.TYPE, Integer.TYPE, char[].class, Integer.TYPE);
    assertEquals("public void getChars(int arg0,int arg1,char[] arg2,int arg3){if(this."
        + "delegateMockObject==null){return ;}this.delegateMockObject.getChars(arg0,arg1,arg2,arg3)"
        + ";}", getAndroidMockGenerator().getDelegateMethodSource(method));
  }

  public void testGetDelegateMethodSourceNoParams() throws SecurityException,
      NoSuchMethodException {
    Method method = Object.class.getMethod("toString");
    assertEquals("public java.lang.String toString(){if(this.delegateMockObject==null){return null;"
        + "}return this.delegateMockObject.toString();}",
        getAndroidMockGenerator().getDelegateMethodSource(method));
  }

  public void testGetDelegateMethodSourceVoidReturn() throws SecurityException,
      NoSuchMethodException {
    Method method = Thread.class.getMethod("run");
    assertEquals("public void run(){if(this.delegateMockObject==null){return ;}this."
        + "delegateMockObject.run();}", getAndroidMockGenerator().getDelegateMethodSource(method));
  }

  public void testGetDelegateMethodSourceFinal() throws SecurityException, NoSuchMethodException {
    Method method = Object.class.getMethod("notify");
    try {
      getAndroidMockGenerator().getDelegateMethodSource(method);
      fail("Exception not thrown on a final method");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  public void testGetDelegateMethodSourceStatic() throws SecurityException, NoSuchMethodException {
    Method method = Thread.class.getMethod("currentThread");
    try {
      getAndroidMockGenerator().getDelegateMethodSource(method);
      fail("Exception not thrown on a static method");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  public void testGenerateEmptySubclass() throws ClassNotFoundException, NotFoundException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    CtClass generatedInterface = mockGenerator.generateInterface(String.class);
    CtClass generatedClass =
        getAndroidMockGenerator().generateSkeletalClass(String.class, generatedInterface);

    assertEquals("genmocks.java.lang", generatedClass.getPackageName());
    assertEquals("StringDelegateSubclass", generatedClass.getSimpleName());
    assertEquals("java.lang.String", generatedClass.getSuperclass().getName());
    cleanupGeneratedClasses(generatedInterface, generatedClass);
  }

  public void testAddMethods() throws ClassNotFoundException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    CtClass generatedInterface = mockGenerator.generateInterface(Number.class);
    CtClass generatedClass = mockGenerator.generateSkeletalClass(Number.class, generatedInterface);

    mockGenerator.addMethods(Number.class, generatedClass);

    List<String> expectedNames = getExpectedNamesForNumberClass();
    List<String> actualNames = getMethodNames(generatedClass.getDeclaredMethods());
    assertUnorderedContentsSame(expectedNames, actualNames);
    cleanupGeneratedClasses(generatedInterface, generatedClass);
  }

  public void testAddMethodsObjectClass() throws ClassNotFoundException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    CtClass generatedInterface = mockGenerator.generateInterface(Object.class);
    CtClass generatedClass = mockGenerator.generateSkeletalClass(Object.class, generatedInterface);

    mockGenerator.addMethods(Object.class, generatedClass);

    List<String> expectedNames = getExpectedNamesForObjectClass();
    List<String> actualNames = getMethodNames(generatedClass.getDeclaredMethods());
    assertUnorderedContentsSame(expectedNames, actualNames);
    cleanupGeneratedClasses(generatedInterface, generatedClass);
  }

  public void testAddMethodsUsesSuperclass() throws ClassNotFoundException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    CtClass generatedInterface = mockGenerator.generateInterface(BigInteger.class);
    CtClass generatedClass =
        mockGenerator.generateSkeletalClass(BigInteger.class, generatedInterface);

    mockGenerator.addMethods(BigInteger.class, generatedClass);

    List<String> expectedNames = getExpectedNamesForBigIntegerClass();
    List<String> actualNames = getMethodNames(generatedClass.getDeclaredMethods());
    assertUnorderedContentsSame(expectedNames, actualNames);
    cleanupGeneratedClasses(generatedInterface, generatedClass);
  }

  public void testGetAllMethods() throws ClassNotFoundException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    CtClass generatedInterface = mockGenerator.generateInterface(BigInteger.class);
    CtClass generatedClass =
        mockGenerator.generateSkeletalClass(BigInteger.class, generatedInterface);

    Method[] methods = mockGenerator.getAllMethods(BigInteger.class);

    List<String> expectedNames = getExpectedSignaturesForBigIntegerClass();
    List<String> actualNames = getMethodSignatures(methods);
    assertUnorderedContentsSame(expectedNames, actualNames);
    cleanupGeneratedClasses(generatedInterface, generatedClass);
  }

  public void testGenerateInterface() {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    CtClass generatedInterface = mockGenerator.generateInterface(Number.class);

    List<String> expectedNames = getExpectedNamesForNumberClass();
    List<String> actualNames = getMethodNames(generatedInterface.getDeclaredMethods());
    assertUnorderedContentsSame(expectedNames, actualNames);
    cleanupGeneratedClasses(generatedInterface);
  }

  public void testAddInterfaceMethods() {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    CtClass generatedInterface = mockGenerator.getClassPool().makeInterface("testInterface");

    mockGenerator.addInterfaceMethods(Number.class, generatedInterface);

    List<String> expectedNames = getExpectedNamesForNumberClass();
    List<String> actualNames = getMethodNames(generatedInterface.getDeclaredMethods());
    assertUnorderedContentsSame(expectedNames, actualNames);
    cleanupGeneratedClasses(generatedInterface);
  }

  public void testGenerateSubclass() throws ClassNotFoundException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    CtClass generatedInterface = mockGenerator.generateInterface(Number.class);

    CtClass generatedClass = mockGenerator.generateSubClass(Number.class, generatedInterface);

    List<String> expectedNames = getExpectedNamesForNumberClass(true);
    List<String> actualNames = getMethodNames(generatedClass.getDeclaredMethods());
    assertUnorderedContentsSame(expectedNames, actualNames);
    cleanupGeneratedClasses(generatedInterface, generatedClass);
  }

  public void testCreateMockForClass() throws ClassNotFoundException {
    NoFileAndroidMockGenerator mockGenerator = getNoFileMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(Object.class);

    List<String> expectedNames = new ArrayList<String>();
    expectedNames.addAll(Arrays.asList(new String[] {"genmocks.java.lang.ObjectDelegateSubclass",
        "genmocks.java.lang.ObjectDelegateInterface"}));
    List<String> actualNames = getClassNames(classes);
    assertUnorderedContentsSame(expectedNames, actualNames);
  }

  public void testGetSetDelegateMethodSource() {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    CtClass generatedInterface = mockGenerator.generateInterface(Object.class);
    String expectedSource =
        "public void setDelegate___AndroidMock(genmocks.java.lang.ObjectDelegateInterface obj) {"
            + " this.delegateMockObject = obj;}";

    assertEquals(expectedSource, mockGenerator.getSetDelegateMethodSource(generatedInterface));
  }

  public void testIsForbiddenMethod() throws SecurityException, NoSuchMethodException {
    Method[] forbiddenMethods =
        new Method[] {Object.class.getMethod("equals", Object.class),
            Object.class.getMethod("toString"), Object.class.getMethod("hashCode")};
    Method[] allowedMethods = new Method[] {BigInteger.class.getMethod("toString", Integer.TYPE)};
    for (Method method : forbiddenMethods) {
      assertTrue(getAndroidMockGenerator().isForbiddenMethod(method));
    }
    for (Method method : allowedMethods) {
      assertFalse(getAndroidMockGenerator().isForbiddenMethod(method));
    }
  }

  /**
   * Support test class for capturing the names of files that would have been saved to a jar file.
   *
   * @author swoodward@google.com (Stephen Woodward)
   */
  class NoFileAndroidMockGenerator extends AndroidMockGenerator {
    List<CtClass> savedClasses = new ArrayList<CtClass>();

    @Override
    void saveCtClass(CtClass clazz) {
      savedClasses.add(clazz);
    }
  }
}
