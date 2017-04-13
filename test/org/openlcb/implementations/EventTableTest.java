package org.openlcb.implementations;

import junit.framework.TestCase;

import org.openlcb.EventID;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by bracz on 4/6/17.
 */
public class EventTableTest extends TestCase {
    EventTable tbl = new EventTable();

    EventID e1 = new EventID("01.02.03.04.05.06.07.08");
    EventID e2 = new EventID("01.02.03.04.05.06.07.09");
    EventID e3 = new EventID("01.02.03.04.05.06.07.0a");

    public void setUp() throws Exception {
        super.setUp();

    }

    public void testAddRemove() {
        EventTable.EventTableEntryHolder h1 = tbl.addEvent(e1, "teste1");
        EventTable.EventTableEntryHolder h2 = tbl.addEvent(e2, "teste2");
        EventTable.EventTableEntryHolder h1alt = tbl.addEvent(e1, "teste1alt");

        EventTable.EventInfo einfo1 = tbl.getEventInfo(e1);
        assertTrue(einfo1 == h1.event);
        assertTrue(einfo1 == h1alt.event);
        assertTrue(einfo1 != h2.event);
        assertEquals("teste1", h1.entry.getDescription());
        assertEquals("teste2", h2.entry.getDescription());
        assertEquals("teste1alt", h1alt.entry.getDescription());
        h1alt.getEntry().updateDescription("teste1altupd");
        assertEquals("teste1altupd", h1alt.entry.getDescription());

        EventTable.EventTableEntry[] elist = einfo1.getAllEntries();
        assertEquals(2, elist.length);
        assertTrue(elist[0].h == h1);
        assertTrue(elist[1].h == h1alt);

        elist = tbl.getEventInfo(e2).getAllEntries();
        assertEquals(1, elist.length);
        assertTrue(elist[0].h == h2);

        h1.release();
        elist = tbl.getEventInfo(e1).getAllEntries();
        assertEquals(1, elist.length);
        assertTrue(elist[0].h == h1alt);

        h1alt.release();
        elist = tbl.getEventInfo(e1).getAllEntries();
        assertEquals(0, elist.length);
    }

    class FakeListener implements PropertyChangeListener {
        EventTable.EventInfo newValue = null;
        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            assertEquals(EventTable.UPDATED_EVENT_LIST, propertyChangeEvent.getPropertyName());
            assertNull(propertyChangeEvent.getOldValue());

            assertNull("duplicate listener call", newValue);
            newValue = (EventTable.EventInfo) propertyChangeEvent.getNewValue();
            assertNotNull(newValue);
        }

        public void reset() { newValue = null; }

        public void verifyCall(EventTable.EventInfo expected) {
            assertNotNull("Expected call, but did not happen.", newValue);
            assertTrue("Incorrect newValue passed to property listener", newValue == expected);
            reset();
        }

        public void verifyNoInteraction() {
            assertNull("Expected no call, got one.", newValue);
        }
    }

    public void testNotify() {
        EventTable.EventInfo elist = tbl.getEventInfo(e3);
        FakeListener l = new FakeListener();

        elist.addPropertyChangeListener(l);
        l.verifyNoInteraction();
        EventTable.EventTableEntryHolder h1 = elist.add("teste3");
        l.verifyCall(elist);

        elist.add("teste3alt");
        l.verifyCall(elist);

        h1.release();
        l.verifyCall(elist);

        h1 = elist.add("testf3");
        l.verifyCall(elist);

        h1.getEntry().updateDescription("testf3bar");
        l.verifyCall(elist);
    }

    private final static int MPREFIX = 0;
    private final static int MSUB = 1;

    private boolean match(int type, String description, String query) {
        boolean actual = false;
        if (type == MPREFIX) actual = EventTable.wordPrefixMatch(description, query);
        else if (type == MSUB) actual = EventTable.substringMatch(description, query);
        else assertTrue("Unexpected match type " + Integer.toString(type), false);
        return actual;
    }

    private void expectMatch(int type, String description, String query) {
        assertTrue("Query '" + query + "' Description '" + description + "': Expected match, actual not match", match(type, description, query));
    }

    private void expectNotMatch(int type, String description, String query) {
        assertFalse("Query '" + query + "' Description '" + description + "': Expected not match, actual match", match(type, description, query));
    }

    public void testSubstringMatch() {
        expectMatch(MSUB, "", "");
        expectNotMatch(MSUB, "", "aa");
        expectMatch(MSUB, "aa", "");
        expectMatch(MSUB, "aa", "a");
        expectMatch(MSUB, "aa", "aa");
        expectNotMatch(MSUB, "aa", "aaa");

        expectMatch(MSUB, "aba", "");
        expectMatch(MSUB, "aba", "a");
        expectMatch(MSUB, "aba", "aa");
        expectNotMatch(MSUB, "aba", "aaa");

        expectMatch(MSUB, "aba", "aba");

        expectMatch(MSUB, "xaba", "");
        expectMatch(MSUB, "xaba", "a");
        expectMatch(MSUB, "xaba", "aa");
        expectNotMatch(MSUB, "xaba", "aaa");

        expectMatch(MSUB, "xaba", "aba");
    }

    public void testWordPrefixMatch() {
        expectMatch(MPREFIX, "", "");
        expectNotMatch(MPREFIX, "", "aa");
        expectMatch(MPREFIX, "aba", "a");
        expectNotMatch(MPREFIX, "aba", "b");
        expectNotMatch(MPREFIX, "aba", "aa");
        // Adding spaces to the query or description should not matter
        expectMatch(MPREFIX, "  aba", " a");
        expectNotMatch(MPREFIX, " aba", " b");
        expectNotMatch(MPREFIX, " aba", " aa");
        // non-space too
        expectMatch(MPREFIX, "  aba aba", " a a");
        expectNotMatch(MPREFIX, "  aba aba", " aa");
        expectMatch(MPREFIX, "  aba-aba", " a a");
        expectNotMatch(MPREFIX, "  a1a-a1a", " aa a");
    }


    private void expectBetterMatch(String query, String better, String worse) {
        float scbetter = EventTable.match(better, query);
        float scworse = EventTable.match(worse, query);
        String msg = String.format("Query '%s' expects '%s' match better()")
    }

    public void testQuery() {

    }

}