package com.graphhopper.routing.util;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.parsers.CountryParser;
import com.graphhopper.routing.util.parsers.OSMMaxSpeedParser;
import com.graphhopper.routing.util.parsers.OSMRoadClassParser;
import com.graphhopper.routing.util.parsers.StateParser;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.util.EdgeIteratorState;
import de.westnordost.osm_legal_default_speeds.LegalDefaultSpeeds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.graphhopper.routing.ev.MaxSpeed.UNLIMITED_SIGN_SPEED;
import static com.graphhopper.routing.ev.MaxSpeed.UNSET_SPEED;
import static com.graphhopper.routing.ev.State.*;
import static com.graphhopper.routing.ev.UrbanDensity.CITY;
import static com.graphhopper.routing.ev.UrbanDensity.RURAL;
import static org.junit.jupiter.api.Assertions.*;

class MaxSpeedCalculatorTest {

    private final LegalDefaultSpeeds defaultSpeeds = MaxSpeedCalculator.createLegalDefaultSpeeds();
    private MaxSpeedCalculator calc;
    private BaseGraph graph;
    private EncodingManager em;
    private EnumEncodedValue<UrbanDensity> urbanDensity;
    private DecimalEncodedValue maxSpeedEnc;
    private BooleanEncodedValue maxSpeedEstEnc;
    private OSMParsers parsers;

    @BeforeEach
    public void setup() {
        BooleanEncodedValue accessEnc = VehicleAccess.create("car");
        DecimalEncodedValue speedEnc = VehicleSpeed.create("car", 5, 5, false);
        EnumEncodedValue<RoadClass> roadClassEnc = RoadClass.create();
        urbanDensity = UrbanDensity.create();
        EnumEncodedValue<Country> countryEnc = Country.create();
        EnumEncodedValue<State> stateEnc = State.create();
        maxSpeedEnc = MaxSpeed.create();
        maxSpeedEstEnc = MaxSpeedEstimated.create();
        em = EncodingManager.start().add(urbanDensity).add(stateEnc).add(countryEnc).
                add(Roundabout.create()).add(Surface.create()).add(Lanes.create()).add(roadClassEnc).
                add(maxSpeedEnc).add(maxSpeedEstEnc).add(accessEnc).add(speedEnc).build();
        graph = new BaseGraph.Builder(em).create();
        calc = new MaxSpeedCalculator(defaultSpeeds);
        parsers = new OSMParsers();
        parsers.addWayTagParser(new StateParser(stateEnc));
        parsers.addWayTagParser(new CountryParser(countryEnc));
        parsers.addWayTagParser(new OSMRoadClassParser(roadClassEnc));
        parsers.addWayTagParser(new OSMMaxSpeedParser(maxSpeedEnc));
        parsers.addWayTagParser(calc.getParser());

        calc.createDataAccessForParser(new RAMDirectory());
    }

    @Test
    public void internalMaxSpeed() {
        EdgeIntAccess storage = calc.getInternalMaxSpeedStorage();
        DecimalEncodedValue ruralEnc = calc.getRuralMaxSpeedEnc();
        ruralEnc.setDecimal(false, 0, storage, UNSET_SPEED);
        assertEquals(UNSET_SPEED, ruralEnc.getDecimal(false, 0, storage));

        ruralEnc.setDecimal(false, 1, storage, 33);
        assertEquals(34, ruralEnc.getDecimal(false, 1, storage));

        DecimalEncodedValue urbanEnc = calc.getUrbanMaxSpeedEnc();
        urbanEnc.setDecimal(false, 1, storage, UNSET_SPEED);
        assertEquals(UNSET_SPEED, urbanEnc.getDecimal(false, 1, storage));

        urbanEnc.setDecimal(false, 0, storage, 46);
        assertEquals(46, urbanEnc.getDecimal(false, 0, storage));

        // check that they are not modified
        assertEquals(UNSET_SPEED, ruralEnc.getDecimal(false, 0, storage));
        assertEquals(34, ruralEnc.getDecimal(false, 1, storage));
    }

