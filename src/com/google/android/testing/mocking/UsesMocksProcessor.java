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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

/**
 * Annotation Processor to generate the mocks for Android Mock.
 * 
 * This processor will automatically create mocks for all classes
 * specified by {@link UsesMocks} annotations.
 * 
 * @author swoodward@google.com (Stephen Woodward)
 */
@SupportedAnnotationTypes("com.google.android.testing.mocking.UsesMocks")
@SupportedSourceVersion(SourceVersion.RELEASE_5)
public class UsesMocksProcessor extends AbstractProcessor {
  private AndroidMockGenerator mockGenerator = new AndroidMockGenerator();
  private List<Class<?>> classesToMock = new ArrayList<Class<?>>();
  
  @Override
  public boolean process(Set<? extends TypeElement> annotations,
      RoundEnvironment environment) {
    this.processingEnv.getMessager().printMessage(Kind.NOTE, "Start Processing Annotations");
    classesToMock.addAll(findClassesToMock(
        environment.getElementsAnnotatedWith(UsesMocks.class)));
    if (environment.processingOver()) {
      this.processingEnv.getMessager().printMessage(Kind.NOTE,
          "Found " + classesToMock.size() + " classes to mock");
      Set<CtClass> mockedClassesSet = getClassMocks(classesToMock);
      this.processingEnv.getMessager().printMessage(Kind.NOTE,
          "Found " + mockedClassesSet.size() + " mocked classes to save");
      writeMocks(mockedClassesSet);
    }
    return false;
  }

  List<Class<?>> findClassesToMock(Set<? extends Element> annotatedElements) {
    List<Class<?>> classList = new ArrayList<Class<?>>();
    for (Element annotation : annotatedElements) {
      List<? extends AnnotationMirror> mirrors = annotation.getAnnotationMirrors();
      for (AnnotationMirror mirror : mirrors) {
        if (mirror.getAnnotationType().toString().equals(UsesMocks.class.getName())) {
          for (AnnotationValue annotationValue : mirror.getElementValues().values()) {
            for (Object classFileName : (Iterable<?>) annotationValue.getValue()) {
              String className = classFileName.toString().substring(
                  0, classFileName.toString().length() - 6);
              this.processingEnv.getMessager().printMessage(Kind.NOTE,
                  "Adding Class to Mocking List: " + className);
              try {
                classList.add(Class.forName(className, false,
                    this.getClass().getClassLoader()));
              } catch (ClassNotFoundException e) {
                reportClasspathError(className);
              }
            }
          }
        }
      }
    }
    return classList;
  }

  Set<CtClass> getClassMocks(List<Class<?>> classesToMock) {
    Set<CtClass> mockedClassesSet = new HashSet<CtClass>();
    for (Class<?> clazz : classesToMock) {
      try {
        this.processingEnv.getMessager().printMessage(Kind.NOTE, "Mocking " + clazz);
        mockedClassesSet.addAll(getAndroidMockGenerator().createMocksForClass(clazz));
      } catch (ClassNotFoundException e) {
        reportClasspathError(clazz.getName());
      }
    }
    return mockedClassesSet;
  }

  private void reportClasspathError(String clazz) {
    this.processingEnv.getMessager().printMessage(Kind.ERROR,
        "Could not find " + clazz);
    this.processingEnv.getMessager().printMessage(Kind.NOTE, "Known Classpath: ");
    URL[] allUrls = ((URLClassLoader) this.getClass().getClassLoader()).getURLs();
    for (URL url : allUrls) {
      this.processingEnv.getMessager().printMessage(Kind.NOTE, url.toString());
    }
  }

  void writeMocks(Set<CtClass> mockedClassesSet) {
    for (CtClass clazz : mockedClassesSet) {
      OutputStream classFileStream;
      try {
        this.processingEnv.getMessager().printMessage(Kind.NOTE, "Saving " + clazz.getName());
        JavaFileObject classFile = this.processingEnv.getFiler().createClassFile(clazz.getName());
        classFileStream = classFile.openOutputStream();
        classFileStream.write(clazz.toBytecode());
        classFileStream.close();
      } catch (IOException e) {
        this.processingEnv.getMessager().printMessage(Kind.ERROR,
            "Internal Error saving mock: " + clazz.getName());
        this.processingEnv.getMessager().printMessage(Kind.ERROR, e.toString());
      } catch (CannotCompileException e) {
        this.processingEnv.getMessager().printMessage(Kind.ERROR,
            "Internal Error converting mock to .class file: " + clazz.getName());
        this.processingEnv.getMessager().printMessage(Kind.ERROR, e.getReason());
      }
    }
  }

  private AndroidMockGenerator getAndroidMockGenerator() {
    return mockGenerator;
  }
}
