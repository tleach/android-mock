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

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import junit.framework.TestCase;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Various tests that verify that different types of Classes are handled correctly.
 *
 * @author swoodward@google.com (Stephen Woodward)
 */
public class ClassTypeTests extends TestCase {
  private AndroidMockGenerator androidMockGenerator = new AndroidMockGenerator();

  private AndroidMockGenerator getAndroidMockGenerator() {
    return androidMockGenerator;
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

  private List<String> getExpectedNames(Class<?> clazz) {
    return new ArrayList<String>(Arrays.asList(new String[] {
        "genmocks." + clazz.getCanonicalName() + "DelegateInterface",
        "genmocks." + clazz.getCanonicalName() + "DelegateSubclass"}));
  }

  private List<String> getMethodNames(Method[] methods, String[] exclusions) {
    List<String> methodNames = new ArrayList<String>();
    for (Method method : methods) {
      if (!Arrays.asList(exclusions).contains(method.getName())) {
        methodNames.add(method.getName());
      }
    }
    return methodNames;
  }

  private List<String> getMethodNames(CtMethod[] methods) {
    List<String> methodNames = new ArrayList<String>();
    for (CtMethod method : methods) {
      methodNames.add(method.getName());
    }
    return methodNames;
  }

  private Map<String, List<String>> getExpectedMethodsMap(List<String> expectedNames,
      Class<?> clazz) {
    return getExpectedMethodsMap(expectedNames, clazz, new String[0]);
  }

  private Map<String, List<String>> getExpectedMethodsMap(List<String> expectedNames,
      Class<?> clazz, String[] exclusions) {
    Map<String, List<String>> expectedMethods = new HashMap<String, List<String>>();
    expectedMethods.put(expectedNames.get(0), new ArrayList<String>(Arrays.asList(new String[] {
        "finalize", "clone"})));
    expectedMethods.put(expectedNames.get(1), new ArrayList<String>(Arrays.asList(new String[] {
        "finalize", "clone", "setDelegate___AndroidMock", "getDelegate___AndroidMock"})));
    expectedMethods.get(expectedNames.get(0)).addAll(
        getMethodNames(clazz.getDeclaredMethods(), exclusions));
    expectedMethods.get(expectedNames.get(1)).addAll(
        getMethodNames(clazz.getDeclaredMethods(), exclusions));
    return expectedMethods;
  }

  public void testClassIsDuplicate() throws ClassNotFoundException {
    List<CtClass> classList = getAndroidMockGenerator().createMocksForClass(Object.class);
    List<CtClass> secondClassList = getAndroidMockGenerator().createMocksForClass(Object.class);
    assertEquals(classList, secondClassList);
  }

  public void testClassHasDelegateMethods() throws ClassNotFoundException, IOException,
      CannotCompileException {
    List<String> expectedNames = getExpectedNames(ClassHasDelegateMethods.class);
    Map<String, List<String>> expectedMethods =
        getExpectedMethodsMap(expectedNames, ClassHasDelegateMethods.class,
            new String[] {"getDelegate___AndroidMock"});
    // This use case doesn't fit our util in any nice way, so just tweak it.
    expectedMethods.get(
        "genmocks.com.google.android.testing.mocking.ClassHasDelegateMethodsDelegateInterface").add(
        "getDelegate___AndroidMock");

    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(ClassHasDelegateMethods.class);
    assertEquals(2, classes.size());
    for (CtClass clazz : classes) {
      assertTrue(expectedNames.contains(clazz.getName()));
      assertUnorderedContentsSame(expectedMethods.get(clazz.getName()), getMethodNames(clazz
          .getDeclaredMethods()));
    }
    for (CtClass clazz : classes) {
      // Forces code to be compiled to check for compilation errors (via
      // thrown exception)
      clazz.toBytecode();
    }
  }

  public void testClassHasFinalMethods() throws ClassNotFoundException, IOException,
      CannotCompileException {
    List<String> expectedNames = getExpectedNames(ClassHasFinalMethods.class);
    Map<String, List<String>> expectedMethods =
        getExpectedMethodsMap(expectedNames, ClassHasFinalMethods.class, new String[] {"foo",
            "foobar"});

    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(ClassHasFinalMethods.class);
    assertEquals(2, classes.size());
    for (CtClass clazz : classes) {
      assertTrue(expectedNames.contains(clazz.getName()));
      assertUnorderedContentsSame(expectedMethods.get(clazz.getName()), getMethodNames(clazz
          .getDeclaredMethods()));
    }
    for (CtClass clazz : classes) {
      // Forces code to be compiled to check for compilation errors (via
      // thrown exception)
      clazz.toBytecode();
    }
  }

  public void testClassHasNoDefaultConstructor() throws ClassNotFoundException, IOException,
      CannotCompileException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(ClassHasNoDefaultConstructor.class);
    assertEquals(2, classes.size());
    for (CtClass clazz : classes) {
      // Forces code to be compiled to check for compilation errors (via
      // thrown exception)
      clazz.toBytecode();
    }
  }

