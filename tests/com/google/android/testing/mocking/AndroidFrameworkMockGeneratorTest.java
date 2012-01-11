/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.testing.mocking;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.jar.JarEntry;

/**
 * @author swoodward@google.com (Stephen Woodward)
 */
public class AndroidFrameworkMockGeneratorTest extends TestCase {
  private void cleanupGeneratedClasses(CtClass... classes) {
    for (CtClass clazz : classes) {
      clazz.detach();
    }
  }

  private Collection<JarEntry> getMockJarEntries() {
    JarEntry firstEntry = new JarEntry("java/lang/Object.class");
    JarEntry secondEntry = new JarEntry(
        "com/google/android/testing/mocking/AndroidFrameworkMockGeneratorTest$Inner.class");
    List<JarEntry> entryList = new ArrayList<JarEntry>();
    entryList.add(firstEntry);
    entryList.add(secondEntry);
    return entryList;
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

  private List<String> getClassNames(List<GeneratedClassFile> classes) {
    List<String> classNames = new ArrayList<String>();
    for (GeneratedClassFile clazz : classes) {
      classNames.add(clazz.getClassName());
    }
    return classNames;
  }

  private AndroidFrameworkMockGenerator getMockGenerator() {
    return new AndroidFrameworkMockGenerator();
  }

  public void testCreateMockForClass() throws ClassNotFoundException, IOException,
      CannotCompileException {
    AndroidFrameworkMockGenerator mockGenerator = getMockGenerator();
    List<GeneratedClassFile> classes = mockGenerator.createMocksForClass(Object.class);

    List<String> expectedNames = new ArrayList<String>();
    expectedNames.addAll(Arrays.asList(new String[] {
        "genmocks.java.lang.ObjectDelegateSubclass",
        "genmocks.java.lang.ObjectDelegateInterface"}));
    List<String> actualNames = getClassNames(classes);
    assertUnorderedContentsSame(expectedNames, actualNames);
  }

  public void testGetClassList() throws ClassNotFoundException {
    Collection<JarEntry> jarEntries = getMockJarEntries();
    List<String> expectedClassNames =
        new ArrayList<String>(Arrays.asList(new String[] {
            "java.lang.Object",
            "com.google.android.testing.mocking.AndroidFrameworkMockGeneratorTest$Inner"}));
    List<Class<?>> list = getMockGenerator().getClassList(jarEntries);
    assertEquals(expectedClassNames.size(), list.size());
    for (Class<?> clazz : list) {
      assertTrue(clazz.getName(), expectedClassNames.contains(clazz.getName()));
    }
  }

  public void testIsClassFile() {
    assertTrue(getMockGenerator().jarEntryIsClassFile(new JarEntry("something.class")));
    assertTrue(getMockGenerator().jarEntryIsClassFile(new JarEntry("/Foo/Bar.class")));
    assertFalse(getMockGenerator().jarEntryIsClassFile(new JarEntry("/Foo/Bar.clas")));
    assertFalse(getMockGenerator().jarEntryIsClassFile(new JarEntry("/Foo/Bar.class ")));
    assertFalse(getMockGenerator().jarEntryIsClassFile(new JarEntry("/Foo/Bar")));
  }

  public void testGetJarFileNameForVersion() {
    getMockGenerator();
    assertEquals("some_folder/platforms/android-10/android.jar",
        AndroidFrameworkMockGenerator.getJarFileNameForVersion(10, "some_folder")
            .replace('\\', '/'));
  }

  public void testGetMocksForClass() throws ClassNotFoundException, IOException,
      CannotCompileException, NotFoundException {
    List<CtClass> createdClasses = new ArrayList<CtClass>();
    AndroidFrameworkMockGenerator mockGenerator = getMockGenerator();
    List<GeneratedClassFile> createdMocks = mockGenerator.createMocksForClass(
        Hashtable.class);
    for (GeneratedClassFile mock : createdMocks) {
      CtClass ctClass = ClassPool.getDefault().get(mock.getClassName());
      createdClasses.add(ctClass);
      ctClass.toClass();
    }
    List<GeneratedClassFile> mocks = mockGenerator.getMocksForClass(Hashtable.class);
    String[] expectedClassNames = new String[] {
        "genmocks.java.util.HashtableDelegateSubclass",
        "genmocks.java.util.HashtableDelegateInterface",
    };
    assertEquals(expectedClassNames.length, mocks.size());
    for (int i = 0; i < mocks.size(); ++i) {
      assertEquals(expectedClassNames[i], mocks.get(i).getClassName());
    }
    cleanupGeneratedClasses(createdClasses.toArray(new CtClass[0]));
  }

  class Inner {
  }
}
