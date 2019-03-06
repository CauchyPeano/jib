/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.jib.image;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link ImageReference}. */
public class ImageReferenceTest {

  private static final List<String> goodRegistries =
      Arrays.asList("some.domain---name.123.com:8080", "gcr.io", "localhost", null, "");
  private static final List<String> goodRepositories =
      Arrays.asList("some123_abc/repository__123-456/name---here", "distroless/java", "repository");
  private static final List<String> goodTags = Arrays.asList("some-.-.Tag", "", "latest", null);
  private static final List<String> goodDigests =
      Arrays.asList(
          "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", null);

  private static final List<String> badImageReferences =
      Arrays.asList(
          "",
          ":justsometag",
          "@sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "repository@sha256:a",
          "repository@notadigest",
          "Repositorywithuppercase",
          "registry:8080/Repositorywithuppercase/repository:sometag",
          "domain.name:nonnumberport/repository",
          "domain.name:nonnumberport//:no-repository");

  @Test
  public void testParse_pass() throws InvalidImageReferenceException {
    for (String goodRegistry : goodRegistries) {
      for (String goodRepository : goodRepositories) {
        for (String goodTag : goodTags) {
          verifyParse(goodRegistry, goodRepository, ":", goodTag);
        }
        for (String goodDigest : goodDigests) {
          verifyParse(goodRegistry, goodRepository, "@", goodDigest);
        }
      }
    }
  }

  @Test
  public void testParse_dockerHub_official() throws InvalidImageReferenceException {
    String imageReferenceString = "busybox";
    ImageReference imageReference = ImageReference.parse(imageReferenceString);

    Assert.assertEquals("registry.hub.docker.com", imageReference.getRegistry());
    Assert.assertEquals("library/busybox", imageReference.getRepository());
    Assert.assertEquals("latest", imageReference.getTag());
  }

  @Test
  public void testParse_dockerHub_user() throws InvalidImageReferenceException {
    String imageReferenceString = "someuser/someimage";
    ImageReference imageReference = ImageReference.parse(imageReferenceString);

    Assert.assertEquals("registry.hub.docker.com", imageReference.getRegistry());
    Assert.assertEquals("someuser/someimage", imageReference.getRepository());
    Assert.assertEquals("latest", imageReference.getTag());
  }

  @Test
  public void testParse_invalid() {
    for (String badImageReference : badImageReferences) {
      try {
        ImageReference.parse(badImageReference);
        Assert.fail(badImageReference + " should not be a valid image reference");

      } catch (InvalidImageReferenceException ex) {
        Assert.assertThat(ex.getMessage(), CoreMatchers.containsString(badImageReference));
      }
    }
  }

  @Test
  public void testOf_smoke() {
    String expectedRegistry = "someregistry";
    String expectedRepository = "somerepository";
    String expectedTag = "sometag";

    Assert.assertEquals(
        expectedRegistry,
        ImageReference.of(expectedRegistry, expectedRepository, expectedTag).getRegistry());
    Assert.assertEquals(
        expectedRepository,
        ImageReference.of(expectedRegistry, expectedRepository, expectedTag).getRepository());
    Assert.assertEquals(
        expectedTag, ImageReference.of(expectedRegistry, expectedRepository, expectedTag).getTag());
    Assert.assertEquals(
        "registry.hub.docker.com",
        ImageReference.of(null, expectedRepository, expectedTag).getRegistry());
    Assert.assertEquals(
        "registry.hub.docker.com", ImageReference.of(null, expectedRepository, null).getRegistry());
    Assert.assertEquals(
        "latest", ImageReference.of(expectedRegistry, expectedRepository, null).getTag());
    Assert.assertEquals("latest", ImageReference.of(null, expectedRepository, null).getTag());
    Assert.assertEquals(
        expectedRepository, ImageReference.of(null, expectedRepository, null).getRepository());
  }

