# tch_server
Server component of the TCruch - TrueCrypt detection and distributed attack system


# TCrunch
This was devised as a method to more accurately detect TrueCrypt containers on a host than the tools already available as well as provide an efficient distributed attack system in order to maximize available CPU power.

The detection system works on a combination of the Chi-Square and Monte Carlo Pi tests, the results being far more accurate than other detection systems.

The attack system is a dynamic heterogeneous distributed attack structure allowing for the addition and removal of attacker machines at will without loss or degradation of the attack process.

# Component purpose
This is the server element for the distriburted attack system. This component manages all the incoming and outgoing requests from all connected nodes and clients for attack operations against TrueCrype containers. 

The system works on a RESTFUL system (I know not the most secure but it worked). The server holds the appropriate file fragement on file and distributes it to attached nodes as and when they become available. All nodes are recorded with an ID and their attack sequence co-ordinated against the heteregeneous balancing mechanisms employed. 

The server has a basic form of security and user account settings, this means that only those with the appropriate settings and credentials can connect and make requests on its internal functions. This is by no means a secure system, but rather a redimentary incarnation of the server structure. The attack sequencing works nicely though. 
