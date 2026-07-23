#pragma once

#include <array>
#include <cstddef>
#include <cstdint>
#include <string>

#include <windows.h>

#include <remix/remix_c.h>

#include <mcrtx/core/remix_geometry_common.hpp>

// Constants and utility declarations shared by multiple render domains.
namespace mcrtx {
namespace detail {

// ---- Shared constants -----------------------------------------------------

constexpr std::size_t kTerrainMaterialClassCount = 7;
constexpr std::uint8_t kOpaqueTerrainMaterialClass = 0;
constexpr std::uint8_t kCutoutTerrainMaterialClass = 1;
constexpr std::uint8_t kWaterTerrainMaterialClass = 2;
constexpr std::uint8_t kLavaTerrainMaterialClass = 3;
constexpr std::uint8_t kPoweredRedstoneTerrainMaterialClass = 4;
constexpr std::uint8_t kPortalTerrainMaterialClass = 5;
constexpr std::uint8_t kIceTerrainMaterialClass = 6;
constexpr std::uint8_t kCubeBlockRenderType = 0;
constexpr std::uint8_t kCrossedQuadBlockRenderType = 1;
constexpr std::uint8_t kTorchBlockRenderType = 2;
constexpr std::uint8_t kFireBlockRenderType = 3;
constexpr std::uint8_t kRedstoneDustBlockRenderType = 5;
constexpr std::uint8_t kCropBlockRenderType = 6;
constexpr std::uint8_t kDoorBlockRenderType = 7;
constexpr std::uint8_t kLadderBlockRenderType = 8;
constexpr std::uint8_t kRailBlockRenderType = 9;
constexpr std::uint8_t kStairBlockRenderType = 10;
constexpr std::uint8_t kFenceBlockRenderType = 11;
constexpr std::uint8_t kLeverOrButtonBlockRenderType = 12;
constexpr std::uint8_t kCactusBlockRenderType = 13;
constexpr std::uint8_t kBedBlockRenderType = 14;
constexpr std::uint8_t kRepeaterBlockRenderType = 15;
constexpr std::uint8_t kPistonBaseBlockRenderType = 16;
constexpr std::uint8_t kPistonHeadBlockRenderType = 17;
constexpr std::uint8_t kLiquidBlockRenderType = 4;
constexpr std::uint8_t kGrassBlockId = 2;
constexpr std::uint8_t kLeavesBlockId = 18;
constexpr std::uint8_t kDoubleSlabBlockId = 43;
constexpr std::uint8_t kSingleSlabBlockId = 44;
constexpr std::uint8_t kBedBlockId = 26;
constexpr std::uint8_t kTallGrassBlockId = 31;
constexpr std::uint8_t kTorchBlockId = 50;
constexpr std::uint8_t kCropsBlockId = 59;
constexpr std::uint8_t kWoodStairsBlockId = 53;
constexpr std::uint8_t kRedstoneDustBlockId = 55;
constexpr std::uint8_t kWoodDoorBlockId = 64;
constexpr std::uint8_t kLadderBlockId = 65;
constexpr std::uint8_t kLeverBlockId = 69;
constexpr std::uint8_t kStonePressurePlateBlockId = 70;
constexpr std::uint8_t kStoneStairsBlockId = 67;
constexpr std::uint8_t kWoodPressurePlateBlockId = 72;
constexpr std::uint8_t kRedstoneTorchOffBlockId = 75;
constexpr std::uint8_t kRedstoneTorchOnBlockId = 76;
constexpr std::uint8_t kStoneButtonBlockId = 77;
constexpr std::uint8_t kIronDoorBlockId = 71;
constexpr std::uint8_t kGoldenRailBlockId = 27;
constexpr std::uint8_t kDetectorRailBlockId = 28;
constexpr std::uint8_t kIceBlockId = 79;
constexpr std::uint8_t kCactusBlockId = 81;
constexpr std::uint8_t kRailBlockId = 66;
constexpr std::uint8_t kFenceBlockId = 85;
constexpr std::uint8_t kNetherPortalBlockId = 90;
constexpr std::uint8_t kRepeaterIdleBlockId = 93;
constexpr std::uint8_t kRepeaterPoweredBlockId = 94;
constexpr std::uint8_t kStickyPistonBlockId = 29;
constexpr std::uint8_t kPistonBaseBlockId = 33;
constexpr std::uint8_t kPistonHeadBlockId = 34;
constexpr std::uint8_t kTrapdoorBlockId = 96;
constexpr std::uint8_t kWaterStillBlockId = 8;
constexpr std::uint8_t kWaterFlowingBlockId = 9;
constexpr std::uint8_t kLavaStillBlockId = 10;
constexpr std::uint8_t kLavaFlowingBlockId = 11;
constexpr std::int16_t kWaterStillTerrainTile = 205;
constexpr std::int16_t kWaterFlowingTerrainTile = 206;
constexpr std::int16_t kLavaStillTerrainTile = 237;
constexpr std::int16_t kLavaFlowingTerrainTile = 238;
constexpr std::uint8_t kGrassOverlayTerrainTile = 38;
constexpr std::int16_t kCobblestoneTerrainTile = 16;
constexpr std::uint8_t kLeavesFancyTextureOak = 52;
constexpr std::uint8_t kLeavesFastTextureOak = 53;
constexpr std::uint8_t kLeavesFancyTextureBirchSpruce = 132;
constexpr std::uint8_t kLeavesFastTextureBirchSpruce = 133;
constexpr float kAtlasSizePixels = 256.0f;
constexpr float kAtlasTileSizePixels = 16.0f;
constexpr float kAtlasUvInsetPixels = 0.01f;
constexpr float kFireAtlasWidthPixels = 256.0f;
constexpr float kFireAtlasHeightPixels = 32.0f;
constexpr std::uint32_t kFireAnimationFrameCount = 16;
constexpr ULONGLONG kFireAnimationFrameIntervalMilliseconds = 50;
constexpr std::uint8_t kLiquidAnimationFrameCount = 32;
constexpr std::uint8_t kLiquidAnimationFramesPerSecond = 20;
constexpr std::uint8_t kPortalAnimationFrameCount = 32;
constexpr std::uint8_t kPortalAnimationFramesPerSecond = 20;
constexpr float kFireEmissiveIntensity = 1.35f;
inline constexpr remixapi_Float3D kFireEmissiveColor = {1.0f, 1.0f, 1.0f};
constexpr float kLavaEmissiveIntensity = 3.0f;
inline constexpr remixapi_Float3D kLavaEmissiveColor = {1.0f, 1.0f, 1.0f};
constexpr float kPortalEmissiveIntensity = 0.85f;
inline constexpr remixapi_Float3D kPortalEmissiveColor = {1.0f, 1.0f, 1.0f};
constexpr float kTerrainEmissiveIntensity = 5.0f;
inline constexpr remixapi_Float3D kTerrainEmissiveColor = {1.0f, 1.0f, 1.0f};

// Translucent material physics parameters
inline constexpr remixapi_Float3D kWaterTransmittanceColor = {0.74f, 0.9f, 1.0f};
constexpr float kWaterMinTransmittanceColor = 0.01f;
constexpr float kWaterMaxTransmittanceColor = 1.0f;
constexpr float kWaterTransmittanceDistance = 1.5f;
constexpr float kWaterMinTransmittanceDistance = 0.01f;
constexpr float kWaterMaxTransmittanceDistance = 25.0f;
constexpr float kWaterRefractiveIndex = 1.333f;
constexpr float kWaterMinRefractiveIndex = 1.0f;
constexpr float kWaterMaxRefractiveIndex = 3.0f;
constexpr bool kWaterUseDiffuseLayer = true;
constexpr float kWaterDiffuseLayerScale = 1.0f;
constexpr float kWaterMinDiffuseLayerScale = 0.0f;
constexpr float kWaterMaxDiffuseLayerScale = 1.0f;
constexpr float kWaterThinWallThickness = 0.001f;
constexpr float kWaterDefaultThinWallThickness = 1.0f;
constexpr float kWaterMaxThinWallThickness = 5.0f;

inline constexpr remixapi_Float3D kPortalTransmittanceColor = {0.5f, 0.2f, 0.8f};
constexpr float kPortalTransmittanceDistance = 0.5f;
constexpr float kPortalRefractiveIndex = 1.1f;

inline constexpr remixapi_Float3D kIceTransmittanceColor = {0.78f, 0.9f, 1.0f};
constexpr float kIceTransmittanceDistance = 0.45f;
constexpr float kIceRefractiveIndex = 1.31f;

constexpr float kFaceOverlayBias = 0.001f;
constexpr std::uint64_t kOpaqueTerrainMaterialHash = 0x4D435254584F5041ull;
constexpr std::uint64_t kCutoutTerrainMaterialHash = 0x4D43525458435554ull;
constexpr std::uint64_t kWaterTerrainMaterialHash = 0x4D43525458575452ull;
constexpr std::uint64_t kLavaTerrainMaterialHash = 0x4D435254584C4156ull;
constexpr std::uint64_t kPoweredRedstoneTerrainMaterialHash = 0x4D43525458524453ull;
constexpr std::uint64_t kPortalTerrainMaterialHash = 0x4D4352545850544Cull;
constexpr std::uint64_t kIceTerrainMaterialHash = 0x4D43525458494345ull;
constexpr std::uint64_t kDestroyOverlayMaterialHash = 0x4D43525458444F4Dull;
constexpr std::uint64_t kBlockOutlineGlowMaterialHash = 0x4D4352545842474Cull;
constexpr std::uint64_t kBlockOutlineRgbMaterialHashSeed = 0x4D43525458425200ull;
constexpr std::uint64_t kCloudMaterialHash = 0x4D43525458434C44ull;
constexpr std::uint64_t kFireMaterialHash = 0x4D43525458464952ull;
constexpr std::uint64_t kParticleMaterialHashSeed = 0x4D43525458505443ull;
constexpr std::uint32_t kDefaultVertexColor = 0xFFFFFFFFu;

inline constexpr float kFaceVertexOffsets[6][4][3] = {
  {{0.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}},
  {{0.0f, 0.0f, 1.0f}, {0.0f, 1.0f, 1.0f}, {1.0f, 1.0f, 1.0f}, {1.0f, 0.0f, 1.0f}},
  {{0.0f, 0.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 1.0f}, {0.0f, 0.0f, 1.0f}},
  {{1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 1.0f}, {1.0f, 1.0f, 1.0f}, {1.0f, 1.0f, 0.0f}},
  {{0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 1.0f}, {1.0f, 0.0f, 1.0f}, {1.0f, 0.0f, 0.0f}},
  {{0.0f, 1.0f, 0.0f}, {1.0f, 1.0f, 0.0f}, {1.0f, 1.0f, 1.0f}, {0.0f, 1.0f, 1.0f}},
};

inline constexpr float kFaceNormals[6][3] = {
  {0.0f, 0.0f, -1.0f},
  {0.0f, 0.0f, 1.0f},
  {-1.0f, 0.0f, 0.0f},
  {1.0f, 0.0f, 0.0f},
  {0.0f, -1.0f, 0.0f},
  {0.0f, 1.0f, 0.0f},
};

inline constexpr int kNativeFaceToMinecraftSide[6] = {2, 3, 4, 5, 0, 1};

inline constexpr float kFaceTexcoords[6][4][2] = {
  {{1.0f, 1.0f}, {0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}},
  {{0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}},
  {{0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}},
  {{1.0f, 1.0f}, {0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}},
  {{0.0f, 0.0f}, {0.0f, 1.0f}, {1.0f, 1.0f}, {1.0f, 0.0f}},
  {{0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}, {0.0f, 1.0f}},
};

inline constexpr std::uint32_t kFaceIndices[6] = {0, 1, 2, 0, 2, 3};

inline constexpr int kNeighborOffsets[6][3] = {
  {0, 0, -1},
  {0, 0, 1},
  {-1, 0, 0},
  {1, 0, 0},
  {0, -1, 0},
  {0, 1, 0},
};


using geometry::SurfaceBuildBuffers;

// ---- Shared render utilities ---------------------------------------------

std::string errorCodeToString(remixapi_ErrorCode code);
remixapi_Transform makeTranslationTransform(float x, float y, float z);
std::uint64_t mixHashComponent(std::uint64_t hash, std::uint32_t value);
std::uint8_t clampColorChannel(float value);
std::uint32_t packVertexColorRgba(float red, float green, float blue, float alpha);
std::uint32_t packVertexColor(std::uint32_t rgbColor);

std::array<float, 3> computeQuadNormal(
    float x0, float y0, float z0,
    float x1, float y1, float z1,
    float x2, float y2, float z2);

}  // namespace detail
}  // namespace mcrtx
