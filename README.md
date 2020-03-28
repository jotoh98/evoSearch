<script src="https://polyfill.io/v3/polyfill.min.js?features=es6"></script>
<script id="MathJax-script" async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"></script>

<p align="center">
  <img src="icon.png" width="128px">
</p>

# What is evoSearch?

This project arises in the course of my bachelor thesis, which examines the behavior of genetic algorithms on the following geometric problem: We choose a certain amount of equally distributed one-dimensional sub-spaces of the two-dimensional space, all sharing the origin as one element. In one of those sub-spaces we place a treasure point and get a list of distances which are transferred into points with corresponding distances to the origin. Now the problem is:

**How do we distribute these points to find treasures efficiently?**


# Definitions
### Trace length
Length of the visited path to find a treasure point.
Let \(a=(a_1,\dots,a_n)\) be the individual with \(a_i = (p, d)\) being a genome consisting of the position \(p\) and the distance to origin \(d\).

## Step 1: Two-way problem
The first step of evoSearch's research lies in the consideration of the two-way problem. In two-way, the point to be found is an element of either the positive, or the negative real numbers and we start to again search from the origin. For this problem it is already known that the optimal strategy is the doubling strategy. We run one length unit in one direction, run back to the origin, then the doubled length in the other direction. This process is repeated until the treasure is found. The first part of the research is to find out if a genetic algorithm will mimic this behavior.

## First test set: One-treasure-fitness
For the first set of tests, the fitness function is evaluated as the run distance of the individual which was necessary to find the treasure.
