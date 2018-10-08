Recommender Systems Sample Code
===

## Introduction


This repo contains sample code for building Collaborative Filtering and Content-Based recommender systems in Scala.

Three files are provided:

- `input/ratings.csv` contains ratings from a user to a movie.

|userId|movieId|rating| timestamp|
|-----:|------:|-----:|---------:|
|     1|  81834|   5.0|1425942133|
|     1| 112552|   5.0|1425941336|
|     1|  98809|   0.5|1425942640|

- `input/movies_metadata.csv` contains the metadata of movies.

|adult|belongs_to_collection                                                                                   |budget   |genres                                                   |homepage                                    |movieId|imdb_id  |original_language|original_title                |overview                                                                                                                                                                                                                                                                                                                                                                                                                                                        |popularity|poster_path                     |production_companies                                                                                                                                                                                          |production_countries                                                  |release_date|revenue     |runtime|spoken_languages                          |status  |tagline                                                                                                                                                    |title                         |video|vote_average|vote_count|
|-----|--------------------------------------------------------------------------------------------------------|---------|---------------------------------------------------------|--------------------------------------------|-------|---------|-----------------|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|--------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|------------|------------|-------|------------------------------------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------|-----|------------|----------|
|false|[10194,Toy Story Collection,/7G9915LfUQ2lVfwMEEhDsn3kT4B.jpg,/9FBwqcd9IRruEDUrTdcaafOMKUq.jpg]          |3.0E7    |[[16,Animation], [35,Comedy], [10751,Family]]            |http://toystory.disney.com/toy-story        |862    |tt0114709|en               |Toy Story                     |Led by Woody, Andy's toys live ...                                                                   |21.946943 |/rhIRbceoE9lR4veEXuwCC2wARtG.jpg|[[3,Pixar Animation Studios]]                                                                                                                                                                                 |[[US,United States of America]]                                       |1995-10-30  |3.73554033E8|81.0   |[[en,English]]                            |Released|null                                                                                                                                                       |Toy Story                     |False|7.7         |5415      |
|false|null                                                                                                    |6.5E7    |[[12,Adventure], [14,Fantasy], [10751,Family]]           |null                                        |8844   |tt0113497|en               |Jumanji                       |When siblings Judy and Peter ...|17.015539 |/vzmL6fP7aPKNKPRTFnZmiUfciyV.jpg|[[559,TriStar Pictures], [2550,Teitler Film], [10201,Interscope Communications]]                                                                                                                              |[[US,United States of America]]                                       |1995-12-15  |2.62797249E8|104.0  |[[en,English], [fr,Français]]             |Released|Roll the dice and unleash the excitement!                                                                                                                  |Jumanji                       |False|6.9         |2413      |

- `input/evaluation_ratings.csv` is the file with ratings to be predicted.

|userId|movieId|rating|
|-----:|------:|-----:|
|     1|    110|     ?|
|     1|   1968|     ?|
|     1|   4878|     ?|

The submission will be evaluated by RMSE.


## Getting Started

Before starting, make sure Scala 2.11 and sbt 1.2 are installed.

1. Clone repo `git clone https://github.com/qikaigu/recommender-systems.git`
2. Copy the 3 csv files into `input/`
3. Compile `sbt compile`
4. Test `sbt test`
5. Run `sbt run`

The submission file can be found in `output/` after execution.


## Description

### Collaborative Filtering

The basic idea is to find users having similar behaviour (highly / lowly rated on common movies), then recommend to a user movies highly rated by their similar users.

Based on this idea, the content of movies is not needed, only ratings on (user, movie) pairs are required.

Technically, to solve this problem, a common way is to consider the the ratings as a user-movie 2 dimensional sparse rating matrix, and fill the matrix with matrix decomposition methods.

There is an existing implementation of [alternating least squares (ALS)](http://spark.apache.org/docs/2.3.2/ml-collaborative-filtering.html) in Apache Spark. I directly applied it for the collaborative filtering model and obtained ~0.85 RMSE on 0.8/0.2 randomly train test split.


### Content-Based

For a content-based approach, a user profile containing the user's preference over movie features, such as genres, language, adults only or not, etc. will be determined for each user. Once we got the user profiles, we can then find the distance between a user profile and a movie by calculating the distance of the two vectors.

To calculate the user profiles, it is possible to approach it by optimizing the squared distance between the dot product of a user profile and features of a movie and the actual rating of the user on the movie, which is basically a linear regression optimization problem.

**Attention**: The code for this part is still WIP and not finished yet :( I'm not very familiar with linear algebra libraries in Scala at the moment. I've found [ND4S](https://github.com/deeplearning4j/nd4s) and use it together with Spark DataFrame, which doesn't seem to be a good idea converting through two different data structures. Also, DN4J doesn't support sparse matrix yet. Any recommendations are welcome.


## Future work

Some ideas of improvement are listed below. They are not implemented yet due to time reason.

- For both collaborative filtering and content-based models, normalize user ratings with each user's average rating.
- For the content-based model, feature engineering remains a big part of work to be done, such as extracting text features (TFIDF and/or Word2Vec), release decade and season, normalize / bucketize continuous features, etc.