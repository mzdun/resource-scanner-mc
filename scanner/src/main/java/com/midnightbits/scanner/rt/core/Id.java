package com.midnightbits.scanner.rt.core;

import java.util.function.UnaryOperator;

public class Id {
   public static final char NAMESPACE_SEPARATOR = ':';
   public static final String DEFAULT_NAMESPACE = "minecraft";
   public static final String REALMS_NAMESPACE = "realms";
   private final String namespace;
   private final String path;

   private Id(String namespace, String path) {
      assert isNamespaceValid(namespace);
      assert isPathValid(path);
      this.namespace = namespace;
      this.path = path;
   }

   private static Id ofValidated(String namespace, String path) {
      return new Id(validateNamespace(namespace, path), validatePath(namespace, path));
   }

   public static Id of(String namespace, String path) {
      return ofValidated(namespace, path);
   }

   public static Id of(String id) {
      return splitOn(id, NAMESPACE_SEPARATOR);
   }

   public static Id ofVanilla(String path) {
      return new Id(DEFAULT_NAMESPACE, validatePath(DEFAULT_NAMESPACE, path));
   }

   public static Id tryParse(String id) {
      return trySplitOn(id, NAMESPACE_SEPARATOR);
   }

   public static Id tryParse(String namespace, String path) {
      return isNamespaceValid(namespace) && isPathValid(path) ? new Id(namespace, path) : null;
   }

   public static Id splitOn(String id, char delimiter) {
      int i = id.indexOf(delimiter);
      if (i >= 0) {
         String string = id.substring(i + 1);
         if (i != 0) {
            String string2 = id.substring(0, i);
            return ofValidated(string2, string);
         } else {
            return ofVanilla(string);
         }
      } else {
         return ofVanilla(id);
      }
   }

   public static Id trySplitOn(String id, char delimiter) {
      int i = id.indexOf(delimiter);
      if (i >= 0) {
         String string = id.substring(i + 1);
         if (!isPathValid(string)) {
            return null;
         } else if (i != 0) {
            String string2 = id.substring(0, i);
            return isNamespaceValid(string2) ? new Id(string2, string) : null;
         } else {
            return new Id(DEFAULT_NAMESPACE, string);
         }
      } else {
         return isPathValid(id) ? new Id(DEFAULT_NAMESPACE, id) : null;
      }
   }

   public String getPath() {
      return this.path;
   }

   public String getNamespace() {
      return this.namespace;
   }

   public Id withPath(String path) {
      return new Id(this.namespace, validatePath(this.namespace, path));
   }

   public Id withPath(UnaryOperator<String> pathFunction) {
      return this.withPath((String) pathFunction.apply(this.path));
   }

   public Id withPrefixedPath(String prefix) {
      return this.withPath(prefix + this.path);
   }

   public Id withSuffixedPath(String suffix) {
      return this.withPath(this.path + suffix);
   }

   public String toString() {
      return this.namespace + ":" + this.path;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Id identifier)) {
         return false;
      } else {
         return this.namespace.equals(identifier.namespace) && this.path.equals(identifier.path);
      }
   }

   public int hashCode() {
      return 31 * this.namespace.hashCode() + this.path.hashCode();
   }

   public int compareTo(Id identifier) {
      int i = this.path.compareTo(identifier.path);
      if (i == 0) {
         i = this.namespace.compareTo(identifier.namespace);
      }

      return i;
   }

   public String toUnderscoreSeparatedString() {
      return this.toString().replace('/', '_').replace(NAMESPACE_SEPARATOR, '_');
   }

   public String toTranslationKey() {
      return this.namespace + "." + this.path;
   }

   public String toShortTranslationKey() {
      return this.namespace.equals(DEFAULT_NAMESPACE) ? this.path : this.toTranslationKey();
   }

   public String toTranslationKey(String prefix) {
      return prefix + "." + this.toTranslationKey();
   }

   public String toTranslationKey(String prefix, String suffix) {
      return prefix + "." + this.toTranslationKey() + "." + suffix;
   }

   public static boolean isCharValid(char c) {
      return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' || c == NAMESPACE_SEPARATOR || c == '/'
            || c == '.' || c == '-';
   }

   public static boolean isPathValid(String path) {
      for (int i = 0; i < path.length(); ++i) {
         if (!isPathCharacterValid(path.charAt(i))) {
            return false;
         }
      }

      return true;
   }

   public static boolean isNamespaceValid(String namespace) {
      for (int i = 0; i < namespace.length(); ++i) {
         if (!isNamespaceCharacterValid(namespace.charAt(i))) {
            return false;
         }
      }

      return true;
   }

   private static String validateNamespace(String namespace, String path) {
      if (!isNamespaceValid(namespace)) {
         throw new InvalidIdentifierException(
               "Non [a-z0-9_.-] character in namespace of location: " + namespace + ":" + path);
      } else {
         return namespace;
      }
   }

   public static boolean isPathCharacterValid(char character) {
      return character == '_' || character == '-' || character >= 'a' && character <= 'z'
            || character >= '0' && character <= '9' || character == '/' || character == '.';
   }

   private static boolean isNamespaceCharacterValid(char character) {
      return character == '_' || character == '-' || character >= 'a' && character <= 'z'
            || character >= '0' && character <= '9' || character == '.';
   }

   private static String validatePath(String namespace, String path) {
      if (!isPathValid(path)) {
         throw new InvalidIdentifierException(
               "Non [a-z0-9/._-] character in path of location: " + namespace + ":" + path);
      } else {
         return path;
      }
   }
}
