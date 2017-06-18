# DataProcessor

This class contains methods to calculate the concentration of the ants within the Grid and builds a JSON Document to transfer the data to the front-end visualisation.  


## methods

Name | Return Value | Description
------------ | ------------- | -------------
[**transformGrid**](DataProcessor.md#transformGrid) | String | calculates concentration of ants |
[**laodRDD**](DataProcessor.md#loadRDD) | MongoRDD[Document] | creates connection to MongoDB and sets configurations |
[**parseToJson**](DataProcessor.md#parseToJson) | String | parses GridRepresentattion to Json |
[**extractConfig**](DataProcessor.md#extractConfig) | MongoRDD[Document] | extracts the configuration parameter out of the database |
[**extractAntPos**](DataProcessor.md#extractAntPos) | MongoRDD[Document] | extracts all positions out of the database |
[**filterCurrentPos**](DataProcessor.md#filterCurrentPos) | Boolean | finds the current position of each ant |


# **transformGrid**
> String transformGrid (gridRequest: GridRequest)

Calculates the concentration for each intervall. The concentration is defined as the number of ants in each part of the Grid at a given period of time. The data is stored and transferred in Json.

### parameters
Name | Type | Description
------------- | ------------- | -------------
 **GridRequest** | **GridRequest**| Contains a Collection of Strings, coordinates x (int) and y (int) and a timestep (int) |

### Return type
String



# **loadRDD**
> MongoRDD[Document] loadRDD (gridRequest: GridRequest)

Defines the SparkContext and configuration for the Database. 

### parameters
Name | Type | Description
------------- | ------------- | -------------
 **gridRequest** | **GridRequest**| class GridRequest(collection: String, x: Int, y: Int, timestep: Int) |

### Return type
MongoRDD[Document]



# **parseToJson**
> String parseToJson (gridRep: List[GridRepresentation])

This method parses the transferred data to Json

### parameters
Name | Type | Description
------------- | ------------- | -------------
 **gridRep** | **List[GridRepresentation]**| class GridRepresentation (step: Int, time: Long, fields: Array[Document]) |

### Return type
String



# **extractConfig**
> MongoRDD[Document] extractConfig (rdd: MongoRDD[Document])

Extracts the Configuration parameters out of the database.

### parameters
Name | Type | Description
------------- | ------------- | -------------
**rdd** | **MongoRDD[Document]**|  |


### Return type
MongoRDD[Document]



# **extractAntPos**
> MongoRDD[Document] extractAntPos (rdd: MongoRDD[Document])

Extracts all positions of the Ants out of the database.

### parameters
Name | Type | Description
------------- | ------------- | -------------
**rdd** | **MongoRDD[Document]**|  |


### Return type
MongoRDD[Document]




# **filterCurrentPos**
> Boolean filterCurrentPos (gridRequest: GridRequest,doc: Document, currentMillis: Long)

Filters all positions and finds the current position of each Ant.

### parameters
Name | Type | Description
------------- | ------------- | -------------
**gridRequest** | **GridRequest**| class GridRequest(collection: String, x: Int, y: Int, timestep: Int) |
**doc** | **Document**|  |
**currentMillis** | **long**| timestamp in milliseconds |


### Return type
**true**: if the position within the document is the current position of the ant
 
**false**: if the position within the document isn't the current position of the ant




