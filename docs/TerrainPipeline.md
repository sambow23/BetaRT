# Terrain pipeline

`DirectSectionResidency` scans loaded chunks within render distance every 100 ms.
It allocates eight 16³ sections per chunk, independent of vanilla renderer/display
list objects. Chunk arrival/removal invalidates neighboring boundaries. World
changes advance a generation; coordinate reuse receives a new lifetime.

`DirectSectionUpdates` merges block notifications without dropping dirty regions.
Java collects Minecraft-dependent shapes, tiles, tints and fluid properties in
256-block slices with a 2 ms frame budget. Missing chunks remain pending without
generation or disk access. Backpressure retries the retained records unchanged;
an edit during collection restarts it. World/lifetime changes reject stale work.
These are changed block records, not per-frame snapshots of rendered geometry.

The private terrain JNI packet carries coordinates, generation, lifetime, revision,
inclusive dirty bounds and 23 integers per block. JNI copies records before
returning. Native storage merges partial updates into immutable section inputs.

One or two mesh workers retain a one-block neighbor halo. Boundary revisions,
section lifetimes, generation and content revisions invalidate obsolete jobs.
Identical normalized inputs avoid a rebuild. Pending/completed queues are bounded,
and reset/shutdown releases blocked workers before joining them.

The SDL owner thread admits up to four completed sections, 8 MiB or 1 ms each
iteration. Both passes publish atomically. Matching geometry fingerprints reuse
existing Remix mesh handles. Per-frame CPU frustum checks select resident draws;
rotation does not upload geometry. Materials use the CPU terrain atlas and
animated fluid/portal textures. Fire is ordinary textured geometry. There is no
light-placement, acceleration-structure or path-tracing work.

Native tests compare retained block emitters with a reference implementation and
exercise partial/empty updates, boundary changes, stale lifetimes, queue limits
and shutdown. Java fixtures exercise loaded-chunk residency, missing neighbors,
backpressure, edit-during-collection and world changes. The in-memory client test
adds actual vanilla block data, entities, effects, inventory and unload.
