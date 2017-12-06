/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.crepecake.image;

import com.google.cloud.tools.crepecake.blob.BlobDescriptor;

/**
 * A {@link Layer} reference that <b>does not</b> have the underlying content. It references the
 * layer with its digest and size, but <b>not</b> its diff ID.
 */
class ReferenceNoDiffIdLayerDataProvider extends LayerDataProvider {

  /** The {@link BlobDescriptor} of the compressed layer content. */
  private final BlobDescriptor blobDescriptor;

  /** Instantiate with a {@link BlobDescriptor} and diff ID. */
  ReferenceNoDiffIdLayerDataProvider(BlobDescriptor blobDescriptor) {
    this.blobDescriptor = blobDescriptor;
  }

  @Override
  BlobDescriptor getBlobDescriptor() {
    return blobDescriptor;
  }

  @Override
  DescriptorDigest getDiffId() throws LayerException {
    throw new LayerException("Diff ID not available for reference layer without diff ID");
  }
}
