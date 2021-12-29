// automatically generated by the FlatBuffers compiler, do not modify

package MyGame.Sample;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class Edge extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_2_0_0(); }
  public static Edge getRootAsEdge(ByteBuffer _bb) { return getRootAsEdge(_bb, new Edge()); }
  public static Edge getRootAsEdge(ByteBuffer _bb, Edge obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Edge __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public byte type() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public int time() { int o = __offset(6); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int validity() { int o = __offset(8); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int routeType() { int o = __offset(10); return o != 0 ? bb.get(o + bb_pos) & 0xFF : 0; }
  public int feedIdWithTimezone() { int o = __offset(12); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int transfers() { int o = __offset(14); return o != 0 ? bb.get(o + bb_pos) & 0xFF : 0; }
  public int stopSequence() { int o = __offset(16); return o != 0 ? bb.get(o + bb_pos) & 0xFF : 0; }
  public int tripDescriptor() { int o = __offset(18); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int platformDescriptor() { int o = __offset(20); return o != 0 ? bb.getInt(o + bb_pos) : 0; }

  public static int createEdge(FlatBufferBuilder builder,
      byte type,
      int time,
      int validity,
      int route_type,
      int feed_id_with_timezone,
      int transfers,
      int stop_sequence,
      int trip_descriptor,
      int platform_descriptor) {
    builder.startTable(9);
    Edge.addPlatformDescriptor(builder, platform_descriptor);
    Edge.addTripDescriptor(builder, trip_descriptor);
    Edge.addFeedIdWithTimezone(builder, feed_id_with_timezone);
    Edge.addValidity(builder, validity);
    Edge.addTime(builder, time);
    Edge.addStopSequence(builder, stop_sequence);
    Edge.addTransfers(builder, transfers);
    Edge.addRouteType(builder, route_type);
    Edge.addType(builder, type);
    return Edge.endEdge(builder);
  }

  public static void startEdge(FlatBufferBuilder builder) { builder.startTable(9); }
  public static void addType(FlatBufferBuilder builder, byte type) { builder.addByte(0, type, 0); }
  public static void addTime(FlatBufferBuilder builder, int time) { builder.addInt(1, time, 0); }
  public static void addValidity(FlatBufferBuilder builder, int validity) { builder.addInt(2, validity, 0); }
  public static void addRouteType(FlatBufferBuilder builder, int routeType) { builder.addByte(3, (byte)routeType, (byte)0); }
  public static void addFeedIdWithTimezone(FlatBufferBuilder builder, int feedIdWithTimezone) { builder.addInt(4, feedIdWithTimezone, 0); }
  public static void addTransfers(FlatBufferBuilder builder, int transfers) { builder.addByte(5, (byte)transfers, (byte)0); }
  public static void addStopSequence(FlatBufferBuilder builder, int stopSequence) { builder.addByte(6, (byte)stopSequence, (byte)0); }
  public static void addTripDescriptor(FlatBufferBuilder builder, int tripDescriptor) { builder.addInt(7, tripDescriptor, 0); }
  public static void addPlatformDescriptor(FlatBufferBuilder builder, int platformDescriptor) { builder.addInt(8, platformDescriptor, 0); }
  public static int endEdge(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }
  public static void finishEdgeBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
  public static void finishSizePrefixedEdgeBuffer(FlatBufferBuilder builder, int offset) { builder.finishSizePrefixed(offset); }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Edge get(int j) { return get(new Edge(), j); }
    public Edge get(Edge obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

