# What is evoSearch?

This project arises in the course of my bachelor thesis, which examines the behavior of genetic algorithms on the following
geometric problem: We choose a certain amount of equally distributed one-dimensional sub-spaces of the two-dimensional space,
all sharing the origin as one element. In one of those sub-spaces we place a treasure point and get a list of distances which
are transferred into points with correpsonding distances to the origin. Now the problem is: **How do we distribute these points
to find treasures efficiently?**

# Step 1: Two-way problem
The first step of evoSearch's research lies in the consideration of the two-way problem. In two-way, the point to be found is
an element of either the positive, or the negative real numbers and we start to again search from the origin. For this problem
it is already known that the optimal strategy is the doubling strategy. We run one length unit in one direction, run back to the
origin, then the doubled length in the other direction. This process is repeated until the treasure is found.
The first part of the research is to find out if a genetic algorithm will mimic this behavior.
