#pragma once

#include <functional>

namespace mcrtx {

void withCocoaEventPump(const std::function<void()>& action);
void installCocoaEventPump(std::function<void()> pump);
void removeCocoaEventPump();

}
