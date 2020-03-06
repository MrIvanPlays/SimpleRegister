package com.mrivanplays.simpleregister.dependency;

import com.mrivanplays.simpleregister.dependency.relocation.Relocation;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Credits: lucko Source: github.com/lucko/LuckPerms
 *
 * @author lucko
 */
public enum Dependency {
  ASM("org.ow2.asm", "asm", "7.1", "SrL6K20sycyx6qBeoynEB7R7E+0pFfYvjEuMyWJY1N4="),
  ASM_COMMONS("org.ow2.asm", "asm-commons", "7.1", "5VkEidjxmE2Fv+q9Oxc3TFnCiuCdSOxKDrvQGVns01g="),
  JAR_RELOCATOR("me.lucko", "jar-relocator", "1.4", "1RsiF3BiVztjlfTA+svDCuoDSGFuSpTZYHvUK8yBx8I="),
  HIKARI(
      "com{}zaxxer",
      "HikariCP",
      "3.4.1",
      "uCbLTp8iz699ZJS3TxSAf4j9UfrikmgxvTHT0+N/Bck=",
      Relocation.of("hikari", "com{}zaxxer{}hikari")),
  H2_DRIVER(
      "com.h2database",
      "h2",
      // seems to be a compat bug in 1.4.200 with older dbs
      // see: https://github.com/h2database/h2database/issues/2078
      "1.4.199",
      "MSWhZ0O8a0z7thq7p4MgPx+2gjCqD9yXiY95b5ml1C4="
      // we don't apply relocations to h2 - it gets loaded via
      // an isolated classloader
      ),
  SQLITE_DRIVER(
      "org.xerial", "sqlite-jdbc", "3.28.0", "k3hOVtv1RiXgbJks+D9w6cG93Vxq0dPwEwjIex2WG2A="
      // we don't apply relocations to sqlite - it gets loaded via
      // an isolated classloader
      ),
  MARIADB_DRIVER(
      "org{}mariadb{}jdbc",
      "mariadb-java-client",
      "2.5.1",
      "/AxG0o0JnIme7hnDTO2WEUxgF1yXPiWPhMKermXAzZE=",
      Relocation.of("mariadb", "org{}mariadb{}jdbc")),
  MYSQL_DRIVER(
      "mysql",
      "mysql-connector-java",
      "5.1.48",
      "VuJsqqOCH1rkr0T5x09mz4uE6gFRatOAPLsOkEm27Kg=",
      Relocation.of("mysql", "com{}mysql")),
  POSTGRESQL_DRIVER(
      "org{}postgresql",
      "postgresql",
      "9.4.1212",
      "DLKhWL4xrPIY4KThjI89usaKO8NIBkaHc/xECUsMNl0=",
      Relocation.of("postgresql", "org{}postgresql")),
  SLF4J_SIMPLE(
      "org.slf4j", "slf4j-simple", "1.7.28", "YO863GwYR8RuGr16gGIlWqPizh2ywI37H9Q/GkYgdzY="),
  SLF4J_API("org.slf4j", "slf4j-api", "1.7.28", "+25PZ6KkaJ4+cTWE2xel0QkMHr5u7DDp4DSabuEYFB4=");

  private final List<URL> urls;
  private final String version;
  private final byte[] checksum;
  private final List<Relocation> relocations;

  private static final String MAVEN_CENTRAL_REPO = "https://repo.maven.apache.org/maven2/";
  private static final String MAVEN_FORMAT = "%s/%s/%s/%s-%s.jar";

  Dependency(String groupId, String artifactId, String version, String checksum) {
    this(groupId, artifactId, version, checksum, MAVEN_CENTRAL_REPO, new Relocation[0]);
  }

  Dependency(
      String groupId,
      String artifactId,
      String version,
      String checksum,
      Relocation... relocations) {
    this(groupId, artifactId, version, checksum, MAVEN_CENTRAL_REPO, relocations);
  }

  Dependency(String groupId, String artifactId, String version, String repoUrl, String checksum) {
    this(groupId, artifactId, version, checksum, repoUrl, new Relocation[0]);
  }

  Dependency(
      String groupId,
      String artifactId,
      String version,
      String checksum,
      String repoUrl,
      Relocation... relocations) {
    String path =
        String.format(
            MAVEN_FORMAT,
            rewriteEscaping(groupId).replace(".", "/"),
            rewriteEscaping(artifactId),
            version,
            rewriteEscaping(artifactId),
            version);
    try {
      URL url;
      if (repoUrl.endsWith(".jar")) {
        url = new URL(repoUrl);
      } else {
        url = new URL(repoUrl + path);
      }
      this.urls = Collections.singletonList(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    this.version = version;
    this.checksum = Base64.getDecoder().decode(checksum);
    this.relocations = Arrays.asList(relocations);
  }

  private static String rewriteEscaping(String s) {
    return s.replace("{}", ".");
  }

  public String getFileName() {
    return name().toLowerCase().replace('_', '-') + "-" + this.version;
  }

  /*
  public static void main(String[] args) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    for (Dependency dependency : values()) {
      List<byte[]> hashes = new ArrayList<>();
      for (URL url : dependency.getUrls()) {
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "networkbans");
        try (InputStream in = connection.getInputStream()) {
          byte[] bytes = readAllBytes(in);
          if (bytes.length == 0) {
            throw new RuntimeException("Empty stream");
          }
          hashes.add(digest.digest(bytes));
        }
      }
      for (int i = 0; i < hashes.size(); i++) {
        byte[] hash = hashes.get(i);
        if (!Arrays.equals(hash, dependency.getChecksum())) {
          System.out.println(
              "NO MATCH - REPO "
                  + i
                  + " - "
                  + dependency.name()
                  + ": "
                  + Base64.getEncoder().encodeToString(hash));
        }
      }
    }
  }
   */

  private static byte[] readAllBytes(InputStream in) throws IOException {
    List<byte[]> bufs = null;
    byte[] result = null;
    int total = 0;
    int remaining = Integer.MAX_VALUE;
    int n;
    do {
      byte[] buf = new byte[Math.min(remaining, 8192)];
      int nread = 0;

      while ((n = in.read(buf, nread, Math.min(buf.length - nread, remaining))) > 0) {
        nread += n;
        remaining -= n;
      }

      if (nread > 0) {
        if ((Integer.MAX_VALUE - 8) - total < nread) {
          throw new OutOfMemoryError("Required array size too large");
        }
        total += nread;
        if (result == null) {
          result = buf;
        } else {
          if (bufs == null) {
            bufs = new ArrayList<>();
            bufs.add(result);
          }
          bufs.add(buf);
        }
      }
    } while (n >= 0 && remaining > 0);

    if (bufs == null) {
      if (result == null) {
        return new byte[0];
      }
      return result.length == total ? result : Arrays.copyOf(result, total);
    }

    result = new byte[total];
    int offset = 0;
    remaining = total;
    for (byte[] b : bufs) {
      int count = Math.min(b.length, remaining);
      System.arraycopy(b, 0, result, offset, count);
      offset += count;
      remaining -= count;
    }

    return result;
  }

  public List<URL> getUrls() {
    return this.urls;
  }

  public String getVersion() {
    return this.version;
  }

  public byte[] getChecksum() {
    return this.checksum;
  }

  public List<Relocation> getRelocations() {
    return this.relocations;
  }
}
