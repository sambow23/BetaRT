package mcrtx.bridge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.CancellationException;

/** World-space visibility; no game classes or rendering API dependencies. */
public final class UndergroundVisibility {
    public static final byte SOLID = 1;
    public static final byte TERRAIN = 2;
    public static final byte UNKNOWN = 4;
    public static final int SIZE = 4096;
    private static final int[][] STEPS = {{-1,0,0},{1,0,0},{0,-1,0},{0,1,0},{0,0,-1},{0,0,1}};
    private static final long EXTERIOR_DELAY = 500_000_000L;
    private static final int MAX_BEAMS = 16384;
    private static final double EPSILON = 0.0001;

    public static final class Section {
        public final int x, y, z;
        public final long revision;
        public final byte[] blocks;
        public final short[] labels = new short[SIZE];
        public final List<Pocket> pockets = new ArrayList<Pocket>();
        public final long[] hidden;
        public boolean dirty;

        Section(int x, int y, int z, long revision, byte[] blocks) {
            this.x = x; this.y = y; this.z = z; this.revision = revision;
            this.blocks = blocks.clone();
            Arrays.fill(labels, (short)-1);
            int[] queue = new int[SIZE];
            for (int start = 0; start < SIZE; ++start) {
                if ((blocks[start] & SOLID) != 0 || labels[start] >= 0) continue;
                Pocket pocket = new Pocket(this, pockets.size());
                pockets.add(pocket);
                int head = 0, tail = 0;
                queue[tail++] = start;
                labels[start] = (short)pocket.id;
                while (head != tail) {
                    int cell = queue[head++];
                    pocket.cells.set(cell);
                    if ((blocks[cell] & UNKNOWN) != 0) pocket.unknown = true;
                    int cx = cell & 15, cy = (cell >> 4) & 15, cz = cell >> 8;
                    for (int face = 0; face < 6; ++face) {
                        int nx = cx + STEPS[face][0], ny = cy + STEPS[face][1], nz = cz + STEPS[face][2];
                        if (nx < 0 || nx > 15 || ny < 0 || ny > 15 || nz < 0 || nz > 15) continue;
                        int next = index(nx, ny, nz);
                        if ((blocks[next] & SOLID) == 0 && labels[next] < 0) {
                            labels[next] = (short)pocket.id;
                            queue[tail++] = next;
                        }
                    }
                }
            }
            hidden = new long[(pockets.size()+63)/64];
        }
    }

    private static final class Pocket {
        final Section section;
        final int id;
        final BitSet cells = new BitSet(SIZE);
        final List<Portal> edges = new ArrayList<Portal>();
        boolean exterior, unknown, unknownBase;
        long lastRetained = Long.MIN_VALUE;
        Pocket(Section section, int id) { this.section = section; this.id = id; }
    }

    private static final class Portal {
        final Pocket target;
        final int axis, sign;
        final double plane;
        double u0 = Double.POSITIVE_INFINITY, v0 = Double.POSITIVE_INFINITY;
        double u1 = Double.NEGATIVE_INFINITY, v1 = Double.NEGATIVE_INFINITY;
        Portal(Pocket target, int axis, int sign, double plane) {
            this.target = target; this.axis = axis; this.sign = sign; this.plane = plane;
        }
        List<double[]> polygon() {
            List<double[]> result = new ArrayList<double[]>(4);
            for (int corner = 0; corner < 4; ++corner) {
                double[] p = new double[3];
                p[axis] = plane;
                p[(axis + 1) % 3] = (corner == 0 || corner == 3) ? u0 - EPSILON : u1 + EPSILON;
                p[(axis + 2) % 3] = corner < 2 ? v0 - EPSILON : v1 + EPSILON;
                result.add(p);
            }
            return result;
        }
    }

    private static final class Beam {
        final Pocket pocket;
        final List<double[]> planes;
        final Beam parent;
        final Section origin;
        Beam(Pocket pocket, List<double[]> planes, Beam parent, Section origin) {
            this.pocket=pocket; this.planes=planes; this.parent=parent; this.origin=origin;
        }
        boolean crossesAgain(Section section) {
            if (section==origin) return true;
            for (Beam beam=this;beam!=null;beam=beam.parent) if (beam.pocket.section==section) return true;
            return false;
        }
    }

