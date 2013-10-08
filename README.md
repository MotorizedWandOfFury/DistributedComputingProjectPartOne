Distributed Computing Project PartOne
==================================
Due: Oct 21, 2013

DC Protocol

structure: \<command> \<param1> \<param2>...\<param n>


Protocol Scenarios in depth
------------------

Client Tells CSRouter it exists

-Client connects with CSRouter and sends message HELLO \<clientname> \<portnumber>

-CSRouter receives this message

-CSRouter connects with ServerRouter

-CSRouter sends message ADD \<clientname> \<portnumber> 

-ServerRouter adds the client name and port number to routing table

-CSRouter disconnects from ServerRouter

-CSRouter responds to client ACKNOWLEDGED

-Client receives message and disconnects


---------------


Client shuts down

-Client connects with CSRouter and sends message BYE \<clientname>

-CSRouter receives message

-CSRouter connects with ServerRouter

-CSRouter sends message REMOVE \<clientname>

-ServerRouter removes client name from routing table

-CSRouter disconnects from ServerRouter

-Client disconnects from CSRouter




-----------


Client requests a file

-Client connects with CSRouter and sends message GET \<filename> \<fromclient>

-CSRouter receives this message

-CSRouter connects with ServerRouter

-CSRouter sends message FIND \<fromclient>

-ServerRouter looks up client and returns NOTFOUND if fromClient does not exist in table or FOUND <portnumber> if client resolution was successful

-CSRouter connects with fromClient at portnumber

-CSRouter sends message GET \<filename>

-fromClient responds with FILENOTFOUND if file does not exist on fromClient, or FILE if it does

-CSRouter prepares for file reception if it receives FILE

-CSRouter gets file

-CSRouter disconnects from fromClient

-CSRouter sends message to Client SEND \<filename>

-Client responds with READY

-CSRouter proceeds to send file

-Client receives file and disconnects

Note: CSRouter will respond with REQUESTUNSUCCESSFULL if at any point there is a failure, after which Client will disconnect
