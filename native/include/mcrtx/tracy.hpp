#pragma once

#include <condition_variable>
#include <mutex>

#if defined(MCRTX_ENABLE_TRACY)
#include "tracy/Tracy.hpp"

#define MCRTX_TRACY_SCOPE(name_literal) ZoneScopedN(name_literal)
#define MCRTX_TRACY_VALUE(value) ZoneValue(static_cast<uint64_t>(value))
#define MCRTX_TRACY_FRAME_MARK() FrameMark
#define MCRTX_TRACY_SET_THREAD_NAME(name_literal) ::tracy::SetThreadName(name_literal)
#define MCRTX_TRACY_LOCK_MARK(lockable) LockMark(lockable)
#define MCRTX_TRACY_LOCKABLE_TYPE(type) LockableBase(type)
#define MCRTX_TRACY_LOCKABLE_N(type, varname, desc) TracyLockableN(type, varname, desc)
#else
#define MCRTX_TRACY_SCOPE(name_literal)
#define MCRTX_TRACY_VALUE(value)
#define MCRTX_TRACY_FRAME_MARK()
#define MCRTX_TRACY_SET_THREAD_NAME(name_literal)
#define MCRTX_TRACY_LOCK_MARK(lockable)
#define MCRTX_TRACY_LOCKABLE_TYPE(type) type
#define MCRTX_TRACY_LOCKABLE_N(type, varname, desc) type varname
#endif

namespace mcrtx {

using TracyMutex = MCRTX_TRACY_LOCKABLE_TYPE(std::mutex);
using TracyUniqueLock = std::unique_lock<TracyMutex>;

#if defined(MCRTX_ENABLE_TRACY)
using TracyConditionVariable = std::condition_variable_any;
#else
using TracyConditionVariable = std::condition_variable;
#endif

}  // namespace mcrtx
