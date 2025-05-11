module P2PLink {
    requires java.net.http;
    requires ice4j;
    requires java.desktop;
    requires jitsi.metaconfig;
    requires jitsi.utils;
    requires kotlin.stdlib;
    requires jicoco.config;
    requires typesafe.config;
    requires kotlin.reflect;

    
    exports com.courtesilol.P2PLink;
    exports com.courtesilol.P2PLink.client;
    exports com.courtesilol.P2PLink.records;
    exports com.courtesilol.P2PLink.server;
}