    EdgeIteratorState createEdge(ReaderWay way) {
        EdgeIteratorState edge = graph.edge(0, 1);
        EdgeIntAccess edgeIntAccess = graph.createEdgeIntAccess();
        parsers.handleWayTags(edge.getEdge(), edgeIntAccess, way, em.createRelationFlags());
        return edge;
    }

    @Test
    public void testCityGermany() {
        ReaderWay way = new ReaderWay(0L);
        way.setTag("country", Country.DEU);
        way.setTag("highway", "primary");
        EdgeIteratorState edge = createEdge(way).set(urbanDensity, CITY);
        calc.fillMaxSpeed(graph, em);
        assertEquals(50, edge.get(maxSpeedEnc), 1);
        assertTrue(edge.get(maxSpeedEstEnc));

        way = new ReaderWay(0L);
        way.setTag("country", Country.DEU);
        way.setTag("highway", "motorway");
        edge = createEdge(way).set(urbanDensity, CITY);
        calc.fillMaxSpeed(graph, em);
        assertEquals(UNLIMITED_SIGN_SPEED, edge.get(maxSpeedEnc), 1);

        way = new ReaderWay(0L);
        way.setTag("country", Country.DEU);
        way.setTag("highway", "residential");
        edge = createEdge(way).set(urbanDensity, CITY);
        calc.fillMaxSpeed(graph, em);
        assertEquals(50, edge.get(maxSpeedEnc), 1);

        way = new ReaderWay(0L);
        way.setTag("country", Country.DEU);
        way.setTag("highway", "residential");
        way.setTag("maxspeed", "70");
        edge = createEdge(way);
        calc.fillMaxSpeed(graph, em);
        assertEquals(70, edge.get(maxSpeedEnc), 1);
        assertFalse(edge.get(maxSpeedEstEnc));
    }

    @Test
    public void testRuralGermany() {
        ReaderWay way = new ReaderWay(0L);
        way.setTag("country", Country.DEU);
        way.setTag("highway", "primary");
        EdgeIteratorState edge = createEdge(way).set(urbanDensity, RURAL);
        calc.fillMaxSpeed(graph, em);
        assertEquals(100, edge.get(maxSpeedEnc), 1);
        assertEquals(100, edge.getReverse(maxSpeedEnc), 1);

        way = new ReaderWay(0L);
        way.setTag("country", Country.DEU);
        way.setTag("highway", "motorway");
        edge = createEdge(way).set(urbanDensity, RURAL);
        calc.fillMaxSpeed(graph, em);
        assertEquals(UNLIMITED_SIGN_SPEED, edge.get(maxSpeedEnc), 1);

        way = new ReaderWay(0L);
        way.setTag("country", Country.DEU);
        way.setTag("highway", "residential");
        edge = createEdge(way).set(urbanDensity, RURAL);
        calc.fillMaxSpeed(graph, em);
        assertEquals(100, edge.get(maxSpeedEnc), 1);
    }

    @Test
    public void testRoundabout() {
        ReaderWay way = new ReaderWay(0L);
        way.setTag("country", Country.CRI);
        way.setTag("highway", "primary");
        EdgeIteratorState edge = createEdge(way).set(urbanDensity, CITY);
        calc.fillMaxSpeed(graph, em);
        assertEquals(50, edge.get(maxSpeedEnc), 1);

        way = new ReaderWay(0L);
        way.setTag("country", Country.CRI);
        way.setTag("highway", "primary");
        way.setTag("junction", "roundabout");
        edge = createEdge(way).set(urbanDensity, CITY);
        calc.fillMaxSpeed(graph, em);
        assertEquals(30, edge.get(maxSpeedEnc), 1);
    }

    @Test
    public void testLanes() {
        ReaderWay way = new ReaderWay(0L);
        way.setTag("country", Country.CHL);
        way.setTag("highway", "primary");
        EdgeIteratorState edge = createEdge(way).set(urbanDensity, RURAL);
        calc.fillMaxSpeed(graph, em);
        assertEquals(100, edge.get(maxSpeedEnc), 1);

        way = new ReaderWay(0L);
        way.setTag("country", Country.CHL);
        way.setTag("highway", "primary");
        way.setTag("lanes", "4"); // 2 in each direction!
        edge = createEdge(way).set(urbanDensity, RURAL);
        calc.fillMaxSpeed(graph, em);
        assertEquals(120, edge.get(maxSpeedEnc), 1);
    }

