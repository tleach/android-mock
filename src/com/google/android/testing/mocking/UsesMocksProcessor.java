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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
  private OutputStream logFile;
  private static final String BIN_DIR = "bin_dir";
  
  private void printMessage(Kind kind, String message) {
    processingEnv.getMessager().printMessage(kind, message);
    if (logFile != null) {
      try {
        logFile.write((SimpleDateFormat.getDateTimeInstance().format(new Date()) + " - "
            + kind.toString() + " : " + message + "\n").getBytes());
      } catch (IOException e) {
        // That's unfortunate, but not much to do about it.
        processingEnv.getMessager().printMessage(Kind.WARNING, "IOException logging to file" +
            e.toString());
      }
    }
  }
  
  private void printMessage(Kind kind, Exception e) {
    ByteArrayOutputStream stackTraceByteStream = new ByteArrayOutputStream();
    PrintStream stackTraceStream = new PrintStream(stackTraceByteStream);
    e.printStackTrace(stackTraceStream);
    printMessage(kind, stackTraceByteStream.toString());
  }
  
  FileOutputStream openLogFile(String logFileName) {
    try {
      if (logFileName != null) {
        File log = new File(logFileName);
        if (!log.exists() && log.getParentFile() != null) {
          log.getParentFile().mkdirs();
        }
        return new FileOutputStream(log, true);
      }
    } catch (FileNotFoundException e) {
      printMessage(Kind.WARNING, e);
    }
    return null;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations,
      RoundEnvironment environment) {
    try {
      logFile = openLogFile(processingEnv.getOptions().get("logfile"));
      printMessage(Kind.NOTE, "Start Processing Annotations");
      List<Class<?>> classesToMock = new ArrayList<Class<?>>();
      classesToMock.addAll(findClassesToMock(environment.getElementsAnnotatedWith(
          UsesMocks.class)));
      printMessage(Kind.NOTE, "Found " + classesToMock.size() + " classes to mock");
      Set<CtClass> mockedClassesSet = getClassMocks(classesToMock);
      printMessage(Kind.NOTE, "Found " + mockedClassesSet.size() + " mocked classes to save");
      writeMocks(mockedClassesSet);
      printMessage(Kind.NOTE, "Finished Processing Mocks");
    } catch (Exception e) {
      printMessage(Kind.ERROR, e);
    } finally {
      if (logFile != null) {
        try {
          logFile.close();
        } catch (IOException e) {
          // That's ok
        }
      }
    }
    return false;
  }

  List<Class<?>> findClassesToMock(Set<? extends Element> annotatedElements) {
    printMessage(Kind.NOTE, "Processing " + annotatedElements);
    List<Class<?>> classList = new ArrayList<Class<?>>();
    for (Element annotation : annotatedElements) {
      List<? extends AnnotationMirror> mirrors = annotation.getAnnotationMirrors();
      for (AnnotationMirror mirror : mirrors) {
        if (mirror.getAnnotationType().toString().equals(UsesMocks.class.getName())) {
          for (AnnotationValue annotationValue : mirror.getElementValues().values()) {
            for (Object classFileName : (Iterable<?>) annotationValue.getValue()) {
              String className = classFileName.toString();
              if (className.endsWith(".class")) {
                className = className.substring(0, className.length() - 6);
              }
              printMessage(Kind.NOTE, "Adding Class to Mocking List: " + className);
              try {
                classList.add(Class.forName(className, false, getClass().getClassLoader()));
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
        printMessage(Kind.NOTE, "Mocking " + clazz);
        mockedClassesSet.addAll(getAndroidMockGenerator().createMocksForClass(clazz));
      } catch (ClassNotFoundException e) {
        reportClasspathError(clazz.getName());
      }
    }
    return mockedClassesSet;
  }

  private void reportClasspathError(String clazz) {
    printMessage(Kind.ERROR, "Could not find " + clazz);
    printMessage(Kind.NOTE, "Known Classpath: ");
    URL[] allUrls = ((URLClassLoader) getClass().getClassLoader()).getURLs();
    for (URL url : allUrls) {
      printMessage(Kind.NOTE, url.toString());
    }
  }

  void writeMocks(Set<CtClass> mockedClassesSet) {
    for (CtClass clazz : mockedClassesSet) {
      OutputStream classFileStream;
      byte[] classBytes = null;
      try {
        printMessage(Kind.NOTE, "Saving " + clazz.getName());
        classBytes = clazz.toBytecode();
        JavaFileObject classFile = processingEnv.getFiler().createClassFile(clazz.getName());
        classFileStream = classFile.openOutputStream();
        classFileStream.write(classBytes);
        classFileStream.close();
      } catch (IOException e) {
        printMessage(Kind.ERROR, "Internal Error saving mock: " + clazz.getName());
        printMessage(Kind.ERROR, e);
      } catch (CannotCompileException e) {
        printMessage(Kind.ERROR,
            "Internal Error converting mock to .class file: " + clazz.getName());
        printMessage(Kind.ERROR, e);
      } catch (UnsupportedOperationException e) {
        // Eclipse annotation processing doesn't support class creation.
        printMessage(Kind.NOTE, "Saving via Eclipse " + clazz.getName());
        saveMocksEclipse(clazz, classBytes);
      }
    }
  }

  private void saveMocksEclipse(CtClass clazz, byte[] classBytes) {
    File targetFile = null;
    File classFolder = new File(processingEnv.getOptions().get(BIN_DIR).toString().trim());
    targetFile = new File(classFolder, getFilenameForClass(clazz));
    targetFile.getParentFile().mkdirs();
    try {
      FileOutputStream outputStream = new FileOutputStream(targetFile);
      outputStream.write(classBytes);
      outputStream.close();
    } catch (FileNotFoundException e) {
      printMessage(Kind.ERROR, e);
    } catch (IOException e) {
      printMessage(Kind.ERROR, e);
    }
  }
  
  String getFilenameForClass(CtClass clazz) {
    return clazz.getName().replace('.', File.separatorChar) + ".class";
  }

  private AndroidMockGenerator getAndroidMockGenerator() {
    return mockGenerator;
  }
}
