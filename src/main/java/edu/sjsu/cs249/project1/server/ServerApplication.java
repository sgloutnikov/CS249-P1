package edu.sjsu.cs249.project1.server;

public class ServerApplication {

    public static void main(final String[] args) {
        // TODO: On start prompt for bind address and port
        /**
         * We need to determine the flow of events. David's thoughts: <br/>
         * 1. If the client invokes a read operation, then the client is responsible for caching and registering the
         * cache event with the server (via exposing ClientCacheManager.getInstance().registerCachedFile(client,
         * fileName)). <br/>
         * 2. The client can create, delete, and/or modify WITHOUT having a cached copy (meaning read was not called
         * first). All operations can be independent, and the only one which requires a cache is read. In other words,
         * if I am a client, I can modify a file by directly sending the replacement data, even if I don't have the
         * current data stored locally (i.e., blind overwrite). <br/>
         * 3. Server can expose 5 RMI methods - Create, Delete, Read, Modify, and Register Cache. <br/>
         * 4. Client can expose 1 RMI method - Delete Cache.
         */
        System.out.println("Hello Server...");
    }
}
