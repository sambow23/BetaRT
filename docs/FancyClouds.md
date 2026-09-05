# Fancy cloud exterior meshes

Fancy clouds are generated from the selected cloud texture's alpha mask. A texel
is occupied at alpha >= 3/255, matching the previous 2/255 test after the old 0.8
vertex-alpha multiplier. That cutoff is used once on the CPU; the resulting
material has no GPU alpha test or blending. Texture color and optional PBR maps
remain in use. Partial transparency is intentionally replaced by opaque geometry.

Only exposed faces are emitted. Adjacent top/bottom faces and boundary strips are
merged, interior slices and the duplicate top cap are removed, and holes remain
open. The original 576-by-576-block moving patch, 4-block thickness, 12-block
vanilla texel scale, texture scrolling, height and directional vertex tints are
preserved. Other texture resolutions use the same world-space repeat period.
The patch is capped at its outer boundary where occupied cells meet its edge.

Fancy geometry is stored in local coordinates and moved with an instance
transform. It is reused while drifting, changing height or rebasing the world
origin; a changed texture phase or tint rebuilds it. The API mesh hash stays
`0x434c800000000000`. Empty patches are cached too. Fast clouds retain their old
geometry/material and use the separate stable hash `0x434c000000000000`.

Cloud input is published with the completed game frame, like particles and
entities. Mesh replacement only happens during render-frame preparation, after
the preceding submission has finished using that handle. Deferred destruction is
retained for world clears and other changes during submission. A constant hash
alone would be unsafe because Remix derives its API handle directly from it and
ignores duplicate live registrations.

## Texture support

The mask comes from the exact file selected for the cloud material, including
texture-pack overrides. Supported inputs are PNG, RGB(A) DDS with 16/24/32-bit
pixels, and BC1/BC2/BC3 DDS, including the corresponding DX10 formats. Row pitch,
texture wrapping and negative coordinates are supported. Textures are limited
to 4096 pixels per dimension and files to 128 MiB. Unsupported or malformed
textures log `Fancy cloud shell unavailable` and skip fancy geometry rather than
submitting a solid sky-sized rectangle; the fast-cloud path remains available.
For unsupported compressed DDS formats such as BC7, supply a supported cloud
texture and make sure it is the one selected by the normal asset resolver.

PNG decoding uses the existing stb_image header in the Remix checkout's
FidelityFX-SDK submodule. No additional shared library is needed at runtime.

## Verification

Build the bridge with `scripts-linux/build-native.sh`. Native tests use a
separate build directory:

```sh
cmake -S . -B /tmp/betart-cloud-tests -G Ninja \
  -DMCRTX_BUILD_TESTS=ON -DMCRTX_BUILD_JNI=ON \
  -DMCRTX_DXVK_REMIX_ROOT=/path/to/dxvk-remix-gmod
cmake --build /tmp/betart-cloud-tests --parallel
ctest --test-dir /tmp/betart-cloud-tests --output-on-failure
```

`mcrtx_cloud_geometry_tests` checks decoder errors, alpha formats, exposed faces,
holes, merged surfaces, winding, bounds and wrapping. Optional command-line
texture paths additionally test real installed PNG/DDS assets.
`mcrtx_cloud_lifetime_tests` uses a mock Remix interface to check completed-frame
publication, transform-only reuse, stable-hash replacement, rebasing, fast/fancy
switches, deferred destruction, world clears and empty patches.

For gameplay testing, use Fancy graphics with BetaRT's Volumetric Clouds off.
Keep Sky Mode and other lighting options fixed when comparing performance.
Check moving clouds and camera motion, especially crossings of a 12-block cloud
texture phase, looking above/below/through the cloud layer, and world transitions.
