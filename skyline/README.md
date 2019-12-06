## Skyline queries
The Skyline operator is the subject of an optimization problem, used in a query to filter results from a database 
to keep only those objects that are not worse than any other.

A classic example of application of the Skyline operator involves selecting a hotel for a holiday. 
The user wants the hotel to be both cheap and close to the beach. However, hotels that are close to the beach may also
be expensive. In this case, the Skyline operator would only present those hotels that are not worse than any other 
hotel in both price and distance to the beach.

Paper: http://delab.csd.auth.gr/papers/IISA2015tpm.pdf

The [skyline.pdf](skyline.pdf) contains the problem statement.