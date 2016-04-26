LDP-CoAP: A CoAP Mapping for Linked Data Platform
===================

[W3C Linked Data Platform 1.0 specification](http://www.w3.org/TR/ldp/) defines resource management primitives for HTTP only, pushing into the background not-negligible 
use cases related to Web of Things (WoT) scenarios where HTTP-based communication and infrastructures are unfeasible. 

LDP-CoAP proposes a mapping of the LDP specification for [RFC 7252 Constrained Application Protocol](https://tools.ietf.org/html/rfc7252) (CoAP) 
and a complete Java-based framework to publish Linked Data on the WoT. 

A general translation of LDP-HTTP requests and responses is provided, as well as a fully comprehensive framework for HTTP-to-CoAP proxying. 

LDP-CoAP framework can be also tested using the [W3C Test Suite for LDP](http://w3c.github.io/ldp-testsuite/).

Modules
-------------

LDP-CoAP consists of the following sub-projects:

- _ldp-coap-core_: basic framework implementation including the proposed LDP-CoAP mapping;
- _californium-core-ldp_: a modified version of the [Californium CoAP framework](https://github.com/eclipse/californium). Californium-core library was extended to support LDP features;
- _ldp-coap-proxy_: a modified version of the [californium-proxy](http://github.com/eclipse/californium/tree/master/californium-proxy) used to translate LDP-HTTP request methods and headers 
into the corresponding LDP-CoAP ones and then map back LDP-CoAP responses to LDP-HTTP;
- _ldp-coap-android_: porting of _ldp-coap-core_ on Android platform;
- _ldp-coap-raspberry_: usage examples exploiting _ldp-coap-core_ on a [Raspberry Pi board](http://www.raspberrypi.org/);

Usage with Eclipse and Maven
-------------

Each module also includes the project files for Eclipse. Make sure to have the following plugins before importing LDP-CoAP projects:

- [Eclipse EGit](http://www.eclipse.org/egit/)
- [M2Eclipse - Maven Integration for Eclipse](http://www.eclipse.org/m2e/)

License
-------------

_ldp-coap-core_, _ldp-coap-android_ and _ldp-coap-raspberry_ modules are distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).


Contact
-------------

For more information, please visit the [LDP-CoAP webpage](http://sisinflab.poliba.it/swottools/ldp-coap/).

---------