  public void testClassHasNoPublicConstructors() throws ClassNotFoundException, IOException,
      CannotCompileException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(ClassHasNoPublicConstructors.class);
    assertEquals(0, classes.size());
    for (CtClass clazz : classes) {
      // Forces code to be compiled to check for compilation errors (via
      // thrown exception)
      clazz.toBytecode();
      
    }
  }

  public void testClassHasOverloadedMethods() throws ClassNotFoundException, IOException,
      CannotCompileException {
    List<String> expectedNames = getExpectedNames(ClassHasOverloadedMethods.class);
    Map<String, List<String>> expectedMethods =
        getExpectedMethodsMap(expectedNames, ClassHasOverloadedMethods.class);

    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(ClassHasOverloadedMethods.class);
    assertEquals(2, classes.size());
    for (CtClass clazz : classes) {
      assertTrue(expectedNames.contains(clazz.getName()));
      assertUnorderedContentsSame(expectedMethods.get(clazz.getName()), getMethodNames(clazz
          .getDeclaredMethods()));
    }
    for (CtClass clazz : classes) {
      // Forces code to be compiled to check for compilation errors (via
      // thrown exception)
      clazz.toBytecode();
    }
  }

  public void testClassHasStaticMethods() throws ClassNotFoundException, IOException,
      CannotCompileException {
    List<String> expectedNames = getExpectedNames(ClassHasStaticMethods.class);
    Map<String, List<String>> expectedMethods =
        getExpectedMethodsMap(expectedNames, ClassHasStaticMethods.class,
            new String[] {"staticFoo"});

    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(ClassHasStaticMethods.class);
    assertEquals(2, classes.size());
    for (CtClass clazz : classes) {
      assertTrue(expectedNames.contains(clazz.getName()));
      assertUnorderedContentsSame(expectedMethods.get(clazz.getName()), getMethodNames(clazz
          .getDeclaredMethods()));
    }
    for (CtClass clazz : classes) {
      // Forces code to be compiled to check for compilation errors (via
      // thrown exception)
      clazz.toBytecode();
    }
  }

  public void testClassIsAnnotation() throws ClassNotFoundException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(ClassIsAnnotation.class);
    assertEquals(0, classes.size());
  }

  public void testClassIsEnum() throws ClassNotFoundException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(ClassIsEnum.class);
    assertEquals(0, classes.size());
  }

  public void testClassIsFinal() throws ClassNotFoundException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(ClassIsFinal.class);
    assertEquals(0, classes.size());
  }

  public void testClassIsInterface() throws ClassNotFoundException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(ClassIsInterface.class);
    assertEquals(0, classes.size());
  }

  public void testClassIsArray() throws ClassNotFoundException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(Object[].class);
    assertEquals(0, classes.size());
  }

  public void testClassIsNormal() throws ClassNotFoundException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(Object.class);
    assertEquals(2, classes.size());
  }

  public void testClassIsPrimitive() throws ClassNotFoundException {
    AndroidMockGenerator mockGenerator = getAndroidMockGenerator();
    List<CtClass> classes = mockGenerator.createMocksForClass(Integer.TYPE);
    assertEquals(0, classes.size());
  }
}