    @Test
    public void testSurface() {
        ReaderWay way = new ReaderWay(0L);
        way.setTag("country", Country.LTU);
        way.setTag("highway", "primary");
        EdgeIteratorState edge = createEdge(way).set(urbanDensity, RURAL);
        calc.fillMaxSpeed(graph, em);
        assertEquals(90, edge.get(maxSpeedEnc), 1);

        way = new ReaderWay(0L);
        way.setTag("country", Country.LTU);
        way.setTag("highway", "primary");
        way.setTag("surface", "compacted");
        edge = createEdge(way).set(urbanDensity, RURAL);
        calc.fillMaxSpeed(graph, em);
        assertEquals(70, edge.get(maxSpeedEnc), 1);
    }

    @Test
    public void testLivingStreetWithWalk() {
        ReaderWay way = new ReaderWay(0L);
        way.setTag("country", Country.AUT);
        way.setTag("highway", "living_street");
        EdgeIteratorState edge = createEdge(way).set(urbanDensity, CITY);
        calc.fillMaxSpeed(graph, em);
        assertEquals(6, edge.get(maxSpeedEnc), 1);
    }

    @Test
    public void testLivingStreetWithMaxSpeed() {
        ReaderWay way = new ReaderWay(0L);
        way.setTag("country", Country.DEU);
        way.setTag("highway", "living_street");
        way.setTag("maxspeed", "30");
        EdgeIteratorState edge = createEdge(way).set(urbanDensity, CITY);
        calc.fillMaxSpeed(graph, em);
        assertEquals(30, edge.get(maxSpeedEnc), 1);
        assertEquals(30, edge.getReverse(maxSpeedEnc), 1);
    }

    @Test
    public void testFwdBwd() {
        ReaderWay way = new ReaderWay(0L);
        way.setTag("country", Country.DEU);
        way.setTag("highway", "primary");
        way.setTag("maxspeed:forward", "50");
        way.setTag("maxspeed:backward", "70");
        EdgeIteratorState edge = createEdge(way);
        calc.fillMaxSpeed(graph, em);
        // internal max speed must be ignored as library currently ignores forward/backward
        assertEquals(50, edge.get(maxSpeedEnc), 1);
        assertEquals(70, edge.getReverse(maxSpeedEnc), 1);
    }

    @Test
    public void testFwdOnly() {
        ReaderWay way = new ReaderWay(0L);
        way.setTag("country", Country.DEU);
        way.setTag("highway", "primary");
        way.setTag("maxspeed:forward", "50");
        EdgeIteratorState edge = createEdge(way);
        calc.fillMaxSpeed(graph, em);
        assertEquals(50, edge.get(maxSpeedEnc), 1);
        assertEquals(100, edge.getReverse(maxSpeedEnc), 1);
    }

