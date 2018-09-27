package org.openlcb.implementations;

import org.junit.*;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created by bracz on 4/3/17.
 */
public class VersionedValueTest  {
    VersionedValue<Integer> v = new VersionedValue<>(42);

    @Test
    public void testDefaultAndSet() throws Exception {
        Assert.assertEquals("default value", Integer.valueOf(42), v.getLatestData());
        Assert.assertEquals(v.DEFAULT_VERSION, v.getVersion());
        Assert.assertTrue(v.isVersionAtDefault());
        Assert.assertEquals(42, (int)v.getLatestData());

        v.set(13);
        Assert.assertEquals("set value", Integer.valueOf(13), v.getLatestData());
        assert(v.DEFAULT_VERSION < v.getVersion());
        Assert.assertFalse(v.isVersionAtDefault());
        Assert.assertEquals(13, (int)v.getLatestData());

        v.setVersionToDefault();
        assert(v.DEFAULT_VERSION < v.getVersion());
        Assert.assertTrue(v.isVersionAtDefault());
        Assert.assertEquals(13, (int)v.getLatestData());
    }

    @Test
    public void testSetWithForceNotify() throws Exception {
        v.set(33); // gets rid of the default value condition

        MockVersionedValueListener<Integer> l1 = new MockVersionedValueListener<>(v);
        MockVersionedValueListener<Integer> l2 = new MockVersionedValueListener<>(v);

        l1.setFromOwnerWithForceNotify(33);
        verify(l2.stub).update(33);
        verifyNoMoreInteractions(l2.stub);
        verifyNoMoreInteractions(l1.stub);
        reset(l1.stub, l2.stub);

        l2.setFromOwnerWithForceNotify(33);
        verify(l1.stub).update(33);
        verifyNoMoreInteractions(l2.stub);
        verifyNoMoreInteractions(l1.stub);
        reset(l1.stub, l2.stub);

        v.setWithForceNotify(v.getNewVersion(), 33);
        verify(l1.stub).update(33);
        verify(l2.stub).update(33);
        verifyNoMoreInteractions(l2.stub);
        verifyNoMoreInteractions(l1.stub);
        reset(l1.stub, l2.stub);
    }

    @Test
    public void testSet() throws Exception {
        MockVersionedValueListener<Integer> l1 = new MockVersionedValueListener<>(v);
        MockVersionedValueListener<Integer> l2 = new MockVersionedValueListener<>(v);

        l1.setFromOwner(42);
        verify(l2.stub).update(42);
        verifyNoMoreInteractions(l2.stub);
        // no callback to the owner
        verifyNoMoreInteractions(l1.stub);
        reset(l1.stub, l2.stub);

        // skips duplicate notification
        l1.setFromOwner(42);
        verifyNoMoreInteractions(l2.stub);
        verifyNoMoreInteractions(l1.stub);
        reset(l1.stub, l2.stub);

        l1.setFromOwner(13);
        verify(l2.stub).update(13);
        verifyNoMoreInteractions(l2.stub);
        verifyNoMoreInteractions(l1.stub);
        reset(l1.stub, l2.stub);

        l2.setFromOwner(81);
        verify(l1.stub).update(81);
        verifyNoMoreInteractions(l2.stub);
        verifyNoMoreInteractions(l1.stub);
        reset(l1.stub, l2.stub);

        // Unowned set will trigger callbacks both ways.
        v.set(33);
        verify(l1.stub).update(33);
        verify(l2.stub).update(33);
        verifyNoMoreInteractions(l2.stub);
        verifyNoMoreInteractions(l1.stub);
        reset(l1.stub, l2.stub);

        // skips duplicate notification
        l1.setFromOwner(33);
        verifyNoMoreInteractions(l2.stub);
        verifyNoMoreInteractions(l1.stub);
        reset(l1.stub, l2.stub);

        // After reset to default we will get an update notification.
        v.setVersionToDefault();
        l1.setFromOwner(33);
        verify(l2.stub).update(33);
        verifyNoMoreInteractions(l1.stub);
        reset(l1.stub, l2.stub);

        // After reset to default we will get both update notifications.
        v.setVersionToDefault();
        v.set(33);
        verify(l1.stub).update(33);
        verify(l2.stub).update(33);
        reset(l1.stub, l2.stub);

        // After reset to default we will get an update notification.
        v.setVersionToDefault();
        l1.setFromOwner(42);
        verify(l2.stub).update(42);
        verifyNoMoreInteractions(l1.stub);
        reset(l1.stub, l2.stub);
    }

    @Test
    public void testOutOfOrder() throws Exception {
        v.set(33);

        MockVersionedValueListener<Integer> l1 = new MockVersionedValueListener<>(v);

        int ver1 = v.getNewVersion();
        int ver2 = v.getNewVersion();
        v.set(ver2, 21);

        verify(l1.stub).update(21);
        verifyNoMoreInteractions(l1.stub);
        reset(l1.stub);

        v.set(ver1, 13);
        // out of order update will be swallowed.
        verifyNoMoreInteractions(l1.stub);
        reset(l1.stub);
    }

}
