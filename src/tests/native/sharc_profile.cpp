// Standalone profiling helper, separate from BetaRT's JNI transport. Reads
// diagnostics and changes configuration only in the isolated fixture process.
#include <dlfcn.h>
#include <jni.h>
#include <remix/remix_native_ext.h>
#include <remix/remix_reconstruction_ext.h>

extern "C" JNIEXPORT jint JNICALL Java_SharcProfileTest_configure(JNIEnv *env, jclass, jstring path,
                                                                jstring key, jstring value) {
  const char *name = env->GetStringUTFChars(path, nullptr);
  void *library = dlopen(name, RTLD_NOW | RTLD_NOLOAD);
  env->ReleaseStringUTFChars(path, name);
  if (!library)
    return -1;
  auto initialize = reinterpret_cast<decltype(&remixapi_InitializeLibrary)>(dlsym(library, "remixapi_InitializeLibrary"));
  remixapi_InitializeLibraryInfo info{};
  info.sType = REMIXAPI_STRUCT_TYPE_INITIALIZE_LIBRARY_INFO;
  info.version = REMIXAPI_VERSION_MAKE(0, 1003, 0);
  remixapi_Interface api{};
  int result = initialize ? int(initialize(&info, &api)) : -2;
  if (!result) {
    const char *k = env->GetStringUTFChars(key, nullptr), *v = env->GetStringUTFChars(value, nullptr);
    result = api.SetConfigVariable(k, v);
    env->ReleaseStringUTFChars(key, k);
    env->ReleaseStringUTFChars(value, v);
  }
  dlclose(library);
  return result;
}

extern "C" JNIEXPORT jlongArray JNICALL Java_SharcProfileTest_statistics(JNIEnv *env, jclass, jstring path) {
  const char *name = env->GetStringUTFChars(path, nullptr);
  void *library = dlopen(name, RTLD_NOW | RTLD_NOLOAD);
  env->ReleaseStringUTFChars(path, name);
  if (!library)
    return nullptr;
  auto denoise = reinterpret_cast<decltype(&remixapi_GetDenoisingStatisticsEXT)>(
      dlsym(library, "remixapi_GetDenoisingStatisticsEXT"));
  auto reconstruct = reinterpret_cast<decltype(&remixapi_GetReconstructionDiagnosticsEXT)>(
      dlsym(library, "remixapi_GetReconstructionDiagnosticsEXT"));
  auto trace = reinterpret_cast<decltype(&remixapi_GetTracingStatisticsEXT)>(
      dlsym(library, "remixapi_GetTracingStatisticsEXT"));
  auto lights = reinterpret_cast<decltype(&remixapi_GetLocalLightStatisticsEXT)>(
      dlsym(library, "remixapi_GetLocalLightStatisticsEXT"));
  auto cache = reinterpret_cast<decltype(&remixapi_GetSharcStatisticsEXT)>(
      dlsym(library, "remixapi_GetSharcStatisticsEXT"));
  remixapi_DenoisingStatisticsEXT d{};
  d.structSize = sizeof(d);
  remixapi_ReconstructionDiagnosticsEXT r{};
  r.structSize = sizeof(r);
  remixapi_TracingStatisticsEXT t{};
  t.structSize = sizeof(t);
  remixapi_LocalLightStatisticsEXT l{};
  l.structSize = sizeof(l);
  remixapi_SharcStatisticsEXT s{};
  s.structSize = sizeof(s);
  if (!denoise || !reconstruct || !trace || !lights || !cache || denoise(&d) != REMIXAPI_ERROR_CODE_SUCCESS ||
      reconstruct(&r) != REMIXAPI_ERROR_CODE_SUCCESS || trace(&t) != REMIXAPI_ERROR_CODE_SUCCESS ||
      lights(&l) != REMIXAPI_ERROR_CODE_SUCCESS || cache(&s) != REMIXAPI_ERROR_CODE_SUCCESS) {
    dlclose(library);
    return nullptr;
  }
  const jlong data[] = {jlong(d.renderedFrames), jlong(r.evaluatedFrames), jlong(r.activeBackend),
                       jlong(r.renderWidth), jlong(r.renderHeight), jlong(r.displayWidth),
                       jlong(r.displayHeight), jlong(r.failures), jlong(t.staticTlasBuilds),
                       jlong(l.lightCount), jlong(s.resets), jlong(s.occupiedEntries),
                       jlong(s.renderedFrames), jlong(s.active)};
  auto result = env->NewLongArray(14);
  env->SetLongArrayRegion(result, 0, 14, data);
  dlclose(library);
  return result;
}