    @Test
    public void testAllCountriesForMissingSpeedLimitOfFreeways() {
        List<EdgeIteratorState> edges = new ArrayList<>();
        for (Country c : Country.values()) {
            if (c == Country.MISSING) continue;

            ReaderWay way = new ReaderWay(0L);
            way.setTag("country", c);
            way.setTag("highway", "motorway");
            if (c.getStates().isEmpty()) {
                edges.add(createEdge(way).set(urbanDensity, CITY));
                edges.add(createEdge(way).set(urbanDensity, RURAL));
            } else
                for (State s : c.getStates()) {
                    way.setTag("country_state", s);
                    edges.add(createEdge(way).set(urbanDensity, CITY));
                    edges.add(createEdge(way).set(urbanDensity, RURAL));
                }
        }
        calc.fillMaxSpeed(graph, em);

        Map<String, Double> maxSpeedMap = new HashMap<>();
        maxSpeedMap.put(Country.ATG + "|" + MISSING + "|" + RURAL, 64.); // wiki is correct
        maxSpeedMap.put(Country.BHS + "|" + MISSING + "|" + RURAL, 48.); // TODO without state the rural speed limit should be 72
        maxSpeedMap.put(Country.BRB + "|" + MISSING + "|" + RURAL, 60.); // wiki is correct
        maxSpeedMap.put(Country.BTN + "|" + MISSING + "|" + RURAL, 50.); // wiki is correct
        maxSpeedMap.put(Country.CAN + "|" + CA_YT + "|" + RURAL, 50.); // TODO wiki incomplete
        maxSpeedMap.put(Country.CRI + "|" + MISSING + "|" + RURAL, 60.); // TODO wiki: speed limit seems to be 90
        maxSpeedMap.put(Country.DOM + "|" + MISSING + "|" + RURAL, 60.); // TODO wiki: seems to be 80 or for highways 120
        maxSpeedMap.put(Country.FSM + "|" + FM_KSA + "|" + RURAL, 40.); // wiki is correct
        maxSpeedMap.put(Country.FSM + "|" + FM_PNI + "|" + RURAL, 40.); // wiki is correct
        maxSpeedMap.put(Country.FSM + "|" + FM_TRK + "|" + RURAL, 40.); // wiki is correct
        maxSpeedMap.put(Country.FSM + "|" + FM_YAP + "|" + RURAL, 40.); // wiki is correct
        maxSpeedMap.put(Country.GGY + "|" + MISSING + "|" + RURAL, 56.);// wiki is correct
        maxSpeedMap.put(Country.GRD + "|" + MISSING + "|" + RURAL, 64.);// wiki is correct
        maxSpeedMap.put(Country.GUY + "|" + MISSING + "|" + RURAL, 64.);
        maxSpeedMap.put(Country.JEY + "|" + MISSING + "|" + RURAL, 64.);
        maxSpeedMap.put(Country.KIR + "|" + MISSING + "|" + RURAL, 60.);
        maxSpeedMap.put(Country.KNA + "|" + MISSING + "|" + RURAL, 64.);
        maxSpeedMap.put(Country.LBR + "|" + MISSING + "|" + RURAL, 72.);
        maxSpeedMap.put(Country.MCO + "|" + MISSING + "|" + RURAL, 50.);
        maxSpeedMap.put(Country.MHL + "|" + MISSING + "|" + RURAL, 64.);
        maxSpeedMap.put(Country.MSR + "|" + MISSING + "|" + RURAL, 64.);
        maxSpeedMap.put(Country.MUS + "|" + MISSING + "|" + RURAL, 60.);
        maxSpeedMap.put(Country.NIC + "|" + MISSING + "|" + RURAL, 60.);
        maxSpeedMap.put(Country.NRU + "|" + MISSING + "|" + RURAL, 50.);
        maxSpeedMap.put(Country.PCN + "|" + MISSING + "|" + RURAL, 48.);
        maxSpeedMap.put(Country.PER + "|" + MISSING + "|" + RURAL, 60.);
        maxSpeedMap.put(Country.PHL + "|" + MISSING + "|" + RURAL, 40.);
        maxSpeedMap.put(Country.SGP + "|" + MISSING + "|" + RURAL, 50.);
        maxSpeedMap.put(Country.SHN + "|" + MISSING + "|" + RURAL, 48.);
        maxSpeedMap.put(Country.TUV + "|" + MISSING + "|" + RURAL, 40.);
        maxSpeedMap.put(Country.TWN + "|" + MISSING + "|" + RURAL, 50.);
        maxSpeedMap.put(Country.USA + "|" + US_AL + "|" + RURAL, 72.);
        maxSpeedMap.put(Country.USA + "|" + US_DC + "|" + RURAL, 32.);
        maxSpeedMap.put(Country.USA + "|" + US_ME + "|" + RURAL, 72.);
        maxSpeedMap.put(Country.USA + "|" + US_MA + "|" + RURAL, 64.);
        maxSpeedMap.put(Country.WSM + "|" + MISSING + "|" + RURAL, 56.);

        for (EdgeIteratorState edge : edges) {
            State state = edge.get(em.getEnumEncodedValue(State.KEY, State.class));
            Country country = edge.get(em.getEnumEncodedValue(Country.KEY, Country.class));
            UrbanDensity ub = edge.get(urbanDensity);
            Double value = maxSpeedMap.get(country + "|" + state + "|" + ub);
            String msg = edge.getEdge() + ", max:" + edge.get(maxSpeedEnc) + ", " + country + ", " + state + ", " + ub;
            if (value == null) {
                assertTrue(edge.get(maxSpeedEnc) >= 80, msg);
            } else {
                assertEquals(value, edge.get(maxSpeedEnc), msg);
            }
        }
    }