    private final Map<Long, Section> sections = new HashMap<Long, Section>();
    private final Set<Long> dirtyColumns = new HashSet<Long>();
    private final Set<Long> dirtySections = new HashSet<Long>();
    private static final AtomicLong nextRevision = new AtomicLong(1);
    private long exteriorSince = Long.MIN_VALUE;
    private String status = "Waiting for topology";
    private long queryNanos;
    private int retainedPockets, hiddenPockets, beamCount, uncertainPockets;

    public static int index(int x, int y, int z) { return x | (y << 4) | (z << 8); }
    public static byte blockFlags(int id) {
        switch (id) {
            case 1: case 2: case 3: case 7: case 12: case 13: case 14: case 15: case 16:
            case 21: case 24: case 48: case 49: case 56: case 73: case 74: case 80: case 82: case 87:
                return SOLID | TERRAIN;
            case 4: case 5: case 19: case 22: case 23: case 25: case 35: case 41: case 42:
            case 43: case 45: case 46: case 47: case 57: case 58: case 61: case 62: case 84:
            case 86: case 89: case 91:
                return SOLID;
            default:
                return 0;
        }
    }
    public static long key(int x, int y, int z) {
        return (((long)(x >> 4) & 0x3FFFFFFL) << 37)
            | (((long)(z >> 4) & 0x3FFFFFFL) << 11) | ((y >> 4) & 0x7FFL);
    }
    public Section get(int x, int y, int z) { return sections.get(key(x, y, z)); }
    public Iterable<Section> sections() { return sections.values(); }
    public String status() { return status; }
    public String statistics() {
        return status + " pockets=" + retainedPockets + "/" + hiddenPockets
            + " uncertain=" + uncertainPockets + " beams=" + beamCount + " cpuUs=" + queryNanos / 1000;
    }
    public void clear() {
        sections.clear(); dirtyColumns.clear(); dirtySections.clear(); exteriorSince = Long.MIN_VALUE;
        status = "Waiting for topology";
    }
    private void markChanged(int x, int y, int z) {
        dirtyColumns.add(key(x,0,z));
        dirtySections.add(key(x,y,z));
        for (int[] step : STEPS) dirtySections.add(key(x+16*step[0],y+16*step[1],z+16*step[2]));
    }
    public void remove(int x, int y, int z) {
        if (sections.remove(key(x,y,z)) != null) markChanged(x,y,z);
    }
    public void invalidate(int x, int y, int z) {
        Section section = get(x,y,z);
        if (section != null && !section.dirty) {
            section.dirty = true; Arrays.fill(section.hidden, 0); markChanged(x,y,z);
        }
    }
    public Section put(int x, int y, int z, byte[] blocks) {
        if (blocks.length != SIZE || (x & 15) != 0 || (y & 15) != 0 || (z & 15) != 0 || y < 0 || y >= 128) {
            throw new IllegalArgumentException("Invalid section topology");
        }
        Section previous = get(x,y,z);
        if (previous != null && Arrays.equals(previous.blocks, blocks)) {
            if (previous.dirty) markChanged(x,y,z);
            previous.dirty = false;
            return previous;
        }
        Section section = new Section(x,y,z,nextRevision.getAndIncrement(),blocks);
        sections.put(key(x,y,z), section);
        markChanged(x,y,z);
        return section;
    }