  @Test
  public void testToString() throws InvalidImageReferenceException {
    Assert.assertEquals("someimage", ImageReference.of(null, "someimage", null).toString());
    Assert.assertEquals("someimage", ImageReference.of("", "someimage", "").toString());
    Assert.assertEquals(
        "someotherimage", ImageReference.of(null, "library/someotherimage", null).toString());
    Assert.assertEquals(
        "someregistry/someotherimage",
        ImageReference.of("someregistry", "someotherimage", null).toString());
    Assert.assertEquals(
        "anotherregistry/anotherimage:sometag",
        ImageReference.of("anotherregistry", "anotherimage", "sometag").toString());

    Assert.assertEquals(
        "someimage@sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        ImageReference.of(
                null,
                "someimage",
                "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
            .toString());
    Assert.assertEquals(
        "gcr.io/distroless/java@sha256:b430543bea1d8326e767058bdab3a2482ea45f59d7af5c5c61334cd29ede88a1",
        ImageReference.parse(
                "gcr.io/distroless/java@sha256:b430543bea1d8326e767058bdab3a2482ea45f59d7af5c5c61334cd29ede88a1")
            .toString());
  }

  @Test
  public void testToStringWithTag() {
    Assert.assertEquals(
        "someimage:latest", ImageReference.of(null, "someimage", null).toStringWithTag());
    Assert.assertEquals(
        "someimage:latest", ImageReference.of("", "someimage", "").toStringWithTag());
    Assert.assertEquals(
        "someotherimage:latest",
        ImageReference.of(null, "library/someotherimage", null).toStringWithTag());
    Assert.assertEquals(
        "someregistry/someotherimage:latest",
        ImageReference.of("someregistry", "someotherimage", null).toStringWithTag());
    Assert.assertEquals(
        "anotherregistry/anotherimage:sometag",
        ImageReference.of("anotherregistry", "anotherimage", "sometag").toStringWithTag());
  }

  @Test
  public void testIsTagDigest() throws InvalidImageReferenceException {
    Assert.assertFalse(ImageReference.of(null, "someimage", null).isTagDigest());
    Assert.assertFalse(ImageReference.of(null, "someimage", "latest").isTagDigest());
    Assert.assertTrue(
        ImageReference.of(
                null,
                "someimage",
                "sha256:b430543bea1d8326e767058bdab3a2482ea45f59d7af5c5c61334cd29ede88a1")
            .isTagDigest());
    Assert.assertTrue(
        ImageReference.parse(
                "someimage@sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
            .isTagDigest());
  }

  private void verifyParse(String registry, String repository, String tagSeparator, String tag)
      throws InvalidImageReferenceException {
    // Gets the expected parsed components.
    String expectedRegistry = registry;
    if (Strings.isNullOrEmpty(expectedRegistry)) {
      expectedRegistry = "registry.hub.docker.com";
    }
    String expectedRepository = repository;
    if ("registry.hub.docker.com".equals(expectedRegistry) && repository.indexOf('/') < 0) {
      expectedRepository = "library/" + expectedRepository;
    }
    String expectedTag = tag;
    if (Strings.isNullOrEmpty(expectedTag)) {
      expectedTag = "latest";
    }

    // Builds the image reference to parse.
    StringBuilder imageReferenceBuilder = new StringBuilder();
    if (!Strings.isNullOrEmpty(registry)) {
      imageReferenceBuilder.append(registry).append('/');
    }
    imageReferenceBuilder.append(repository);
    if (!Strings.isNullOrEmpty(tag)) {
      imageReferenceBuilder.append(tagSeparator).append(tag);
    }

    ImageReference imageReference = ImageReference.parse(imageReferenceBuilder.toString());

    Assert.assertEquals(expectedRegistry, imageReference.getRegistry());
    Assert.assertEquals(expectedRepository, imageReference.getRepository());
    Assert.assertEquals(expectedTag, imageReference.getTag());
  }

  @Test
  public void testIsValidTag_null() {
    Assert.assertFalse(ImageReference.isValidTag(null));
  }
}
