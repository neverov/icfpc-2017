## Lambda Riot

- Victor Klochikhin
- Ivan Samsonov
- Andrey Neverov

## Installation

Installs [leiningen](https://leiningen.org) – clojure project automation tool via wget.

## About

Our solution is written in clojure and run using leiningen.

Our alghoritm is quite simple: we choose the mine to start from to be the most "central". 
Each turn we assess the possible score gain of each move using the scoring function and choose the most promising one.