    private int flags(int x, int y, int z) {
        if (y >= 128) return 0;
        if (y < 0) return SOLID | TERRAIN;
        Section section = get(x,y,z);
        return section == null || section.dirty ? UNKNOWN : section.blocks[index(x & 15,y & 15,z & 15)];
    }
    private int terrainHeight(int x, int z) {
        for (int y = 127; y >= 0; --y) {
            int value = flags(x,y,z);
            if ((value & UNKNOWN) != 0) return -1;
            if ((value & TERRAIN) != 0) return y + 1;
        }
        return 0;
    }
    public boolean isExterior(double x, double y, double z) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || y < 0) return false;
        int bx = (int)Math.floor(x), bz = (int)Math.floor(z);
        int own = terrainHeight(bx,bz);
        if (own < 0 || y < own) return false;
        int[] heights = new int[9];
        int n = 0;
        for (int dz = -8; dz <= 8; dz += 8) {
            for (int dx = -8; dx <= 8; dx += 8) {
                int height = terrainHeight(bx + dx,bz + dz);
                if (height < 0) return false;
                heights[n++] = height;
            }
        }
        Arrays.sort(heights);
        boolean covered = false;
        for (int by = Math.max(0,(int)Math.floor(y)); by < 128; ++by) {
            int value = flags(bx,by,bz);
            if ((value & UNKNOWN) != 0) return false;
            covered |= (value & SOLID) != 0;
        }
        return !covered || y >= heights[4];
    }

    private void rebuildGraph() {
        // Missing columns are uncertain, never additional sky entrances.
        for (long column : dirtyColumns) {
            checkInterrupted();
            int originX = (int)(column >> 37);
            if ((originX & 0x2000000) != 0) originX |= ~0x3FFFFFF;
            originX <<= 4;
            int originZ = (int)((column >> 11) & 0x3FFFFFFL);
            if ((originZ & 0x2000000) != 0) originZ |= ~0x3FFFFFF;
            originZ <<= 4;
            int[] height = new int[256];
            for (int z = 0; z < 16; ++z) for (int x = 0; x < 16; ++x) {
                height[x | (z << 4)] = terrainHeight(originX+x,originZ+z);
            }
            for (int y = 0; y < 128; y += 16) {
                Section section = get(originX,y,originZ);
                if (section == null) continue;
                dirtySections.add(key(originX,y,originZ));
                for (Pocket pocket : section.pockets) {
                    pocket.exterior = false; pocket.unknownBase = section.dirty;
                    for (int cell = pocket.cells.nextSetBit(0); cell >= 0; cell = pocket.cells.nextSetBit(cell+1)) {
                        int top = height[(cell & 15) | ((cell >> 8) << 4)];
                        if (top < 0 || (section.blocks[cell] & UNKNOWN) != 0) pocket.unknownBase = true;
                        else if (section.y + ((cell >> 4) & 15) >= top) pocket.exterior = true;
                    }
                }
            }
        }
        for (long sectionKey : dirtySections) {
            Section section = sections.get(sectionKey);
            if (section == null) continue;
            for (Pocket pocket : section.pockets) {
                pocket.edges.clear(); pocket.unknown = pocket.unknownBase;
            }
            int[] origin = {section.x,section.y,section.z};
            int[] p = new int[3];
            for (int face = 0; face < 6; ++face) {
                int axis = face / 2, sign = (face & 1) == 0 ? -1 : 1;
                Section neighbor = get(section.x+16*STEPS[face][0],section.y+16*STEPS[face][1],section.z+16*STEPS[face][2]);
                Map<Long, Portal> portals = new HashMap<Long, Portal>();
                for (int v = 0; v < 16; ++v) for (int u = 0; u < 16; ++u) {
                    p[axis] = sign < 0 ? 0 : 15;
                    p[(axis+1)%3] = u; p[(axis+2)%3] = v;
                    int source = section.labels[index(p[0],p[1],p[2])];
                    if (source < 0) continue;
                    Pocket pocket = section.pockets.get(source);
                    if (neighbor == null || neighbor.dirty) {
                        if (!(axis == 1 && (section.y == 0 && sign < 0 || section.y == 112 && sign > 0))) pocket.unknown = true;
                        continue;
                    }
                    p[axis] = sign < 0 ? 15 : 0;
                    int label = neighbor.labels[index(p[0],p[1],p[2])];
                    if (label < 0) continue;
                    long pair = ((long)source << 32) | label;
                    Portal portal = portals.get(pair);
                    if (portal == null) {
                        portal = new Portal(neighbor.pockets.get(label),axis,sign,origin[axis]+(sign < 0 ? 0 : 16));
                        portals.put(pair,portal); pocket.edges.add(portal);
                    }
                    int worldU = u+origin[(axis+1)%3], worldV = v+origin[(axis+2)%3];
                    portal.u0 = Math.min(portal.u0,worldU); portal.u1 = Math.max(portal.u1,worldU+1);
                    portal.v0 = Math.min(portal.v0,worldV); portal.v1 = Math.max(portal.v1,worldV+1);
                }
            }
        }
        dirtyColumns.clear(); dirtySections.clear();
    }

    public void update(double[] eye, double[] player, boolean supportedDimension, long now) {
        if (!dirtyColumns.isEmpty()) rebuildGraph();
        boolean exterior = supportedDimension && isExterior(eye[0],eye[1],eye[2]) && isExterior(player[0],player[1],player[2]);
        if (!exterior) exteriorSince = Long.MIN_VALUE;
        else if (exteriorSince == Long.MIN_VALUE) exteriorSince = now;
        boolean trimming = exterior && now-exteriorSince >= EXTERIOR_DELAY;
        evaluate(new double[][]{eye,eye},new double[][]{player,player},trimming,now,true);
    }

    public void updateRegion(int[] eye, int[] player, int size, boolean supportedDimension) {
        if (!dirtyColumns.isEmpty()) rebuildGraph();
        double[][] source = bounds(eye,size), body = bounds(player,size);
        boolean exterior = supportedDimension && exteriorRegion(eye,size) && exteriorRegion(player,size);
        evaluate(source,body,exterior,0,false);
    }

    private static double[][] bounds(int[] p, int size) {
        return new double[][]{{p[0],p[1],p[2]},{p[0]+size,p[1]+1,p[2]+size}};
    }

    private boolean exteriorRegion(int[] p, int size) {
        for (int z=p[2]; z<p[2]+size; ++z) for (int x=p[0]; x<p[0]+size; ++x) {
            if (!isExterior(x,p[1],z)) return false;
        }
        return true;
    }

    private void evaluate(double[][] source, double[][] body, boolean trimming, long now, boolean delay) {
        long start = System.nanoTime();
        status = sections.isEmpty() ? "Waiting for topology" : trimming ? "Exterior trimming" : "Cave protection";
        Set<Pocket> retained = new HashSet<Pocket>();
        Set<Pocket> uncertain = new HashSet<Pocket>();
        for (Section section : sections.values()) for (Pocket pocket : section.pockets) {
            if (pocket.exterior) retained.add(pocket);
            if (pocket.unknown && !pocket.exterior) uncertain.add(pocket);
        }
        // Unknown regions seed unrestricted portal beams, not unbounded exterior bounce visibility.
        uncertainPockets = uncertain.size();
        retained.addAll(uncertain);
        addRegion(retained,source); addRegion(retained,body);
        beamCount = 0;
        if (trimming) {
            if (!tracePortals(retained,source)) {
                status = "Cave protection (portal budget)";
                flood(retained,Integer.MAX_VALUE);
            } else {
                flood(retained,2);
                for (Section section : sections.values()) {
                    double dx = Math.max(0,Math.max(section.x-body[1][0],body[0][0]-section.x-16));
                    double dy = Math.max(0,Math.max(section.y-body[1][1],body[0][1]-section.y-16));
                    double dz = Math.max(0,Math.max(section.z-body[1][2],body[0][2]-section.z-16));
                    if (dx*dx+dy*dy+dz*dz <= 32*32) retained.addAll(section.pockets);
                }
            }
        } else {
            flood(retained,Integer.MAX_VALUE);
        }
        retainedPockets = hiddenPockets = 0;
        for (Section section : sections.values()) {
            checkInterrupted();
            Arrays.fill(section.hidden,0);
            for (Pocket pocket : section.pockets) {
                boolean keep = retained.contains(pocket);
                if (keep) pocket.lastRetained = now;
                else if (delay && pocket.lastRetained != Long.MIN_VALUE && now-pocket.lastRetained < EXTERIOR_DELAY) keep = true;
                if (keep) ++retainedPockets;
                else {
                    ++hiddenPockets;
                    section.hidden[pocket.id >> 6] |= 1L << (pocket.id & 63);
                }
            }
        }
        queryNanos = System.nanoTime()-start;
    }

    private void addRegion(Set<Pocket> retained, double[][] region) {
        for (int z=(int)Math.floor(region[0][2]); z<=Math.floor(region[1][2]); ++z)
            for (int y=(int)Math.floor(region[0][1]); y<=Math.floor(region[1][1]); ++y)
                for (int x=(int)Math.floor(region[0][0]); x<=Math.floor(region[1][0]); ++x) {
                    addPosition(retained,new double[]{x,y,z});
                }
    }
    private void addPosition(Set<Pocket> retained, double[] p) {
        int x = (int)Math.floor(p[0]), y = (int)Math.floor(p[1]), z = (int)Math.floor(p[2]);
        Section section = get(x,y,z);
        if (section == null) return;
        int label = section.labels[index(x & 15,y & 15,z & 15)];
        if (label >= 0) retained.add(section.pockets.get(label));
        else retained.addAll(section.pockets);
    }
    private static void flood(Set<Pocket> pockets, int limit) {
        ArrayDeque<Pocket> queue = new ArrayDeque<Pocket>(pockets);
        for (int depth = 0; depth < limit && !queue.isEmpty(); ++depth) {
            int count = queue.size();
            while (count-- > 0) for (Portal portal : queue.removeFirst().edges) {
                if (pockets.add(portal.target)) queue.addLast(portal.target);
            }
        }
    }

    private boolean tracePortals(Set<Pocket> retained, double[][] source) {
        ArrayDeque<Beam> queue = new ArrayDeque<Beam>();
        for (Pocket pocket : new ArrayList<Pocket>(retained)) {
            for (Portal portal : pocket.edges) if (!retained.contains(portal.target)) {
                List<double[]> planes = clip(portal,new ArrayList<double[]>(),source);
                if (planes != null) queue.add(new Beam(portal.target,planes,null,pocket.section));
            }
        }
        while (!queue.isEmpty()) {
            checkInterrupted();
            if (++beamCount > MAX_BEAMS) return false;
            Beam beam = queue.removeFirst();
            retained.add(beam.pocket);
            for (Portal portal : beam.pocket.edges) {
                // A straight ray cannot leave and re-enter the same convex section box.
                if (beam.crossesAgain(portal.target.section)) continue;
                double nearest = source[portal.sign > 0 ? 0 : 1][portal.axis];
                if (portal.target.exterior || (nearest-portal.plane)*portal.sign > EPSILON) continue;
                List<double[]> planes = clip(portal,beam.planes,source);
                if (planes != null) queue.add(new Beam(portal.target,planes,beam,beam.origin));
            }
        }
        return true;
    }
    private static void checkInterrupted() {
        if (Thread.currentThread().isInterrupted()) throw new CancellationException();
    }
    private static double dot(double[] plane, double[] p) {
        return plane[0]*p[0]+plane[1]*p[1]+plane[2]*p[2]+plane[3];
    }
    private static List<double[]> clip(Portal portal, List<double[]> previous, double[][] source) {
        List<double[]> polygon = portal.polygon();
        for (double[] plane : previous) {
            List<double[]> output = new ArrayList<double[]>();
            for (int i = 0; i < polygon.size(); ++i) {
                double[] a = polygon.get(i), b = polygon.get((i+1)%polygon.size());
                double da = dot(plane,a)+EPSILON, db = dot(plane,b)+EPSILON;
                if (da >= 0) output.add(a);
                if ((da >= 0) != (db >= 0)) {
                    double t = da/(da-db);
                    output.add(new double[]{a[0]+t*(b[0]-a[0]),a[1]+t*(b[1]-a[1]),a[2]+t*(b[2]-a[2])});
                }
            }
            polygon = output;
            if (polygon.isEmpty()) return null;
        }
        List<double[]> result = new ArrayList<double[]>(previous);
        double[][] corners = new double[8][3];
        for (int i=0; i<8; ++i) for (int axis=0; axis<3; ++axis) corners[i][axis] = source[(i>>axis)&1][axis];
        for (int i = 0; i < polygon.size(); ++i) {
            double[] a = polygon.get(i), b = polygon.get((i+1)%polygon.size());
            for (double[] eye : corners) {
                double ax=a[0]-eye[0], ay=a[1]-eye[1], az=a[2]-eye[2];
                double bx=b[0]-eye[0], by=b[1]-eye[1], bz=b[2]-eye[2];
                double[] plane = {ay*bz-az*by,az*bx-ax*bz,ax*by-ay*bx,0};
                double length = Math.sqrt(plane[0]*plane[0]+plane[1]*plane[1]+plane[2]*plane[2]);
                if (length < EPSILON) continue;
                for (int axis=0; axis<3; ++axis) plane[axis] /= length;
                plane[3] = -(plane[0]*a[0]+plane[1]*a[1]+plane[2]*a[2]);
                boolean accepted = false;
                for (int sign=0; sign<2; ++sign) {
                    // Separating planes bound rays from the entire source box through the aperture.
                    boolean valid = true;
                    for (double[] c : corners) if (dot(plane,c) > EPSILON) valid = false;
                    for (double[] p : polygon) if (dot(plane,p) < -EPSILON) valid = false;
                    if (valid) { accepted = true; break; }
                    for (int axis=0; axis<4; ++axis) plane[axis] = -plane[axis];
                }
                if (accepted) {
                    boolean duplicate=false;
                    for (double[] prior:result) {
                        double delta=0;
                        for (int axis=0;axis<4;++axis) delta=Math.max(delta,Math.abs(prior[axis]-plane[axis]));
                        if (delta<EPSILON) { duplicate=true; break; }
                    }
                    // Dropping additional constraints widens visibility instead of hiding geometry.
                    if (!duplicate && result.size()<64) result.add(plane);
                    break;
                }
            }
        }
        return result;
    }
}
