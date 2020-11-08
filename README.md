# Proyecto MultiAgente Covid19

![Alt text](https://ichef.bbci.co.uk/news/800/cpsprodpb/FBA1/production/_111471446_virus.jpg "Proyecto Multiagente Covid19") 

Proyecto de la asignatura Agentes y Sistemas Multiagente del Máster en
Ciencia y Tecnología Informática de la Universidad Carlos III de Madrid.

La finalidad de este proyecto consiste en **simular mediante un sistema de
agentes la propagación de la COVID19** considerando distintas situaciones
y entidades con las que acercarse a la realidad lo máximo posible.

## Planteamiento de la solución

Para la ejecución del problema se tendrán en cuenta los distintos factores:

* 2 tipos de Agentes
    * Young people:
        * Probabilidad de ser asintomático alta
        * Probabilidad de complicaciones bajo
    * Adult people:
        * Probabilidad de ser asintomático baja
        * Probabilidad de complicaciones alto

* 4 estancias:
    * Bar: Agentes Young se quitarán la mascarilla, mientras agentes Old se la dejarán puesta
    * Work: Misma probabilidad de contagio para todos los agentes
    * Hospital: Los agentes contagiados irán a la casilla Hospital en caso de enfermar
    * Home: Probabilidad de contagio 0 (todos los agentes viven en casas distintas)
    
    ## Esquema de la solución propuesta

En la siguiente imagen veremos un esquema del output que proporcionará el sistema:

![Esquema del output](/images/screen.png)
