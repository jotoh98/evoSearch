<p align="center">
  <img src="https://github.com/jotoh98/evoSearch/workflows/JavaDoc/badge.svg" alt="javadoc"/>
  <img src="https://github.com/jotoh98/evoSearch/workflows/Test%20&%20Build%20with%20maven/badge.svg" alt="maven"/>
  <img src="https://www.deepcode.ai/api/gh/badge?key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwbGF0Zm9ybTEiOiJnaCIsIm93bmVyMSI6ImpvdG9oOTgiLCJyZXBvMSI6ImV2b1NlYXJjaCIsImluY2x1ZGVMaW50IjpmYWxzZSwiYXV0aG9ySWQiOjE1ODE2LCJpYXQiOjE1OTc5MjMwMzN9.doZjCykUD9Bol5VroHvwzckxC1rk9EMf7uNDSKJ-nlQ" alt="DeepCode badge">
</p>

<p align="center">
  <img src="icon.png" width="128px">
</p>

# How to run

1. Install IntelliJ
2. Clone the project fom Github using git
3. Install the lombok plugin https://projectlombok.org/setup/intellij
4. Enable 'Annotation processing' in Preferences > Build, Execution, Deployment > Compiler > Annotation Processors
5. In the maven Tab on the right, hit refresh to download the dependencies
6. Click on run

# What is evoSearch?

This project arises in the course of my bachelor thesis, which examines the behavior of genetic algorithms on the following geometric problem: We choose a certain amount of equally distributed one-dimensional sub-spaces of the two-dimensional space, all sharing the origin as one element. In one of those sub-spaces we place a treasure point and get a list of distances which are transferred into points with corresponding distances to the origin. Now the problem is:

**How do we distribute these points to find treasures efficiently?**