    @Test
    public void testMissingSpeedLimitForTrunk() {
        ReaderWay way = new ReaderWay(0L);
        way.setTag("country", Country.USA);
        way.setTag("highway", "trunk");
        way.setTag("country_state", State.US_CA);
        EdgeIteratorState edge = createEdge(way).set(urbanDensity, CITY);
        calc.fillMaxSpeed(graph, em);
        assertTrue(edge.get(maxSpeedEnc) > 100, "max:" + edge.get(maxSpeedEnc));

        edge = createEdge(way).set(urbanDensity, RURAL);
        calc.fillMaxSpeed(graph, em);
        assertTrue(edge.get(maxSpeedEnc) > 100, "max:" + edge.get(maxSpeedEnc));
    }

    @Test
    public void testDifferentStates() {
        ReaderWay way = new ReaderWay(0L);
        way.setTag("country", Country.USA);
        way.setTag("highway", "primary");

        way.setTag("country_state", State.US_CA);
        EdgeIteratorState edge1 = createEdge(way);
        way.setTag("country_state", State.US_FL);
        EdgeIteratorState edge2 = createEdge(way);

        calc.fillMaxSpeed(graph, em);

        assertEquals(106, edge1.get(maxSpeedEnc));
        assertEquals(90, edge2.get(maxSpeedEnc));
    }

    @Test
    public void testRawAccess_RuralIsDefault_IfNoUrbanDensityWasSet() {
        Map<String, String> tags = new HashMap<>();
        tags.put("highway", "primary");
        LegalDefaultSpeeds.Result res = defaultSpeeds.getSpeedLimits(Country.DEU.getAlpha2(), tags, new ArrayList<>(), (name, eval) -> eval.invoke());

        assertEquals("100", res.getTags().get("maxspeed").toString());
    }

    @Test
    public void testRawAccess_FwdBwdSpeedsAreIgnored() {
        Map<String, String> tags = new HashMap<>();
        tags.put("highway", "primary");
        tags.put("maxspeed:forward", "70");
        tags.put("maxspeed:backward", "30");
        LegalDefaultSpeeds.Result res = defaultSpeeds.getSpeedLimits(Country.DEU.getAlpha2(), tags, new ArrayList<>(), (name, eval) -> eval.invoke());

        assertNull(res.getTags().get("maxspeed:forward"));
        assertEquals("100", res.getTags().get("maxspeed").toString());
    }

    @Test
    public void testGBR() {
        ReaderWay way = new ReaderWay(0L);
        way.setTag("country", Country.GBR);
        way.setTag("highway", "primary");
        EdgeIteratorState edge = createEdge(way).set(urbanDensity, CITY);
        calc.fillMaxSpeed(graph, em);
        assertEquals(48, edge.get(maxSpeedEnc), 1);

        edge = createEdge(way).set(urbanDensity, RURAL);
        calc.fillMaxSpeed(graph, em);
        assertEquals(98, edge.get(maxSpeedEnc), 1);

        way.setTag("highway", "motorway");
        edge = createEdge(way).set(urbanDensity, RURAL);
        calc.fillMaxSpeed(graph, em);
        assertEquals(114, edge.get(maxSpeedEnc), 1);
    }

    @Test
    public void testUnsupportedCountry() {
        ReaderWay way = new ReaderWay(0L);
        way.setTag("country", Country.AIA);
        way.setTag("highway", "primary");
        EdgeIteratorState edge = createEdge(way).set(urbanDensity, CITY);
        calc.fillMaxSpeed(graph, em);
        assertEquals(UNSET_SPEED, edge.get(maxSpeedEnc), 1);
    }
}
