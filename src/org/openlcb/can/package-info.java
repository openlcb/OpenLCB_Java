/**
CAN-specific OpenLCB functionality.

<p>
This package does not provide a CAN implementation; that's 
to be done in a device-specific way elsewhere.

<p>
For testing purposes, that implementation is currently
taken from the tools.cansim package in the test code tree.

<p>
Although Java guarantees that an int is 32 bits and a long
is 64 bits, we want this code to be easy to translate to other
languages (e.g. C) and smaller processors.  We therefore use int for 
quantities that must be at least 16 bits, and long for 
quantities that must be at least 32 bits. Since Java does
not have an "unsigned" modifier, we add that as a comment
on appropriate declarations.

<p>
The OpenLcbCanFrame class centralizes
the frame formatting and in most cases deformatting
specific CAN frame contents to numbers and other values.

<p>
The connection between the {@link org.openlcb} message classes
(e.g. {@link org.openlcb.VerifiedNodeIDNumberMessage} et al)
and CAN frames is through the MessageBuilder class.

<p>
The AliasMap class maintains mapping between NodeIDs and aliases
by monitoring all frame-level traffic.  (This is an absolute
responsibility, and some day may require being able to send frames
to handle ambiguities due to e.g. joining a segment late)
It in turn is used to get the source full NodeID for received
messages from the source alias.

<h2>Related Documentation</h2>


(Pointers to CAN info)

*/
package org.openlcb.can;
