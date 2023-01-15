package tech.hiddenproject.hic;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author Danila Rassokhin
 */
public class AnnotationProcessor {

  /**
   * Searches for annotation on class.
   *
   * @param clazz          Class to search in
   * @param annotationType Annotation class to search
   * @param <A>            Annotation to search
   * @return Annotation or null
   */
  @SuppressWarnings("unchecked")
  public static <A extends Annotation> A findAnnotation(Method clazz, Class<A> annotationType) {
    Annotation[] anns = clazz.getDeclaredAnnotations();
    for (Annotation ann : anns) {
      if (ann.annotationType() == annotationType) {
        return (A) ann;
      }
    }
    for (Annotation ann : anns) {
      A annotation = findAnnotation(ann.annotationType(), annotationType);
      if (annotation != null) {
        return annotation;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
    if (clazz.equals(Target.class)
        || clazz.equals(Documented.class)
        || clazz.equals(Retention.class)
        || clazz.equals(Inherited.class)
        || clazz.equals(Deprecated.class)) {
      return null;
    }
    Annotation[] anns = clazz.getDeclaredAnnotations();
    for (Annotation ann : anns) {
      if (ann.annotationType() == annotationType) {
        return (A) ann;
      }
    }
    for (Annotation ann : anns) {
      A annotation = findAnnotation(ann.annotationType(), annotationType);
      if (annotation != null) {
        return annotation;
      }
    }
    for (Class<?> ifc : clazz.getInterfaces()) {
      A annotation = findAnnotation(ifc, annotationType);
      if (annotation != null) {
        return annotation;
      }
    }
    Class<?> superclass = clazz.getSuperclass();
    if (superclass == null || Object.class == superclass) {
      return null;
    }
    return findAnnotation(superclass, annotationType);
  }

  public static <T extends Annotation> Map<T, Object> extractMethodParametersWithAnnotation(
      Method method,
      Class<T> annotationClass,
      Object[] args) {
    Annotation[][] annotations = method.getParameterAnnotations();
    return IntStream.range(0, annotations.length)
        .filter(parameterIndex -> containsAnnotation(annotations[parameterIndex], annotationClass))
        .collect(HashMap::new,
                 (map, value) -> map.put(
                     findAnnotationIn(annotations[value], annotationClass),
                     args[value]
                 ), HashMap::putAll
        );
  }

  public static boolean containsAnnotation(Annotation[] annotations,
                                           Class<? extends Annotation> annotationClass) {
    return Arrays.stream(annotations)
        .anyMatch(annotation -> annotation.annotationType().equals(annotationClass));
  }

  public static <T extends Annotation> T findAnnotationIn(Annotation[] annotations,
                                                          Class<T> annotationClass) {
    return Arrays.stream(annotations)
        .filter(annotation -> annotation.annotationType().equals(annotationClass))
        .map(annotation -> (T) annotation)
        .findFirst().orElse(null);
  }

  public static <T extends Annotation> T extractMethodAnnotation(Method method,
                                                                 Class<T> annotationClasses) {
    return Arrays.stream(method.getDeclaredAnnotations())
        .filter(annotation -> annotation.annotationType().equals(annotationClasses))
        .map(annotation -> (T) annotation)
        .findFirst().orElse(null);
  }
}
